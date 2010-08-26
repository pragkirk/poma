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

package org.springframework.osgi.test.internal.util;

import java.util.Enumeration;
import java.util.Iterator;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;

import org.springframework.osgi.test.internal.holder.OsgiTestInfoHolder;

/**
 * Utility class for running OSGi-JUnit tests.
 * 
 * @author Costin Leau
 * 
 */
public abstract class TestUtils {

	/**
	 * Clones the test result from a TestResult loaded through a different
	 * classloader.
	 * 
	 * @param source test result loaded through a different classloader
	 * @param destination test result reported to the outside framework
	 * @param test initial test used for bootstrapping the integration framework
	 * @return cloned test result
	 */
	public static TestResult cloneTestResults(OsgiTestInfoHolder source, TestResult destination, Test test) {
		// get errors
		for (Iterator iter = source.getTestErrors().iterator(); iter.hasNext();) {
			destination.addError(test, (Throwable) iter.next());
		}

		// get failures
		// since failures are a special JUnit error, we have to clone the stack
		for (Iterator iter = source.getTestFailures().iterator(); iter.hasNext();) {
			Throwable originalFailure = (Throwable) iter.next();
			AssertionFailedError clonedFailure = new AssertionFailedError(originalFailure.getMessage());
			clonedFailure.setStackTrace(originalFailure.getStackTrace());
			destination.addFailure(test, clonedFailure);
		}

		return destination;
	}

	/**
	 * Utility method which extracts the information from a TestResult and
	 * stores it as primordial classes. This avoids the use of reflection when
	 * reading the results outside OSGi.
	 * 
	 * @param result
	 * @param holder
	 */
	public static void unpackProblems(TestResult result, OsgiTestInfoHolder holder) {
		Enumeration errors = result.errors();
		while (errors.hasMoreElements()) {
			TestFailure failure = (TestFailure) errors.nextElement();
			holder.addTestError(failure.thrownException());
		}
		Enumeration failures = result.failures();
		while (failures.hasMoreElements()) {
			TestFailure failure = (TestFailure) failures.nextElement();
			holder.addTestFailure(failure.thrownException());
		}
	}
}
