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

package org.springframework.osgi.util;

import java.util.Properties;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockServiceReference;

/**
 * @author Costin Leau
 * 
 */
public class OsgiStringUtilsTest extends TestCase {

	private static int state;

	private Bundle bundle;


	protected void setUp() throws Exception {
		OsgiStringUtilsTest.state = Bundle.UNINSTALLED;
		bundle = new MockBundle() {

			public int getState() {
				return state;
			}
		};
	}

	public void testGetBundleEventAsString() {
		assertEquals("INSTALLED", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.INSTALLED));
		assertEquals("STARTING", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.STARTING));
		assertEquals("UNINSTALLED", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.UNINSTALLED));
		assertEquals("UPDATED", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.UPDATED));
		assertTrue(OsgiStringUtils.nullSafeBundleEventToString(-1324).startsWith("UNKNOWN"));
	}

	public void testGetBundleStateAsName() throws Exception {
		OsgiStringUtilsTest.state = Bundle.ACTIVE;
		assertEquals("ACTIVE", OsgiStringUtils.bundleStateAsString(bundle));
		OsgiStringUtilsTest.state = Bundle.STARTING;
		assertEquals("STARTING", OsgiStringUtils.bundleStateAsString(bundle));
		OsgiStringUtilsTest.state = Bundle.STOPPING;
		assertEquals("STOPPING", OsgiStringUtils.bundleStateAsString(bundle));
		OsgiStringUtilsTest.state = -123;
		assertEquals("UNKNOWN STATE", OsgiStringUtils.bundleStateAsString(bundle));
	}

	public void testNullSafeToStringBundleEvent() throws Exception {
		assertEquals("INSTALLED", OsgiStringUtils.nullSafeToString(new BundleEvent(BundleEvent.INSTALLED, bundle)));
		assertEquals("UPDATED", OsgiStringUtils.nullSafeToString(new BundleEvent(BundleEvent.UPDATED, bundle)));
		assertEquals("STOPPING", OsgiStringUtils.nullSafeToString(new BundleEvent(BundleEvent.STOPPING, bundle)));
	}

	public void testNullSafeToStringBundleEventNull() throws Exception {
		assertNotNull(OsgiStringUtils.nullSafeToString((BundleEvent) null));
	}

	public void testNullSafeToStringBundleEventInvalidType() throws Exception {
		assertEquals("UNKNOWN EVENT TYPE", OsgiStringUtils.nullSafeToString(new BundleEvent(-123, bundle)));
	}

	public void testNullSafeToStringServiceEvent() throws Exception {
		ServiceReference ref = new MockServiceReference();
		assertEquals("REGISTERED", OsgiStringUtils.nullSafeToString(new ServiceEvent(ServiceEvent.REGISTERED, ref)));
		assertEquals("MODIFIED", OsgiStringUtils.nullSafeToString(new ServiceEvent(ServiceEvent.MODIFIED, ref)));
		assertEquals("UNREGISTERING",
			OsgiStringUtils.nullSafeToString(new ServiceEvent(ServiceEvent.UNREGISTERING, ref)));
	}

	public void testNullSafeToStringServiceEventNull() throws Exception {
		assertNotNull(OsgiStringUtils.nullSafeToString((ServiceEvent) null));
	}

	public void testNullSafeToStringServiceEventInvalidType() throws Exception {
		assertEquals("UNKNOWN EVENT TYPE", OsgiStringUtils.nullSafeToString(new ServiceEvent(-123,
			new MockServiceReference())));
	}

	public void testNullSafeToStringFrameworkEvent() throws Exception {
		Bundle bundle = new MockBundle();
		Throwable th = new Exception();
		assertEquals("STARTED",
			OsgiStringUtils.nullSafeToString(new FrameworkEvent(FrameworkEvent.STARTED, bundle, th)));
		assertEquals("ERROR", OsgiStringUtils.nullSafeToString(new FrameworkEvent(FrameworkEvent.ERROR, bundle, th)));

		assertEquals("WARNING",
			OsgiStringUtils.nullSafeToString(new FrameworkEvent(FrameworkEvent.WARNING, bundle, th)));

		assertEquals("INFO", OsgiStringUtils.nullSafeToString(new FrameworkEvent(FrameworkEvent.INFO, bundle, th)));

		assertEquals("PACKAGES_REFRESHED", OsgiStringUtils.nullSafeToString(new FrameworkEvent(
			FrameworkEvent.PACKAGES_REFRESHED, bundle, th)));

		assertEquals("STARTLEVEL_CHANGED", OsgiStringUtils.nullSafeToString(new FrameworkEvent(
			FrameworkEvent.STARTLEVEL_CHANGED, bundle, th)));
	}

	public void testNullSafeToStringFrameworkEventNull() throws Exception {
		assertNotNull(OsgiStringUtils.nullSafeToString((FrameworkEvent) null));
	}

	public void testNullSafeToStringFrameworkEventInvalidType() throws Exception {
		assertEquals("UNKNOWN EVENT TYPE", OsgiStringUtils.nullSafeToString(new FrameworkEvent(-123, bundle,
			new Exception())));
	}

	public void testNullSafeToStringServiceReference() throws Exception {
		String symName = "symName";

		MockBundle bundle = new MockBundle(symName);
		Properties props = new Properties();
		String header = "HEADER";
		String value = "VALUE";
		props.put(header, value);
		MockServiceReference ref = new MockServiceReference(bundle, props, null);
		String out = OsgiStringUtils.nullSafeToString(ref);
		assertTrue(out.indexOf(symName) > -1);
		assertTrue(out.indexOf(header) > -1);
		assertTrue(out.indexOf(value) > -1);
	}

	public void testNullSafeToStringServiceReferenceNull() throws Exception {
		assertNotNull(OsgiStringUtils.nullSafeToString((ServiceReference) null));
	}
}
