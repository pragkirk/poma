
package org.springframework.osgi.extender.internal.dependencies.startup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.osgi.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.springframework.osgi.extender.OsgiServiceDependencyFactory;
import org.springframework.osgi.extender.event.BootstrappingDependencyEvent;
import org.springframework.osgi.extender.internal.util.PrivilegedUtils;
import org.springframework.osgi.service.importer.OsgiServiceDependency;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyEvent;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyWaitEndedEvent;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyWaitStartingEvent;
import org.springframework.osgi.util.OsgiListenerUtils;
import org.springframework.osgi.util.OsgiStringUtils;

/**
 * ServiceListener used for tracking dependent services. Even if the
 * ServiceListener receives event synchronously, mutable properties should be
 * synchronized to guarantee safe publishing between threads.
 * 
 * @author Costin Leau
 * @author Hal Hildebrand
 * @author Andy Piper
 */
public class DependencyServiceManager {

	private static final Log log = LogFactory.getLog(DependencyServiceManager.class);

	protected final Map dependencies = Collections.synchronizedMap(new LinkedHashMap());

	protected final Map unsatisfiedDependencies = Collections.synchronizedMap(new LinkedHashMap());

	private final ContextExecutorStateAccessor contextStateAccessor;

	private final BundleContext bundleContext;

	private final ServiceListener listener;

	private final DelegatedExecutionOsgiBundleApplicationContext context;

	/**
	 * Task to execute if all dependencies are met.
	 */
	private final Runnable executeIfDone;

	/** Maximum waiting time used in events when waiting for dependencies */
	private final long waitTime;

	/** dependency factories */
	private List dependencyFactories;


	/**
	 * Actual ServiceListener.
	 * 
	 * @author Costin Leau
	 * @author Hal Hildebrand
	 */
	private class DependencyServiceListener implements ServiceListener {

		/**
		 * Process serviceChanged events, completing context initialization if
		 * all the required dependencies are satisfied.
		 * 
		 * @param serviceEvent
		 */
		public void serviceChanged(ServiceEvent serviceEvent) {
			boolean trace = log.isTraceEnabled();

			try {
				if (unsatisfiedDependencies.isEmpty()) {

					// already completed but likely called due to threading
					if (trace) {
						log.trace("Handling service event, but no unsatisfied dependencies exist for "
								+ context.getDisplayName());
					}

					return;
				}

				ServiceReference ref = serviceEvent.getServiceReference();
				if (trace) {
					log.trace("Handling service event [" + OsgiStringUtils.nullSafeToString(serviceEvent) + ":"
							+ OsgiStringUtils.nullSafeToString(ref) + "] for " + context.getDisplayName());
				}

				updateDependencies(serviceEvent);

				ContextState state = contextStateAccessor.getContextState();

				// already resolved (closed or timed-out)
				if (state.isResolved()) {
					deregister();
					return;
				}

				// Good to go!
				if (unsatisfiedDependencies.isEmpty()) {
					deregister();
					// context.listener = null;
					log.info("No unsatisfied OSGi service dependencies; completing initialization for "
							+ context.getDisplayName());

					// execute task to complete initialization
					// NOTE: the runnable should be able to delegate any long
					// process to a
					// different thread.
					executeIfDone.run();
				}
			}
			catch (Throwable e) {
				// frameworks will simply not log exception for event handlers
				log.error("Exception during dependency processing for " + context.getDisplayName(), e);
			}
		}

