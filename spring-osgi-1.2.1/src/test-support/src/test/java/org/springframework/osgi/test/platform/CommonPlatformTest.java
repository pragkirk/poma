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

package org.springframework.osgi.test.platform;

import org.osgi.framework.BundleContext;

import junit.framework.TestCase;

/**
 * @author Costin Leau
 * 
 */
public abstract class CommonPlatformTest extends TestCase {

	private AbstractOsgiPlatform platform;


	protected void setUp() throws Exception {
		platform = createPlatform();
	}

	protected void tearDown() throws Exception {
		platform.stop();
		platform = null;
	}

	abstract AbstractOsgiPlatform createPlatform();

	/**
	 * Test method for
	 * {@link org.springframework.osgi.test.platform.FelixPlatform#getPlatformProperties()}.
	 */
	public void testGetPlatformProperties() {
		assertNotNull(platform.getPlatformProperties());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.test.platform.FelixPlatform#start()}.
	 */
	public void testStart() throws Exception {
		assertNull(platform.getBundleContext());
		platform.start();
		assertNotNull(platform.getBundleContext());
	}

	public void testMultipleStart() throws Exception {
		platform.start();
		BundleContext ctx = platform.getBundleContext();
		platform.start();
		assertSame(ctx, platform.getBundleContext());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.test.platform.FelixPlatform#stop()}.
	 */
	public void testStop() throws Exception {
		assertNull(platform.getBundleContext());
		platform.start();
		assertNotNull(platform.getBundleContext());
		platform.stop();
		assertNull(platform.getBundleContext());
	}

	public void testMultipleStop() throws Exception {
		platform.start();
		assertNotNull(platform.getBundleContext());
		platform.stop();
		assertNull(platform.getBundleContext());
		platform.stop();
		assertNull(platform.getBundleContext());
	}
}