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

package org.springframework.osgi.test;

import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.springframework.osgi.extender.internal.util.concurrent.Counter;
import org.springframework.osgi.extender.support.internal.ConfigUtils;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiListenerUtils;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.ObjectUtils;

/**
 * JUnit superclass which offers synchronization for application context
 * initialization. The class <b>automatically</b> determines
 * <em>Spring powered</em> bundles that are installed by the testing framework
 * and (by default), will wait for their application context to fully start.
 * Only after all the application contexts have been fully refreshed, the actual
 * test execution will commence.
 * 
 * <p/>The class also provides utility waiting methods for discovering Spring
 * application context (published as OSGi services) in case programmatic waiting
 * is required (for example, when installing bundles manually).
 * 
 * <p/>As the rest of the other classes, the behaviour of this class can be
 * customized by extending its methods.
 * 
 * @author Costin Leau
 * @author Adrian Colyer
 * 
 */
public abstract class AbstractSynchronizedOsgiTests extends AbstractConfigurableOsgiTests {

	protected static final long DEFAULT_WAIT_TIME = 60L;

	private static final long SECOND = 1000;


	/**
	 * 
	 * Default constructor. Constructs a new
	 * <code>AbstractSynchronizedOsgiTests</code> instance.
	 * 
	 */
	public AbstractSynchronizedOsgiTests() {
		super();
	}

	/**
	 * Constructs a new <code>AbstractSynchronizedOsgiTests</code> instance.
	 * 
	 * @param name test name
	 */
	public AbstractSynchronizedOsgiTests(String name) {
		super(name);
	}

	/**
	 * Waits for a <em>Spring powered</em> bundle, given by its symbolic name
	 * to be fully started.
	 * 
	 * <p/>Forces the current (test) thread to wait for the a Spring application
	 * context to be published under the given symbolic name. This method allows
	 * waiting for full initialization of Spring OSGi bundles before starting
	 * the actual test execution. This method will use the test bundle context
	 * for service lookup.
	 * 
	 * @param forBundleWithSymbolicName bundle symbolic name
	 * @param timeout maximum time to wait (in seconds) for the application
	 * context to be published
	 */
	protected void waitOnContextCreation(String forBundleWithSymbolicName, long timeout) {
		waitOnContextCreation(bundleContext, forBundleWithSymbolicName, timeout);

	}

	/**
	 * Waits for a <em>Spring powered</em> bundle, given by its symbolic name,
	 * to be fully started.
	 * 
	 * <p/>Forces the current (test) thread to wait for the a Spring application
	 * context to be published under the given symbolic name. This method allows
	 * waiting for full initialization of Spring OSGi bundles before starting
	 * the actual test execution.
	 * 
	 * @param context bundle context to use for service lookup
	 * @param forBundleWithSymbolicName bundle symbolic name
	 * @param timeout maximum time to wait (in seconds) for the application
	 * context to be published
	 */
	protected void waitOnContextCreation(BundleContext context, String forBundleWithSymbolicName, long timeout) {
		// translate from seconds to milliseconds
		long time = timeout * SECOND;

		// use the counter to make sure the threads block
		final Counter counter = new Counter("waitForContext on bnd=" + forBundleWithSymbolicName);

		counter.increment();

		String filter = "(org.springframework.context.service.name=" + forBundleWithSymbolicName + ")";

		ServiceListener listener = new ServiceListener() {

			public void serviceChanged(ServiceEvent event) {
				if (event.getType() == ServiceEvent.REGISTERED)
					counter.decrement();
			}
		};

		OsgiListenerUtils.addServiceListener(context, listener, filter);

		if (logger.isDebugEnabled())
			logger.debug("Start waiting for Spring/OSGi bundle=" + forBundleWithSymbolicName);

		try {
			if (counter.waitForZero(time)) {
				waitingFailed(forBundleWithSymbolicName);
			}
			else if (logger.isDebugEnabled()) {
				logger.debug("Found applicationContext for bundle=" + forBundleWithSymbolicName);
			}
		}
		finally {
			// inform waiting thread
			context.removeServiceListener(listener);
		}
	}

