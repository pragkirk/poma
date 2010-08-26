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

package org.springframework.osgi.web.extender.internal.activator;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.Version;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.CollectionFactory;
import org.springframework.core.ConcurrentMap;
import org.springframework.osgi.extender.internal.util.concurrent.Counter;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.osgi.web.deployer.ContextPathStrategy;
import org.springframework.osgi.web.deployer.OsgiWarDeploymentException;
import org.springframework.osgi.web.deployer.WarDeployer;
import org.springframework.osgi.web.deployer.WarDeployment;
import org.springframework.osgi.web.extender.internal.scanner.WarScanner;
import org.springframework.scheduling.timer.TimerTaskExecutor;
import org.springframework.util.Assert;

/**
 * OSGi specific listener that bootstraps web applications packed as WARs (Web ARchives). Additionally, it makes the
 * BundleContext available in the ServletContext so that various components can look it up.
 * 
 * @author Costin Leau
 * 
 */
public class WarLoaderListener implements BundleActivator {

	/**
	 * Bundle listener monitoring war-bundles that are being started/stopped.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private class WarBundleListener implements SynchronousBundleListener {

		public void bundleChanged(BundleEvent event) {
			Bundle bundle = event.getBundle();
			boolean trace = log.isTraceEnabled();

			switch (event.getType()) {
			case BundleEvent.STARTED: {
				if (trace)
					log.trace("Processing " + OsgiStringUtils.nullSafeToString(event) + " event for bundle "
							+ OsgiStringUtils.nullSafeNameAndSymName(bundle));

				maybeDeployWar(bundle);

				break;
			}
			case BundleEvent.STOPPING: {
				if (trace)
					log.trace("Processing " + OsgiStringUtils.nullSafeToString(event) + " event for bundle "
							+ OsgiStringUtils.nullSafeNameAndSymName(bundle));

				maybeUndeployWar(bundle);

				break;
			}
			default:
				break;
			}
		}
	}

	/**
	 * Simple WAR deployment manager. Handles the IO process involved in deploying and undeploying the war.
	 */
	private class DeploymentManager implements DisposableBean {

		/** timeout for any possible on going tasks */
		/** default of 1 min */
		private static final long SHUTDOWN_WAIT_TIME = 1000 * 60;

		/** association map between a bundle and its web deployment object */
		private final ConcurrentMap bundlesToDeployments = CollectionFactory.createConcurrentMap(16);

		/** thread for deploying/undeploying bundles */
		private final TimerTaskExecutor executor = new TimerTaskExecutor();
		/** on-going task monitor */
		final Counter onGoingTask = new Counter("ongoing-task");

		public DeploymentManager() {
			executor.afterPropertiesSet();
		}

		public void deployBundle(Bundle bundle, String contextPath) {
			executor.execute(new DeployTask(bundle, contextPath, onGoingTask));
		}

		public void undeployBundle(Bundle bundle) {
			executor.execute(new UndeployTask(bundle, onGoingTask));
		}

		public void destroy() throws Exception {
			// cancel any pending tasks
			executor.destroy();

			final WarListenerConfiguration localConfig;

			synchronized (lock) {
				localConfig = configuration;
			}

			// the config might not be initialized (if the server hasn't been resolved)
			if (localConfig != null) {
				// depending on the configuration, the bundles can be undeployed as well
				if (localConfig.shouldUndeployWarsAtShutdown()) {

					final List bundles = new ArrayList(bundlesToDeployments.keySet());
					StringBuffer bundlesToString = new StringBuffer("\n");
					for (Iterator iterator = bundles.iterator(); iterator.hasNext();) {
						Bundle bundle = (Bundle) iterator.next();
						bundlesToString.append("[ ");
						bundlesToString.append(OsgiStringUtils.nullSafeNameAndSymName(bundle));
						bundlesToString.append(" ]\n");
					}

					// get a snapshot of the keys and maintain the order
					log.info("Undeploying all deployed bundle wars: {" + bundlesToString.toString() + "}");

					Runnable undeployBundlesRunnable = new Runnable() {

						public void run() {
							for (Iterator iterator = bundles.iterator(); iterator.hasNext();) {
								Bundle bundle = (Bundle) iterator.next();
								// undeploy the bundle
								new UndeployTask(bundle, onGoingTask).run();
							}

							// when finished, clear the map just in case
							bundlesToDeployments.clear();

							// only after everything is done, the configuration can be destroyed as well
							// as it holds a reference to the Tomcat service
							localConfig.destroy();
						}
					};

					Thread thread = new Thread(undeployBundlesRunnable, "Spring-DM WebExtender[" + extenderVersion
							+ "] war undeployment thread");
					thread.start();
				} else {
					// if there's a task currently on going, wait for it
					if (onGoingTask.waitForZero(SHUTDOWN_WAIT_TIME)) {
						log.debug("An on-going deploy/undeploy task did not finish in time; continuing shutdown...");
					}
					localConfig.destroy();
				}
			}
		}

