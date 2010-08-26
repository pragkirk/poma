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

import java.util.Hashtable;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.test.internal.OsgiJUnitTest;
import org.springframework.osgi.test.internal.TestRunnerService;
import org.springframework.osgi.test.internal.holder.HolderLoader;
import org.springframework.osgi.test.internal.holder.OsgiTestInfoHolder;
import org.springframework.osgi.test.internal.support.OsgiJUnitTestAdapter;
import org.springframework.osgi.util.OsgiServiceUtils;

/**
 * Test bundle activator - looks for a predefined JUnit test runner and triggers
 * the test execution. This class is used by the testing framework to run
 * integration tests inside the OSGi framework.
 * 
 * <strong>Note:</strong> Programatic usage of this class is strongly
 * discouraged as its semantics might change in the future - in fact, the only
 * reason this class is public is because the OSGi specification requires this.
 * 
 * @author Costin Leau
 */
public class JUnitTestActivator implements BundleActivator {

	private static final Log log = LogFactory.getLog(JUnitTestActivator.class);

	private BundleContext context;
	private ServiceReference reference;
	private ServiceRegistration registration;
	private TestRunnerService service;


	public void start(BundleContext bc) throws Exception {
		this.context = bc;

		reference = context.getServiceReference(TestRunnerService.class.getName());
		if (reference == null)
			throw new IllegalArgumentException("cannot find service at " + TestRunnerService.class.getName());
		service = (TestRunnerService) context.getService(reference);

		registration = context.registerService(JUnitTestActivator.class.getName(), this, new Hashtable());

	}

	/**
	 * Starts executing an instance of OSGiJUnitTest on the TestRunnerService.
	 */
	void executeTest() {
		service.runTest(loadTest());
	}

	/**
	 * Loads the test instance inside OSGi and prepares it for execution.
	 * 
	 * @return
	 */
	private OsgiJUnitTest loadTest() {
		OsgiTestInfoHolder holder = HolderLoader.INSTANCE.getHolder();
		String testClass = holder.getTestClassName();
		if (testClass == null)
			throw new IllegalArgumentException("no test class specified");

		try {
			// use bundle to load the classes
			Class clazz = context.getBundle().loadClass(testClass);
			TestCase test = (TestCase) clazz.newInstance();
			// wrap the test with the OsgiJUnitTestAdapter
			OsgiJUnitTest osgiTest = new OsgiJUnitTestAdapter(test);
			osgiTest.injectBundleContext(context);
			return osgiTest;

		}
		catch (Exception ex) {
			log.error("failed to invoke test execution", ex);
			throw new RuntimeException(ex);
		}
	}

	public void stop(BundleContext bc) throws Exception {
		OsgiServiceUtils.unregisterService(registration);
	}

}
