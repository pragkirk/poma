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

package org.springframework.osgi.test.internal.support;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.osgi.test.internal.OsgiJUnitTest;
import org.springframework.osgi.test.internal.TestRunnerService;
import org.springframework.osgi.test.internal.holder.HolderLoader;
import org.springframework.osgi.test.internal.holder.OsgiTestInfoHolder;
import org.springframework.osgi.test.internal.util.TestUtils;

/**
 * OSGi service for executing JUnit tests.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiJUnitService implements TestRunnerService {

	private static final Log log = LogFactory.getLog(OsgiJUnitService.class);


	public void runTest(OsgiJUnitTest test) {
		try {
			executeTest(test);
		}
		catch (Exception ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			throw new RuntimeException("cannot execute test:" + ex, ex);
		}
	}

	/**
	 * Execute the JUnit test and publish results to the outside-OSGi world.
	 * 
	 * @param test
	 * @throws Exception
	 */
	protected void executeTest(OsgiJUnitTest test) throws Exception {
		// create holder
		// since we're inside OSGi, we have to use the special loading procedure
		OsgiTestInfoHolder holder = HolderLoader.INSTANCE.getHolder();

		// read the test to be executed
		String testName = holder.getTestMethodName();
		if (log.isDebugEnabled())
			log.debug("Reading test [" + testName + "] for execution inside OSGi");
		// execute the test
		TestResult result = runTest(test, testName);

		if (log.isDebugEnabled())
			log.debug("Sending test results from OSGi");
		// write result back to the outside world
		TestUtils.unpackProblems(result, holder);
	}

	/**
	 * Run fixture setup, test from the given test case and fixture teardown.
	 * 
	 * @param osgiTestExtensions
	 * @param testName
	 */
	protected TestResult runTest(final OsgiJUnitTest osgiTestExtensions, String testName) {
		if (log.isDebugEnabled())
			log.debug("Running test [" + testName + "] on testCase " + osgiTestExtensions);
		final TestResult result = new TestResult();
		TestCase rawTest = osgiTestExtensions.getTestCase();

		rawTest.setName(testName);

		try {
			osgiTestExtensions.osgiSetUp();

			try {
				// use TestResult method to bypass the setUp/tearDown methods
				result.runProtected(rawTest, new Protectable() {

					public void protect() throws Throwable {
						osgiTestExtensions.osgiRunTest();
					}

				});
			}
			finally {
				osgiTestExtensions.osgiTearDown();
			}

		}
		// exceptions thrown by osgiSetUp/osgiTearDown
		catch (Exception ex) {
			log.error("test exception threw exception ", ex);
			result.addError((Test) rawTest, ex);
		}
		return result;
	}
}