		private abstract class BaseTask implements Runnable {

			/** bundle to deploy */
			final Bundle bundle;
			/** bundle name */
			final String bundleName;
			/** work monitor */
			final Counter counter;

			/**
			 * Constructs a new <code>BaseTask</code> instance.
			 * 
			 * @param bundle
			 * @param bundleName
			 * @param counter
			 */
			public BaseTask(Bundle bundle, Counter counter) {
				this.bundle = bundle;
				this.bundleName = OsgiStringUtils.nullSafeNameAndSymName(bundle);
				this.counter = counter;
			}

			/**
			 * {@inheritDoc}
			 * 
			 * Add counter to prevent shutting down while tasks are still running.
			 */
			public final void run() {
				counter.increment();
				boolean trace = log.isTraceEnabled();

				if (trace)
					log.trace("Incrementing work counter for " + toString());

				try {
					doRun();
				} finally {
					counter.decrement();
					if (trace)
						log.trace("Decrementing work counter for " + toString());
				}
			}

			/**
			 * Abstract method meant for subclasses.
			 */
			protected abstract void doRun();
		}

		/** deploy war task */
		private class DeployTask extends BaseTask {

			private final String contextPath;

			public DeployTask(Bundle bundle, String contextPath, Counter counter) {
				super(bundle, counter);
				this.contextPath = contextPath;
			}

			public void doRun() {
				try {
					synchronized (lock) {
						// check if the bundle has been stopped in the meantime
						if (destroyed) {
							return;
						}
					}

					// check if the bundle has been already deployed
					if (bundlesToDeployments.containsKey(bundle)) {
						if (log.isDebugEnabled()) {
							log.debug("Bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle)
									+ " is already deployed; ignoring...");
						}
						return;
					}

					WarDeployment deployment = warDeployer.deploy(bundle, contextPath);
					// deploy the bundle
					bundlesToDeployments.put(bundle, deployment);
				} catch (OsgiWarDeploymentException ex) {
					// log exception
					log.error("War deployment of bundle " + bundleName + " failed", ex);
				}
			}
		}

		/** undeploy war task */
		private class UndeployTask extends BaseTask {

			public UndeployTask(Bundle bundle, Counter counter) {
				super(bundle, counter);
			}

			public void doRun() {
				WarDeployment deployment = (WarDeployment) bundlesToDeployments.remove(bundle);
				// double check that we do have a deployment
				if (deployment != null)
					try {
						deployment.undeploy();
					} catch (OsgiWarDeploymentException ex) {
						// log exception
						log.error("War undeployment of bundle " + bundleName + " failed", ex);
					}
			}
		}
	}

	/** logger */
	private static final Log log = LogFactory.getLog(WarLoaderListener.class);

	/** OSGi bundle context */
	private BundleContext bundleContext;

	/** Extender version */
	private Version extenderVersion;

	/** extender bundle id */
	private long bundleId;

	/** map used for tracking bundles deployed as wars */
	private final ConcurrentMap managedBundles;

	/**
	 * Bundle listener for WARs.
	 */
	private SynchronousBundleListener warListener;

	/** war scanner */
	private WarScanner warScanner;

	/** war deployer */
	private WarDeployer warDeployer;

	/** contextPath strategy */
	private ContextPathStrategy contextPathStrategy;

	private final DeploymentManager deploymentManager;

	private WarListenerConfiguration configuration;

	/** lock used for reading non-final fields between multiple threads */
	/** transient would be nice to have ... */
	private final Object lock = new Object();
	/** flag indicated whether the extender has been stopped */
	private boolean destroyed = false;

