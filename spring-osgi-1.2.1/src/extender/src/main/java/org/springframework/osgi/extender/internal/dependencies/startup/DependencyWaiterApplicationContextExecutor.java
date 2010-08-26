/*
 * Copyright 2006-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.osgi.extender.internal.dependencies.startup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.osgi.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.springframework.osgi.context.OsgiBundleApplicationContextExecutor;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEventMulticaster;
import org.springframework.osgi.context.event.OsgiBundleContextFailedEvent;
import org.springframework.osgi.extender.internal.util.concurrent.Counter;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.Assert;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Dependency waiter executor that breaks the 'traditional' {@link ConfigurableApplicationContext#refresh()} in two
 * pieces so that beans are not actually created unless the OSGi service imported are present.
 * 
 * <p/> <p/> <p/> Supports both asynch and synch behaviour.
 * 
 * @author Hal Hildebrand
 * @author Costin Leau
 */
public class DependencyWaiterApplicationContextExecutor implements OsgiBundleApplicationContextExecutor,
		ContextExecutorStateAccessor {

	private static final Log log = LogFactory.getLog(DependencyWaiterApplicationContextExecutor.class);

	/**
	 * this class monitor. Since multiple threads will access this object, we have to use synchronization to guarantee
	 * thread visibility
	 */
	private final Object monitor = new Object();

	/** waiting timeout */
	private long timeout;

	/** the timer used for executing the timeout */
	// NOTE: the dog is not managed by this application so do not cancel it
	private Timer watchdog;

	/** watchdog task */
	private TimerTask watchdogTask;

	/** OSGi service dependencyDetector used for detecting dependencies */
	protected DependencyServiceManager dependencyDetector;

	protected final DelegatedExecutionOsgiBundleApplicationContext delegateContext;

	/** State of the associated context from the executor POV. */
	private ContextState state = ContextState.INITIALIZED;

	private TaskExecutor taskExecutor;

	/**
	 * A synchronized counter used by the Listener to determine the number of children to wait for when shutting down.
	 */
	private Counter monitorCounter;

	/** Should the waiting be synchrous or not ? */
	private final boolean synchronousWait;

	/** Counter used when waiting for dependencies to appear */
	private final Counter waitBarrier = new Counter("syncCounterWait");

	/** delegated multicaster */
	private OsgiBundleApplicationContextEventMulticaster delegatedMulticaster;

	private List dependencyFactories;

	/**
	 * The task for the watch dog.
	 * 
	 * @author Hal Hildebrand
	 */
	private class WatchDogTask extends TimerTask {

		public void run() {
			timeout();
		}
	}

	/**
	 * Create the Runnable action which will complete the context creation process. This process can be called
	 * synchronously or asynchronously, depending on context configuration and availability of dependencies.
	 * 
	 * @author Hal Hildebrand
	 * @author Costin Leau
	 */
	private class CompleteRefreshTask implements Runnable {

		public void run() {
			boolean debug = log.isDebugEnabled();
			if (debug) {
				log.debug("Completing refresh for " + getDisplayName());
			}

			synchronized (monitor) {
				if (state != ContextState.DEPENDENCIES_RESOLVED) {
					logWrongState(ContextState.DEPENDENCIES_RESOLVED);
					return;
				}
			}

			// Continue with the refresh process...
			delegateContext.completeRefresh();

			// Once we are done, tell the world
			synchronized (monitor) {
				// Close might have been called in the meantime
				if (state != ContextState.DEPENDENCIES_RESOLVED) {
					return;
				}
				state = ContextState.STARTED;
			}
		}
	}

	public DependencyWaiterApplicationContextExecutor(DelegatedExecutionOsgiBundleApplicationContext delegateContext,
			boolean syncWait, List dependencyFactories) {
		this.delegateContext = delegateContext;
		this.delegateContext.setExecutor(this);
		this.synchronousWait = syncWait;
		this.dependencyFactories = dependencyFactories;

		synchronized (monitor) {
			watchdogTask = new WatchDogTask();
		}
	}

	/**
	 * Provide a continuation like approach to the application context. Will execute just some parts of refresh and then
	 * leave the rest of to be executed after a number of conditions have been met.
	 */
	public void refresh() throws BeansException, IllegalStateException {
		if (log.isDebugEnabled())
			log.debug("Starting first stage of refresh for " + getDisplayName());

		// sanity check
		init();

		// start the first stage
		stageOne();
	}

	/**
	 * Do some sanity checks
	 */
	protected void init() {
		synchronized (monitor) {
			Assert.notNull(watchdog, "watchdog timer required");
			Assert.notNull(monitorCounter, " monitorCounter required");
			if (state != ContextState.INTERRUPTED && state != ContextState.STOPPED)
				state = ContextState.INITIALIZED;
			else {
				RuntimeException ex = new IllegalStateException("cannot refresh an interrupted/closed context");
				log.fatal(ex);
				throw ex;
			}
		}
	}

	/**
	 * Start the first stage of the application context refresh. Determines the service dependencies and if there are
	 * any, registers a OSGi service dependencyDetector which will continue the refresh process asynchronously. <p/>
	 * Based on the {@link #synchronousWait}, the current thread can simply end if there are any dependencies (the
	 * default) or wait to either timeout or have all its dependencies met.
	 */
	protected void stageOne() {

		boolean debug = log.isDebugEnabled();

		try {
			if (debug)
				log.debug("Calling preRefresh on " + getDisplayName());

			synchronized (monitor) {

				// check before kicking the pedal
				if (state != ContextState.INITIALIZED) {
					logWrongState(ContextState.INITIALIZED);
					return;
				}

				state = ContextState.RESOLVING_DEPENDENCIES;
			}

			delegateContext.startRefresh();

			if (debug)
				log.debug("Pre-refresh completed; determining dependencies...");

			Runnable task = null;

			if (synchronousWait) {
				task = new Runnable() {

					public void run() {
						// inform the waiting thread through the counter
						waitBarrier.decrement();
					}
				};
			} else
				task = new Runnable() {

					public void run() {
						// no waiting involved, just call stageTwo
						stageTwo();
					}
				};

			DependencyServiceManager dl = createDependencyServiceListener(task);
			dl.findServiceDependencies();

			// all dependencies are met, just go with stageTwo
			if (dl.isSatisfied()) {
				log.info("No outstanding OSGi service dependencies, completing initialization for " + getDisplayName());
				stageTwo();
			}

			else {
				// there are dependencies not met
				// register a listener to look for them
				synchronized (monitor) {
					dependencyDetector = dl;
				}

				if (debug)
					log.debug("Registering service dependency dependencyDetector for " + getDisplayName());

				dependencyDetector.register();

				if (synchronousWait) {
					waitBarrier.increment();
					if (debug)
						log.debug("Synchronous wait-for-dependencies; waiting...");

					// if waiting times out...
					if (waitBarrier.waitForZero(timeout)) {
						timeout();
					} else
						stageTwo();
				} else {
					// start the watchdog (we're asynch)
					startWatchDog();
				}
			}
		} catch (Throwable e) {
			fail(e);
		}

	}

	protected void stageTwo() {
		boolean debug = log.isDebugEnabled();

		if (debug)
			log.debug("Starting stage two for " + getDisplayName());

		synchronized (monitor) {

			if (state != ContextState.RESOLVING_DEPENDENCIES) {
				logWrongState(ContextState.RESOLVING_DEPENDENCIES);
				return;
			}

			stopWatchDog();
			state = ContextState.DEPENDENCIES_RESOLVED;
		}

		// always delegate to the taskExecutor since we might be called by the
		// OSGi platform listener
		taskExecutor.execute(new CompleteRefreshTask());
	}

	/**
	 * The application context is being shutdown. Deregister the listener and prevent classes from being loaded since
	 * it's Doom's day.
	 */
	public void close() {
		boolean debug = log.isDebugEnabled();

		boolean normalShutdown = false;
		stopWatchDog();

		synchronized (monitor) {

			// no need for cleanup
			if (state.isDown()) {
				return;
			}

			if (debug) {
				log.debug("Closing appCtx for " + getDisplayName());
			}

			// It's possible for the delegateContext to already be in startRefresh() or completeRefresh().
			// If this is the case then its important to wait for these tasks to complete and then close normally
			// If we simply exit then the bundle may suddnely become invalid under our feet, e.g. if this
			// was triggered by a Bundle update or uininstall.

			// Context is in stageOne(), wait until stageOne() is complete
			// and destroy singletons
			if (state == ContextState.RESOLVING_DEPENDENCIES) {
				if (debug)
					log.debug("Cleaning up appCtx " + getDisplayName());
				synchronized (delegateContext.getMonitor()) {
					if (delegateContext.isActive()) {
						try {
							delegateContext.getBeanFactory().destroySingletons();
						} catch (Exception ex) {
							log.trace("Caught exception while interrupting context refresh ", ex);
						}
					}
					state = ContextState.INTERRUPTED;
				}
			}
			// Context is in stageTwo(), wait until stageTwo() is complete and
			// close normally.
			else if (state == ContextState.DEPENDENCIES_RESOLVED) {
				if (debug)
					log.debug("Shutting down appCtx " + getDisplayName() + " once stageTwo() is complete");
				synchronized (delegateContext.getMonitor()) {
					state = ContextState.STOPPED;
					normalShutdown = true;
				}
			}
			// Context is running, shut it down
			else if (state == ContextState.STARTED) {
				if (debug)
					log.debug("Shutting down normally appCtx " + getDisplayName());
				state = ContextState.STOPPED;
				normalShutdown = true;
			}
			// Something else going on
			else {
				if (debug)
					log.debug("No need to stop context (it hasn't been started yet)");
				state = ContextState.INTERRUPTED;
			}
			// Clean up the detector
			if (dependencyDetector != null) {
				dependencyDetector.deregister();
			}
		}
		try {
			if (normalShutdown) {
				delegateContext.normalClose();
			}
		} catch (Exception ex) {
			log.fatal("Could not succesfully close context " + delegateContext, ex);
		} finally {
			monitorCounter.decrement();
		}

	}

	/**
	 * Fail creating the context. Figure out unsatisfied dependencies and provide a very nice log message before closing
	 * the appContext. <p/> Normally this method is called when an exception is caught.
	 * 
	 * @param t - the offending Throwable which caused our demise
	 */
	private void fail(Throwable t) {

		// this will not thrown any exceptions (it just logs them)
		close();

		StringBuffer buf = new StringBuffer();

		synchronized (monitor) {
			if (dependencyDetector == null || dependencyDetector.getUnsatisfiedDependencies().isEmpty()) {
				buf.append("none");
			} else {
				for (Iterator dependencies = dependencyDetector.getUnsatisfiedDependencies().keySet().iterator(); dependencies
						.hasNext();) {
					MandatoryServiceDependency dependency = (MandatoryServiceDependency) dependencies.next();
					buf.append(dependency.toString());
					if (dependencies.hasNext()) {
						buf.append(", ");
					}
				}
			}
		}

		final StringBuffer message = new StringBuffer();
		message.append("Unable to create application context for [");
		AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				message.append(OsgiStringUtils.nullSafeSymbolicName(getBundle()));
				return null;
			}
		});
		message.append("], unsatisfied dependencies: ");
		message.append(buf.toString());

		log.error(message.toString(), t);

		// send notification
		delegatedMulticaster.multicastEvent(new OsgiBundleContextFailedEvent(delegateContext, delegateContext
				.getBundle(), t));
	}

	/**
	 * Cancel waiting due to timeout.
	 */
	private void timeout() {
		ApplicationContextException e;
		synchronized (monitor) {
			// deregister listener to get an accurate snapshot of the
			// unsatisfied dependencies.

			if (dependencyDetector != null) {
				dependencyDetector.deregister();
			}
		}

		log.warn("Timeout occurred before finding service dependencies for [" + delegateContext.getDisplayName() + "]");

		e = new ApplicationContextException("Application context initialization for '"
				+ (String) AccessController.doPrivileged(new PrivilegedAction() {

					public Object run() {
						return OsgiStringUtils.nullSafeSymbolicName(getBundle());
					}
				}) + "' has timed out");
		e.fillInStackTrace();
		fail(e);
	}

	protected DependencyServiceManager createDependencyServiceListener(Runnable task) {
		return new DependencyServiceManager(this, delegateContext, dependencyFactories, task, timeout);
	}

	/**
	 * Schedule the watchdog task.
	 */
	protected void startWatchDog() {
		boolean started = false;
		synchronized (monitor) {
			if (watchdogTask != null) {
				started = true;
				watchdog.schedule(watchdogTask, timeout);
			}
		}

		boolean debug = log.isDebugEnabled();
		if (debug) {
			if (started)
				log.debug("Asynch wait-for-dependencies started...");
			else
				log.debug("Dependencies satisfied; no need to start a watchdog...");
		}
	}

	protected void stopWatchDog() {
		boolean stopped = false;
		synchronized (monitor) {
			if (watchdogTask != null) {
				watchdogTask.cancel();
				watchdogTask = null;
				stopped = true;
			}
		}

		if (stopped && log.isDebugEnabled()) {
			log.debug("Cancelled dependency watchdog...");
		}
	}

	/**
	 * Sets the timeout (in ms) for waiting for service dependencies.
	 * 
	 * @param timeout
	 */
	public void setTimeout(long timeout) {
		synchronized (monitor) {
			this.timeout = timeout;
		}
	}

	public void setTaskExecutor(TaskExecutor taskExec) {
		synchronized (monitor) {
			this.taskExecutor = taskExec;
		}
	}

	private Bundle getBundle() {
		synchronized (monitor) {
			return delegateContext.getBundle();
		}
	}

	private String getDisplayName() {
		synchronized (monitor) {
			return delegateContext.getDisplayName();
		}

	}

	public void setWatchdog(Timer watchdog) {
		synchronized (monitor) {
			this.watchdog = watchdog;
		}
	}

	/**
	 * Reduce the code pollution.
	 * 
	 * @param expected the expected value for the context state.
	 */
	private void logWrongState(ContextState expected) {
		log.error("Expecting state (" + expected + ") not (" + state + ") for context [" + getDisplayName()
				+ "]; assuming an interruption and bailing out");
	}

	/**
	 * Pass in the context counter. Used by the listener to track the number of contexts started.
	 * 
	 * @param asynchCounter
	 */
	public void setMonitoringCounter(Counter contextsStarted) {
		this.monitorCounter = contextsStarted;
	}

	/**
	 * Sets the multicaster for delegating failing events.
	 * 
	 * @param multicaster
	 */
	public void setDelegatedMulticaster(OsgiBundleApplicationContextEventMulticaster multicaster) {
		this.delegatedMulticaster = multicaster;
	}

	//
	// accessor interface implementations
	//

	public ContextState getContextState() {
		synchronized (monitor) {
			return state;
		}
	}

	public OsgiBundleApplicationContextEventMulticaster getEventMulticaster() {
		return this.delegatedMulticaster;
	}
}
