/*
 * Copyright 2006 the original author or authors.
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
package org.springframework.osgi.util;

import java.util.Properties;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockBundleContext;

/**
 * @author Adrian Colyer
 *
 */
public class OsgiPlatformDetectorTest extends TestCase {

	private Bundle mockBundle;
	private Properties props;
	
	protected void setUp() throws Exception {
		super.setUp();
		this.mockBundle = new MockBundle();
		this.props = new Properties();
	}
	
	public void testEquinoxDetection() {
		props.put(Constants.FRAMEWORK_VENDOR,"Eclipse");
		BundleContext bc = new MockBundleContext(mockBundle,props);
		assertTrue("Detected as Equinox",OsgiPlatformDetector.isEquinox(bc));
		assertFalse(OsgiPlatformDetector.isKnopflerfish(bc));
		assertFalse(OsgiPlatformDetector.isFelix(bc));
	}
	
	public void testKnopflerfishDetection() {
		props.put(Constants.FRAMEWORK_VENDOR,"Knopflerfish");
		BundleContext bc = new MockBundleContext(mockBundle,props);
		assertTrue("Detected as Knopflerfish",OsgiPlatformDetector.isKnopflerfish(bc));		
		assertFalse(OsgiPlatformDetector.isEquinox(bc));
		assertFalse(OsgiPlatformDetector.isFelix(bc));
	}
	
	public void testFelixDetection() {
		props.put(Constants.FRAMEWORK_VENDOR,"Apache Software Foundation");
		BundleContext bc = new MockBundleContext(mockBundle,props);
		assertTrue("Detected as Felix",OsgiPlatformDetector.isFelix(bc));		
		assertFalse(OsgiPlatformDetector.isEquinox(bc));
		assertFalse(OsgiPlatformDetector.isKnopflerfish(bc));
	}
	
}
