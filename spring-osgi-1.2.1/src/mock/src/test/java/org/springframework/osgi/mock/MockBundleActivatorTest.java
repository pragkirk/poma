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
package org.springframework.osgi.mock;

import junit.framework.TestCase;

import org.osgi.framework.BundleActivator;

/**
 * @author Costin Leau
 * 
 */
public class MockBundleActivatorTest extends TestCase {

	BundleActivator mock;

	protected void setUp() throws Exception {
		mock = new MockBundleActivator();
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleActivator#start(org.osgi.framework.BundleContext)}.
	 */
	public void testStart() throws Exception {
		mock.start(new MockBundleContext());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleActivator#stop(org.osgi.framework.BundleContext)}.
	 */
	public void testStop() throws Exception {
		mock.stop(new MockBundleContext());
	}

}
