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

package org.springframework.osgi.test.internal.holder;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom class used for storing JUnit test results. To work, this class should
 * always be loaded through the same class loader, to <em>transport</em>
 * information from OSGi to the outside world.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiTestInfoHolder {

	/** JUnit test problems */
	private List testFailures = new ArrayList(4);
	private List testErrors = new ArrayList(4);

	/** test bundle id */
	private Long testBundleId;
	/** test class name */
	private String testClassName;
	/** test method name */
	private String testMethodName;

	/** static instance */
	public static final OsgiTestInfoHolder INSTANCE = new OsgiTestInfoHolder();


	/**
	 * 
	 * Constructs a new <code>OsgiTestInfoHolder</code> instance.
	 */
	public OsgiTestInfoHolder() {
	}

	/**
	 * Returns the testBundleId.
	 * 
	 * @return Returns the testBundleId
	 */
	public Long getTestBundleId() {
		return testBundleId;
	}

	/**
	 * @param testBundleId The testBundleId to set.
	 */
	public void setTestBundleId(Long testBundleId) {
		this.testBundleId = testBundleId;
	}

	/**
	 * Returns the testClassName.
	 * 
	 * @return Returns the testClassName
	 */
	public String getTestClassName() {
		return testClassName;
	}

	/**
	 * @param testClassName The testClassName to set.
	 */
	public void setTestClassName(String testClassName) {
		this.testClassName = testClassName;
	}

	/**
	 * @param testProblem The testResult to set.
	 */
	public void addTestFailure(Throwable testProblem) {
		testFailures.add(testProblem);
	}

	public void addTestError(Throwable testProblem) {
		testErrors.add(testProblem);
	}

	/**
	 * Returns the testMethodName.
	 * 
	 * @return Returns the testMethodName
	 */
	public String getTestMethodName() {
		return testMethodName;
	}

	/**
	 * @param testMethodName The testMethodName to set.
	 */
	public void setTestMethodName(String testMethodName) {
		this.testMethodName = testMethodName;
	}

	public List getTestFailures() {
		return testFailures;
	}

	public List getTestErrors() {
		return testErrors;
	}

	/**
	 * Clear all information. Used between test runs to clear results.
	 */
	public void clearResults() {
		testFailures.clear();
		testErrors.clear();
	}
}