	/**
	 * Constructs a new <code>WarLoaderListener</code> instance.
	 * 
	 */
	public WarLoaderListener() {
		this.managedBundles = CollectionFactory.createConcurrentMap(16);
		deploymentManager = new DeploymentManager();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Bootstrapping procedure. Monitors deployed bundles and will scan for WARs known locations. Once such a war is
	 * detected, the web application will be deployed through a specific web deployer.
	 */
	public void start(final BundleContext context) throws Exception {
		synchronized (lock) {
			this.bundleContext = context;
			this.bundleId = bundleContext.getBundle().getBundleId();
			this.extenderVersion = OsgiBundleUtils.getBundleVersion(context.getBundle());
			this.destroyed = false;
		}

		final boolean trace = log.isTraceEnabled();

		log.info("Starting [" + bundleContext.getBundle().getSymbolicName() + "] bundle v.[" + extenderVersion + "]");

		// start the initialization on a different thread
		// this helps if the web server is deployed after the extender (due to the service lookup wait)
		Thread th = new Thread(new Runnable() {

			public void run() {
				try {
					// read configuration
					WarListenerConfiguration config = new WarListenerConfiguration(bundleContext);
					synchronized (lock) {
						// check whether the extender has been stopped in the meantime
						if (!destroyed) {
							configuration = config;
							// instantiate fields
							warScanner = configuration.getWarScanner();
							warDeployer = configuration.getWarDeployer();
							contextPathStrategy = configuration.getContextPathStrategy();

							// register war listener
							warListener = new WarBundleListener();
							context.addBundleListener(warListener);
						} else {
							// clean up the configuration
							config.destroy();
						}
					}

					// check existing bundles
					Bundle[] bnds = context.getBundles();

					for (int i = 0; i < bnds.length; i++) {
						Bundle bundle = bnds[i];
						if (OsgiBundleUtils.isBundleActive(bundle)) {
							if (trace)
								log.trace("Checking if bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle)
										+ " is a war..");
							maybeDeployWar(bundle);
						}
					}
				} catch (Exception ex) {
					log.error("Cannot property start Spring DM WebExtender; stopping bundle...", ex);
					try {
						context.getBundle().stop();
					} catch (Exception excep) {
						log.debug("Stopping of the extender bundle failed", excep);
					}
				}
			}
		}, "WebExtender-Init");

		th.start();
	}

	/**
	 * Checks if the given bundle is a war - if it is, deploy it.
	 * 
	 * @param bundle
	 */
	private void maybeDeployWar(Bundle bundle) {
		synchronized (lock) {
			// first check if the bundle has been stopped or not
			if (destroyed)
				return;

			// exclude special bundles (such as the framework or this bundle)
			if (OsgiBundleUtils.isSystemBundle(bundle) || bundle.getBundleId() == bundleId)
				return;
		}

		WarScanner localWarScanner;
		ContextPathStrategy localCPS;

		synchronized (lock) {
			localWarScanner = warScanner;
			localCPS = contextPathStrategy;
		}

		// check if the bundle is a war
		if (localWarScanner.isWar(bundle)) {
			// get bundle name
			String contextPath = localCPS.getContextPath(bundle);
			// make sure it doesn't contain spaces (causes subtle problems with Tomcat Jasper)
			Assert.doesNotContain(contextPath, " ", "context path should not contain whitespaces");
			String msg = OsgiStringUtils.nullSafeNameAndSymName(bundle)
					+ " is a WAR, scheduling war deployment on context path [" + contextPath + "] (";

			URL webXML = getWebXml(bundle);

			if (webXML != null) {
				msg = msg + "web.xml found at [" + webXML + "])";
			} else
				msg = msg + "no web.xml detected)";

			log.info(msg);

			// mark the bundle as managed
			managedBundles.put(bundle, contextPath);
			deploymentManager.deployBundle(bundle, contextPath);
		}
	}

	private URL getWebXml(Bundle bundle) {
		Enumeration enm = bundle.findEntries("WEB-INF/", "web.xml", false);
		if (enm != null && enm.hasMoreElements())
			return (URL) enm.nextElement();

		return null;
	}

	private void maybeUndeployWar(Bundle bundle) {
		boolean debug = log.isDebugEnabled();

		// do a fast look-up to see if the bundle has already been deployed
		// if it has, then undeploy it
		String contextPath = (String) managedBundles.remove(bundle);
		if (contextPath != null) {
			log.info(OsgiStringUtils.nullSafeNameAndSymName(bundle)
					+ " is a WAR, scheduling war undeployment with context path [" + contextPath + "]");

			deploymentManager.undeployBundle(bundle);
		}
	}

	public void stop(BundleContext context) throws Exception {
		// unregister listener
		synchronized (lock) {
			destroyed = true;

			if (warListener != null) {
				context.removeBundleListener(warListener);
				warListener = null;
			}
		}
		// cancel any tasks that have to be processed
		deploymentManager.destroy();
	}
}