		private void updateDependencies(ServiceEvent serviceEvent) {
			boolean trace = log.isTraceEnabled();
			boolean debug = log.isDebugEnabled();

			for (Iterator i = dependencies.keySet().iterator(); i.hasNext();) {
				MandatoryServiceDependency dependency = (MandatoryServiceDependency) i.next();

				// check if there is a match on the service
				if (dependency.matches(serviceEvent)) {
					switch (serviceEvent.getType()) {

						case ServiceEvent.REGISTERED:
						case ServiceEvent.MODIFIED:
							unsatisfiedDependencies.remove(dependency);
							if (debug) {
								log.debug("Found service for " + context.getDisplayName() + "; eliminating "
										+ dependency + ", remaining [" + unsatisfiedDependencies + "]");
							}

							sendDependencySatisfiedEvent(dependency);
							break;

						case ServiceEvent.UNREGISTERING:
							unsatisfiedDependencies.put(dependency, dependency.getBeanName());
							if (debug) {
								log.debug("Service unregistered; adding " + dependency);
							}
							sendDependencyUnsatisfiedEvent(dependency);
							break;
						default: // do nothing
							if (debug) {
								log.debug("Unknown service event type for: " + dependency);
							}
							break;
					}
				}
				else {
					if (trace) {
						log.trace(dependency + " does not match: "
								+ OsgiStringUtils.nullSafeToString(serviceEvent.getServiceReference()));
					}
				}
			}
		}
	}


	/**
	 * Create a dependency manager, indicating the executor bound to, the
	 * context that contains the dependencies and the task to execute if all
	 * dependencies are met.
	 * 
	 * @param executor
	 * @param context
	 * @param executeIfDone
	 */
	public DependencyServiceManager(ContextExecutorStateAccessor executor,
			DelegatedExecutionOsgiBundleApplicationContext context, List dependencyFactories, Runnable executeIfDone,
			long maxWaitTime) {
		this.contextStateAccessor = executor;
		this.context = context;
		this.dependencyFactories = new ArrayList(8);

		if (dependencyFactories != null)
			this.dependencyFactories.addAll(dependencyFactories);

		this.waitTime = maxWaitTime;
		this.bundleContext = context.getBundleContext();
		this.listener = new DependencyServiceListener();

		this.executeIfDone = executeIfDone;
	}

	protected void findServiceDependencies() throws Exception {
		try {

			PrivilegedUtils.executeWithCustomTCCL(context.getClassLoader(),
				new PrivilegedUtils.UnprivilegedThrowableExecution() {

					public Object run() throws Throwable {
						doFindDependencies();
						return null;
					}

				});
		}
		catch (Throwable th) {
			if (th instanceof Exception)
				throw ((Exception) th);
			throw (Error) th;
		}

		if (log.isDebugEnabled()) {
			log.debug(dependencies.size() + " OSGi service dependencies, " + unsatisfiedDependencies.size()
					+ " unsatisfied (for beans " + unsatisfiedDependencies.values() + ") in "
					+ context.getDisplayName());
		}

		if (!unsatisfiedDependencies.isEmpty()) {
			log.info(context.getDisplayName() + " is waiting for unsatisfied dependencies ["
					+ unsatisfiedDependencies.values() + "]");
		}
		if (log.isTraceEnabled()) {
			log.trace("Total OSGi service dependencies beans " + dependencies.values());
			log.trace("Unsatified OSGi service dependencies beans " + unsatisfiedDependencies.values());
		}
	}

	private void doFindDependencies() throws Exception {
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		boolean debug = log.isDebugEnabled();
		boolean trace = log.isTraceEnabled();

		if (trace)
			log.trace("Looking for dependency factories inside bean factory [" + beanFactory.toString() + "]");

		Map localFactories = BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory,
			OsgiServiceDependencyFactory.class, true, false);

		if (debug)
			log.debug("Discovered local dependency factories: " + localFactories.keySet());

		dependencyFactories.addAll(localFactories.values());

        // Sanity check that the bundle hasn't been pulled from underneath our feet.
        /*
        try {
             bundleContext.getBundle();
        } catch (IllegalStateException ise) {
            throw new IllegalStateException("Dependency management could not be completed for ["
                    + context.getDisplayName() +"] because the BundleContext is no longer valid.");
        }
        */

