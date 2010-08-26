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
import java.util.Hashtable;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.ServiceReference;

/**
 * @author Costin Leau
 * 
 */
public class MockServiceRegistrationTest extends TestCase {

	MockServiceRegistration mock;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		mock = new MockServiceRegistration();
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockServiceRegistration#MockServiceRegistration()}.
	 */
	public void testMockServiceRegistration() {
		assertNotNull(mock.getReference());
		assertNotNull(mock.getReference().getPropertyKeys());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockServiceRegistration#MockServiceRegistration(java.util.Hashtable)}.
	 */
	public void testMockServiceRegistrationHashtable() {
		Dictionary props = new Hashtable();
		Object value = new Object();
		props.put("foo", value);

		assertNotNull(mock.getReference());
		mock = new MockServiceRegistration(props);
		assertSame(value, mock.getReference().getProperty("foo"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockServiceRegistration#getReference()}.
	 */
	public void testGetReference() {
		assertNotNull(mock.getReference());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockServiceRegistration#setReference(org.osgi.framework.ServiceReference)}.
	 */
	public void testSetReference() {
		ServiceReference ref = new MockServiceReference();
		mock.setReference(ref);
		assertSame(ref, mock.getReference());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockServiceRegistration#setProperties(java.util.Dictionary)}.
	 */
	public void testSetProperties() {
		Dictionary props = new Hashtable();
		Object value = new Object();
		String key = "foo";
		props.put(key, value);

		assertNull(mock.getReference().getProperty(key));
		mock.setProperties(props);
		assertSame(value, mock.getReference().getProperty(key));
		mock.setReference((ServiceReference) MockControl.createNiceControl(ServiceReference.class).getMock());

		try {
			mock.setProperties(props);
			fail("should have thrown exception");
		}
		catch (RuntimeException ex) {
			// expected
		}
	}

	public void testHashCode() {
		MockServiceReference ref = new MockServiceReference();
		mock.setReference(ref);

		MockServiceRegistration other = new MockServiceRegistration();
		other.setReference(ref);

		assertEquals(mock.hashCode(), other.hashCode());

	}

	public void testHashCodeWithDifferentServiceRef() {
		MockServiceRegistration other = new MockServiceRegistration();
		assertFalse(mock.hashCode() == other.hashCode());
	}

	public void testHashCodeSelf() {
		assertEquals(mock.hashCode(), mock.hashCode());

		mock.setReference(new MockServiceReference());
		assertEquals(mock.hashCode(), mock.hashCode());
	}

	public void testEqualsTrue() {
		MockServiceReference ref = new MockServiceReference();
		mock.setReference(ref);

		MockServiceRegistration other = new MockServiceRegistration();
		other.setReference(ref);

		assertEquals(mock, other);
	}

	public void testEqualsFalse() {
		MockServiceRegistration other = new MockServiceRegistration();
		assertFalse(mock.equals(other));
	}

	public void testEqualsThis() {
		assertEquals(mock, mock);
	}
}
