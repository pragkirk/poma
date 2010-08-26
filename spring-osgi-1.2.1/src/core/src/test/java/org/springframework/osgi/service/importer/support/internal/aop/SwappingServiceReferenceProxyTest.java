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

package org.springframework.osgi.service.importer.support.internal.aop;

import java.util.Arrays;
import java.util.Properties;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.service.importer.support.internal.aop.SwappingServiceReferenceProxy;

/**
 * 
 * @author Costin Leau
 * 
 */
public class SwappingServiceReferenceProxyTest extends TestCase {

	private SwappingServiceReferenceProxy reference;
	private ServiceReference serviceReference;


	protected void setUp() throws Exception {
		Properties props = new Properties();
		props.setProperty("composer", "Rachmaninoff");
		reference = new SwappingServiceReferenceProxy();
		serviceReference = new MockServiceReference();
	}

	protected void tearDown() throws Exception {
		reference = null;
		serviceReference = null;
	}

	public void testHashCode() {
		assertTrue(reference.hashCode() != 0);
	}

	public void testGetBundle() {
		assertNull(reference.getBundle());
	}

	public void testGetProperty() {
		assertNull(reference.getProperty("foo"));
	}

	public void testGetPropertyKeys() {
		String[] array = reference.getPropertyKeys();
		assertNotNull(array);
		assertEquals(0, array.length);
	}

	public void testGetUsingBundles() {
		Bundle[] array = reference.getUsingBundles();
		assertNotNull(array);
		assertEquals(0, array.length);
	}

	public void testIsAssignableTo() {
		assertFalse(reference.isAssignableTo(new MockBundle(), "Object"));
	}

	public void testGetTargetServiceReference() {
		assertNull(reference.getTargetServiceReference());
		assertNull(reference.swapDelegates(serviceReference));
		assertSame(serviceReference, reference.getTargetServiceReference());
	}

	public void testEqualsObject() {
		SwappingServiceReferenceProxy anotherRef = new SwappingServiceReferenceProxy();
		assertEquals(anotherRef, reference);
		assertEquals(reference, anotherRef);
		anotherRef.swapDelegates(serviceReference);
		assertFalse(anotherRef.equals(reference));
		assertFalse(reference.equals(anotherRef));
		assertEquals(reference, reference);
	}

	public void testSwapDelegates() {
		int originalHashCode = reference.hashCode();
		assertNull(reference.swapDelegates(serviceReference));
		assertSame(serviceReference, reference.getTargetServiceReference());
		assertFalse(originalHashCode == reference.hashCode());
		assertSame(serviceReference, reference.getTargetServiceReference());
	}

	public void testHashCodeWithNotNullDelegate() {
		int originalHashCode = reference.hashCode();
		reference.swapDelegates(serviceReference);
		assertFalse(originalHashCode == reference.hashCode());
		assertEquals(reference.hashCode(), reference.hashCode());
	}

	public void testGetBundleWithNotNullDelegate() {
		reference.swapDelegates(serviceReference);
		assertSame(serviceReference.getBundle(), reference.getBundle());
	}

	public void testGetPropertyWithNotNullDelegate() {
		reference.swapDelegates(serviceReference);
		assertSame(serviceReference.getProperty("composer"), reference.getProperty("composer"));
		assertSame(serviceReference.getProperty("foo"), reference.getProperty("foo"));
	}

	public void testGetPropertyKeysWithNotNullDelegate() {
		reference.swapDelegates(serviceReference);
		assertTrue(Arrays.equals(serviceReference.getPropertyKeys(), reference.getPropertyKeys()));
	}

	public void testGetUsingBundlesWithNotNullDelegate() {
		reference.swapDelegates(serviceReference);
		assertTrue(Arrays.equals(serviceReference.getUsingBundles(), reference.getUsingBundles()));
	}

	public void testIsAssignableToWithNotNullDelegate() {
		MockBundle bundle = new MockBundle();
		String className = "Object";
		reference.swapDelegates(serviceReference);
		assertEquals(serviceReference.isAssignableTo(bundle, className), reference.isAssignableTo(bundle, className));
	}

	public void testEqualsObjectWithNotNullDelegate() {
		reference.swapDelegates(serviceReference);
		SwappingServiceReferenceProxy anotherRef = new SwappingServiceReferenceProxy();
		assertFalse(anotherRef.equals(reference));
		assertFalse(reference.equals(anotherRef));
		assertEquals(reference, reference);
		anotherRef.swapDelegates(serviceReference);
		assertEquals(anotherRef, reference);
		assertEquals(reference, anotherRef);
		assertEquals(reference, reference);
	}

	public void testCompareToItself() throws Exception {
		assertEquals(0, reference.compareTo(reference));
	}

	public void testDefaultCompareTo() throws Exception {
		assertEquals(0, reference.compareTo(new SwappingServiceReferenceProxy()));
	}

	public void testCompareToDifferentService() throws Exception {
		SwappingServiceReferenceProxy proxy = new SwappingServiceReferenceProxy();
		proxy.swapDelegates(new MockServiceReference());
		try {
			reference.compareTo(proxy);
			fail("expected CCE");
		}
		catch (Exception ex) {

		}
	}
}
