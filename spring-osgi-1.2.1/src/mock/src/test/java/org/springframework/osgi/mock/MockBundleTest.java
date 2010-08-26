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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

/**
 * @author Costin Leau
 * 
 */
public class MockBundleTest extends TestCase {

	MockBundle mock;


	protected void setUp() throws Exception {
		mock = new MockBundle();
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#MockBundle()}.
	 */
	public void testMockBundle() {
		assertNotNull(mock.getLocation());
		assertNotNull(mock.getHeaders());
		assertNotNull(mock.getSymbolicName());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#MockBundle(java.util.Dictionary)}.
	 */
	public void testMockBundleDictionary() {
		Dictionary headers = new Hashtable();

		mock = new MockBundle(headers);

		assertNotNull(mock.getLocation());
		assertSame(headers, mock.getHeaders());
		assertNotNull(mock.getSymbolicName());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#MockBundle(java.lang.String)}.
	 */
	public void testMockBundleString() {
		String location = "some location";
		String symName = "symName";
		mock = new MockBundle(symName);
		((MockBundle) mock).setLocation(location);

		assertSame(location, mock.getLocation());
		assertNotNull(mock.getHeaders());
		assertEquals(symName, mock.getSymbolicName());
		assertEquals(location, mock.getLocation());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#MockBundle(org.osgi.framework.BundleContext)}.
	 */
	public void testMockBundleBundleContext() {
		BundleContext context = new MockBundleContext();

		mock = new MockBundle(context);
		assertNotNull(mock.getLocation());
		assertNotNull(mock.getHeaders());
		assertNotNull(mock.getSymbolicName());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#MockBundle(java.lang.String, java.util.Dictionary, org.osgi.framework.BundleContext)}.
	 */
	public void testMockBundleStringDictionaryBundleContext() {
		BundleContext context = new MockBundleContext();
		String location = "some location";
		Dictionary headers = new Hashtable();

		mock = new MockBundle(location, headers, context);

		assertNotNull(mock.getLocation());
		assertNotNull(mock.getHeaders());
		assertNotNull(mock.getSymbolicName());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#findEntries(java.lang.String, java.lang.String, boolean)}.
	 */
	public void testFindEntries() {
		Enumeration enm = mock.findEntries("", null, true);
		assertNotNull(enm);
		assertFalse(enm.hasMoreElements());

		try {
			enm.nextElement();
			fail("expected exception");
		}
		catch (NoSuchElementException ex) {
			// expected
		}

	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getBundleId()}.
	 */
	public void testGetBundleId() {
		assertTrue(mock.getBundleId() != 0);
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getEntry(java.lang.String)}.
	 */
	public void testGetEntry() throws Exception {
		assertNotNull(mock.getEntry(MockBundleTest.class.getPackage().getName().replace('.', '/')
				+ "/MockBundleTest.class"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getEntryPaths(java.lang.String)}.
	 */
	public void testGetEntryPaths() {
		assertNotNull(mock.getEntryPaths(null));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getHeaders()}.
	 */
	public void testGetHeaders() {
		assertNotNull(mock.getHeaders());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getHeaders(java.lang.String)}.
	 */
	public void testGetHeadersString() {
		assertNotNull(mock.getHeaders("GB"));
		assertSame(mock.getHeaders(), mock.getHeaders("RO"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getLastModified()}.
	 */
	public void testGetLastModified() {
		assertEquals(0, mock.getLastModified());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getLocation()}.
	 */
	public void testGetLocation() {
		assertNotNull(mock.getLocation());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getRegisteredServices()}.
	 */
	public void testGetRegisteredServices() {
		assertNotNull(mock.getRegisteredServices());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getResource(java.lang.String)}.
	 */
	public void testGetResource() {
		assertNotNull(mock.getResource(MockBundleTest.class.getPackage().getName().replace('.', '/')
				+ "/MockBundleTest.class"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getResources(java.lang.String)}.
	 */
	public void testGetResources() throws Exception {
		assertNotNull(mock.getResources(MockBundleTest.class.getPackage().getName().replace('.', '/')));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getServicesInUse()}.
	 */
	public void testGetServicesInUse() {
		assertNotNull(mock.getServicesInUse());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getState()}.
	 */
	public void testGetState() {
		assertEquals(Bundle.ACTIVE, mock.getState());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#getSymbolicName()}.
	 */
	public void testGetSymbolicName() {
		assertNotNull(mock.getSymbolicName());

		Dictionary headers = new Hashtable();
		String symbolic = "symbolicName";
		headers.put(Constants.BUNDLE_SYMBOLICNAME, symbolic);
		mock = new MockBundle(headers);

		assertSame(mock.getSymbolicName(), symbolic);
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#hasPermission(java.lang.Object)}.
	 */
	public void testHasPermission() {
		assertEquals(true, mock.hasPermission(new Object()));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockBundle#loadClass(java.lang.String)}.
	 */
	public void testLoadClass() throws Exception {
		assertSame(getClass(), mock.loadClass(MockBundleTest.class.getName()));
	}

	public void testGetEmptyEnumerator() {
		Enumeration enm = mock.getEntryPaths("bla");
		assertFalse(enm.hasMoreElements());

		try {
			enm.nextElement();
			fail("should have thrown exception");
		}
		catch (NoSuchElementException nsee) {
			// expected
		}
	}

	public void testDefaultStart() throws Exception {
		mock = new MockBundle() {

			public void start(int options) throws BundleException {
				assertEquals(0, options);
			}
		};
		mock.start();
	}

	public void testStartWithOptions() throws Exception {
		mock = new MockBundle() {

			public void start(int options) throws BundleException {
				assertEquals(3, options);
			}
		};

		mock.start(3);
	}

	public void testDefaultStop() throws Exception {
		mock = new MockBundle() {

			public void stop(int options) throws BundleException {
				assertEquals(0, options);
			}
		};
		mock.stop();
	}

	public void testStopWithOptions() throws Exception {
		mock = new MockBundle() {

			public void stop(int options) throws BundleException {
				assertEquals(3, options);
			}
		};
		mock.stop(3);
	}

	public void testDefaultGetBundleContext() throws Exception {
		assertNotNull(mock.getBundleContext());
	}

	public void testBundleContextSpecified() throws Exception {
		BundleContext ctx = new MockBundleContext();
		mock = new MockBundle(ctx);
		assertSame(ctx, mock.getBundleContext());
	}
}
