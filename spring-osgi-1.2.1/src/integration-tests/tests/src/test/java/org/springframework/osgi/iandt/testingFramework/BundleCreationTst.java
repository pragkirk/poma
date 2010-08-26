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
package org.springframework.osgi.iandt.testingFramework;

import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;

/**
 * Test to check if the testcase is properly packaged in a bundle jar and deploy
 * on the OSGi platform.
 * 
 * Note: this test case is not intended to be run (hence the Tst name).
 * 
 * @author Costin Leau
 * 
 */
public class BundleCreationTst extends AbstractConfigurableBundleCreatorTests {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.osgi.test.OsgiTest#getBundles()
	 */
	protected String[] getBundleLocations() {
		// no test bundle is included
		return new String[] {};
	}
	
	public void testAssertionPass() {
		assertTrue(true);
	}

	public void testAssertionFailure() {
		assertTrue(false);
	}

	public void testFailure() {
		fail("this is a failure");
	}

	public void testException() {
		throw new RuntimeException("this is an exception");
	}

	public void testError() {
		throw new Error("this is an error");
	}
}
