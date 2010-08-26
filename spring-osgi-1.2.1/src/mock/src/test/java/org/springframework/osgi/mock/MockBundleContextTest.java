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

import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

/**
 * @author Costin Leau
 * 
 */
public class MockBundleContextTest extends TestCase {

	MockBundleContext mock;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		mock = new MockBundleContext();
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#MockBundleContext()}.
	 */
	public void testMockBundleContext() {
		assertNotNull(mock.getBundle());
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_VENDOR));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#MockBundleContext(org.osgi.framework.Bundle)}.
	 */
	public void testMockBundleContextBundle() {
		Bundle bundle = new MockBundle();
		mock = new MockBundleContext(bundle);

		assertSame(bundle, mock.getBundle());
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_VENDOR));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#MockBundleContext(org.osgi.framework.Bundle, java.util.Properties)}.
	 */
	public void testMockBundleContextBundleProperties() {
		Bundle bundle = new MockBundle();
		Properties props = new Properties();

		mock = new MockBundleContext(bundle, props);
		assertSame(bundle, mock.getBundle());
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_VENDOR));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#createFilter(java.lang.String)}.
	 */
	public void testCreateFilter() throws Exception {
		assertNotNull(mock.createFilter("foo-bar"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#getAllServiceReferences(java.lang.String, java.lang.String)}.
	 */
	public void testGetAllServiceReferences() throws Exception {
		assertNotNull(mock.getAllServiceReferences(getClass().getName(), "*"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#getBundle()}.
	 */
	public void testGetBundle() {
		assertNotNull(mock.getBundle());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#getBundle(long)}.
	 */
	public void testGetBundleLong() {
		assertNotNull(mock.getBundle(123));
		assertSame(mock.getBundle(), mock.getBundle(321));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#getBundles()}.
	 */
	public void testGetBundles() {
		assertNotNull(mock.getBundles());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#getDataFile(java.lang.String)}.
	 */
	public void testGetDataFile() {
		assertNull(mock.getDataFile(""));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#getProperty(java.lang.String)}.
	 */
	public void testGetProperty() {
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_VERSION));
		assertNull(mock.getProperty("foo"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#getService(org.osgi.framework.ServiceReference)}.
	 */
	public void testGetService() {
		assertNotNull(mock.getService(new MockServiceReference()));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#getServiceReference(java.lang.String)}.
	 */
	public void testGetServiceReference() throws Exception {
		assertNotNull(mock.getServiceReference(getClass().getName()));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#getServiceReferences(java.lang.String, java.lang.String)}.
	 */
	public void testGetServiceReferences() throws Exception {
		assertNotNull(mock.getServiceReferences(getClass().getName(), "*"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#installBundle(java.lang.String)}.
	 */
	public void testInstallBundleString() throws Exception {
		String location = "location";
		assertSame(location, mock.installBundle(location).getLocation());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#installBundle(java.lang.String, java.io.InputStream)}.
	 */
	public void testInstallBundleStringInputStream() throws Exception {
		String location = "location";
		assertSame(location, mock.installBundle(location, new ByteArrayInputStream(new byte[0])).getLocation());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#registerService(java.lang.String[], java.lang.Object, java.util.Dictionary)}.
	 */
	public void testRegisterServiceStringArrayObjectDictionary() {
		assertNotNull(mock.registerService(new String[] { "foo" }, new Object(), new Hashtable()));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#registerService(java.lang.String, java.lang.Object, java.util.Dictionary)}.
	 */
	public void testRegisterServiceStringObjectDictionary() {
		assertNotNull(mock.registerService("foo", new Object(), new Hashtable()));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundleContext#ungetService(org.osgi.framework.ServiceReference)}.
	 */
	public void testUngetService() {
		assertFalse(mock.ungetService(new MockServiceReference()));
	}

	public void testMandatoryPropertiesAvailable() {

		assertNotNull(mock.getProperty(Constants.FRAMEWORK_VERSION));
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_VENDOR));
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_LANGUAGE));
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_OS_NAME));
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_OS_VERSION));
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_PROCESSOR));
	}

	public void testAddServiceListener() throws Exception {
		ServiceListener listener = new ServiceListener() {
			public void serviceChanged(ServiceEvent event) {
			}
		};
		mock.addServiceListener(listener);

		assertEquals(1, mock.getServiceListeners().size());
		assertSame(listener, mock.getServiceListeners().iterator().next());
	}

	public void testRemoveServiceListener() throws Exception {
		ServiceListener listener = new ServiceListener() {
			public void serviceChanged(ServiceEvent event) {
			}
		};

		Set listeners = mock.getServiceListeners();

		mock.removeServiceListener(null);
		
		assertEquals(0, listeners.size());
		
		mock.removeServiceListener(listener);
		assertEquals(0, listeners.size());
		
		mock.addServiceListener(listener);
		assertEquals(1, listeners.size());
		
		mock.removeServiceListener(listener);
		assertEquals(0, listeners.size());
	}
}