        for (Iterator iterator = dependencyFactories.iterator(); iterator.hasNext();) {
			OsgiServiceDependencyFactory dependencyFactory = (OsgiServiceDependencyFactory) iterator.next();
			Collection discoveredDependencies = null;

			try {
				discoveredDependencies = dependencyFactory.getServiceDependencies(bundleContext, beanFactory);
			}
			catch (Exception ex) {
				log.warn("Dependency factory " + dependencyFactory
						+ " threw exception while detecting dependencies for beanFactory " + beanFactory + " in "
						+ context.getDisplayName(), ex);
				throw ex;
			}
			// add the dependencies one by one
			if (discoveredDependencies != null)
				for (Iterator dependencyIterator = discoveredDependencies.iterator(); dependencyIterator.hasNext();) {
					OsgiServiceDependency dependency = (OsgiServiceDependency) dependencyIterator.next();
					MandatoryServiceDependency msd = new MandatoryServiceDependency(bundleContext, dependency);
					dependencies.put(msd, dependency.getBeanName());

					if (!msd.isServicePresent()) {
						log.info("Adding OSGi service dependency for importer [" + msd.getBeanName()
								+ "] matching OSGi filter [" + msd.filterAsString + "]");
						unsatisfiedDependencies.put(msd, dependency.getBeanName());
					}
				}
		}
	}

	protected boolean isSatisfied() {
		return unsatisfiedDependencies.isEmpty();
	}

	public Map getUnsatisfiedDependencies() {
		return unsatisfiedDependencies;
	}

	protected void register() {
		String filter = createDependencyFilter();
		if (log.isDebugEnabled()) {
			log.debug(context.getDisplayName() + " has registered service dependency dependencyDetector with filter: "
					+ filter);
		}

		// send dependency event before registering the filter
		sendInitialDependencyEvents();
		OsgiListenerUtils.addServiceListener(bundleContext, listener, filter);
	}

	/**
	 * Look at the existing dependencies and create an appropriate filter. This
	 * method concatenates the filters into one.
	 * 
	 * @return
	 */
	protected String createDependencyFilter() {
		boolean multiple = unsatisfiedDependencies.size() > 1;
		StringBuffer sb = new StringBuffer(100 * unsatisfiedDependencies.size());
		if (multiple) {
			sb.append("(|");
		}
		for (Iterator i = unsatisfiedDependencies.keySet().iterator(); i.hasNext();) {
			sb.append(((MandatoryServiceDependency) i.next()).filterAsString);
		}
		if (multiple) {
			sb.append(')');
		}
		return sb.toString();
	}

	protected void deregister() {
		if (log.isDebugEnabled()) {
			log.debug("Deregistering service dependency dependencyDetector for " + context.getDisplayName());
		}

		OsgiListenerUtils.removeServiceListener(bundleContext, listener);
	}

	// event notification
	private void sendInitialDependencyEvents() {
		for (Iterator iterator = unsatisfiedDependencies.keySet().iterator(); iterator.hasNext();) {
			MandatoryServiceDependency entry = (MandatoryServiceDependency) iterator.next();
			OsgiServiceDependencyEvent nestedEvent = new OsgiServiceDependencyWaitStartingEvent(context,
				entry.getServiceDependency(), waitTime);
			BootstrappingDependencyEvent dependencyEvent = new BootstrappingDependencyEvent(context,
				context.getBundle(), nestedEvent);
			publishEvent(dependencyEvent);
		}
	}

	private void sendDependencyUnsatisfiedEvent(MandatoryServiceDependency dependency) {
		OsgiServiceDependencyEvent nestedEvent = new OsgiServiceDependencyWaitStartingEvent(context,
			dependency.getServiceDependency(), waitTime);
		BootstrappingDependencyEvent dependencyEvent = new BootstrappingDependencyEvent(context, context.getBundle(),
			nestedEvent);
		publishEvent(dependencyEvent);
	}

	private void sendDependencySatisfiedEvent(MandatoryServiceDependency dependency) {
		OsgiServiceDependencyEvent nestedEvent = new OsgiServiceDependencyWaitEndedEvent(context,
			dependency.getServiceDependency(), waitTime);
		BootstrappingDependencyEvent dependencyEvent = new BootstrappingDependencyEvent(context, context.getBundle(),
			nestedEvent);
		publishEvent(dependencyEvent);
	}

	private void publishEvent(BootstrappingDependencyEvent dependencyEvent) {
		this.contextStateAccessor.getEventMulticaster().multicastEvent(dependencyEvent);
	}
}