	/**
	 * Waits for a <em>Spring powered</em> bundle, given by its symbolic name,
	 * to be fully started.
	 * 
	 * <p/>This method uses the default wait time and test bundle context and is
	 * identical to #waitOnContextCreation(bundleContext,
	 * forBundleWithSymbolicName, {@link #getDefaultWaitTime()}).
	 * 
	 * <p/>This method is used by the testing framework at startup before
	 * executing the actual tests.
	 * 
	 * @param forBundleWithSymbolicName bundle symbolic name
	 * @see #getDefaultWaitTime()
	 * @see #waitOnContextCreation(BundleContext, String, long)
	 */
	protected void waitOnContextCreation(String forBundleWithSymbolicName) {
		waitOnContextCreation(forBundleWithSymbolicName, getDefaultWaitTime());
	}

	private void waitingFailed(String bundleName) {
		logger.warn("Waiting for applicationContext for bundle=" + bundleName + " timed out");

		throw new RuntimeException("Gave up waiting for application context for '" + bundleName + "' to be created");
	}

	/**
	 * Returns the test default waiting time (in seconds). Subclasses should
	 * override this method if the {@link #DEFAULT_WAIT_TIME} is not enough. For
	 * more customization, consider setting
	 * {@link #shouldWaitForSpringBundlesContextCreation()} to false and using
	 * {@link #waitOnContextCreation(BundleContext, String, long)}.
	 * 
	 * @return the default wait time (in seconds) for each spring bundle context
	 * to be published as an OSGi service
	 */
	protected long getDefaultWaitTime() {
		return DEFAULT_WAIT_TIME;
	}

	/**
	 * Indicates whether the test class should wait or not for the context
	 * creation of Spring/OSGi bundles before executing the tests. Default is
	 * true.
	 * 
	 * @return true (the default) if the test will wait for spring bundle
	 * context creation or false otherwise
	 */
	protected boolean shouldWaitForSpringBundlesContextCreation() {
		return true;
	}

	/*
	 * Takes care of automatically waiting for the application context creation
	 * of <em>Spring powered</em> bundles.
	 */
	protected void postProcessBundleContext(BundleContext platformBundleContext) throws Exception {
		if (shouldWaitForSpringBundlesContextCreation()) {
			boolean debug = logger.isDebugEnabled();
			boolean trace = logger.isTraceEnabled();
			if (debug)
				logger.debug("Looking for Spring/OSGi powered bundles to wait for...");

			// determine Spring/OSGi bundles
			Bundle[] bundles = platformBundleContext.getBundles();
			for (int i = 0; i < bundles.length; i++) {
				Bundle bundle = bundles[i];
				String bundleName = OsgiStringUtils.nullSafeSymbolicName(bundle);
				if (OsgiBundleUtils.isBundleActive(bundle)) {
					if (isSpringDMManaged(bundle) && ConfigUtils.getPublishContext(bundle.getHeaders())) {
						if (debug)
							logger.debug("Bundle [" + bundleName + "] triggers a context creation; waiting for it");
						// use platformBundleContext
						waitOnContextCreation(platformBundleContext, bundleName, getDefaultWaitTime());
					}
					else if (trace)
						logger.trace("Bundle [" + bundleName + "] does not trigger a context creation.");
				}
				else {
					if (trace)
						logger.trace("Bundle [" + bundleName + "] is not active (probably a fragment); ignoring");
				}
			}
		}
	}

	/**
	 * Determines if the given bundle, is Spring DM managed or not. This method
	 * is used at startup, for waiting on all Spring DM contexts to be properly
	 * started and published.
	 * 
	 * @param bundle
	 * @return
	 */
	protected boolean isSpringDMManaged(Bundle bundle) {
		if (!ObjectUtils.isEmpty(ConfigUtils.getHeaderLocations(bundle.getHeaders())))
			return true;
		Enumeration enm = bundle.findEntries("META-INF/spring", "*.xml", false);
		return (enm != null && enm.hasMoreElements());
	}
}
