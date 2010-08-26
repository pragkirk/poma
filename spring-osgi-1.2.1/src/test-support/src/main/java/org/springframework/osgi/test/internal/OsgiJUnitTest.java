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

package org.springframework.osgi.test.internal;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;

/**
 * JUnit contract for OSGi environments. It wraps some of TestCase methods as
 * well as adds some to allow flexible access to the test instance by the
 * TestRunnerService implementation.
 * 
 * @author Costin Leau
 * 
 */
public interface OsgiJUnitTest {

	/**
	 * Replacement for the 'traditional' setUp. Called by TestRunnerService.
	 * 
	 * @see junit.framework.TestCase#setUp
	 * @throws Exception
	 */
	void osgiSetUp() throws Exception;

	/**
	 * Replacement for the 'traditional' tearDown. Called by TestRunnerService.
	 * 
	 * @see junit.framework.TestCase#tearDown
	 * @throws Exception
	 */
	void osgiTearDown() throws Exception;

	/**
	 * Replacement for the 'traditional' runTest. Called by TestRunnerService.
	 * 
	 * @throws Throwable
	 */
	void osgiRunTest() throws Throwable;

	/**
	 * Provides the OSGi bundle context to the test
	 * 
	 * @param bundleContext
	 */
	void injectBundleContext(BundleContext bundleContext);

	/**
	 * Simple getter that returns the raw TestCase class. Used mainly when
	 * applying OsgiJUnit functionality through decoration rather then
	 * inheritance.
	 * 
	 * @return
	 */
	TestCase getTestCase();
}
