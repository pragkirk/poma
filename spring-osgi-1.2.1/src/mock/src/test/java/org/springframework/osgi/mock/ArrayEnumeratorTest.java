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

import java.util.Enumeration;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

/**
 * @author Costin Leau
 * 
 */
public class ArrayEnumeratorTest extends TestCase {

	private Enumeration enm;

	private Object[] source;

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		source = new Object[] { "A", "B", "C" };
		enm = new ArrayEnumerator(source);
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		enm = null;
		source = null;
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.ArrayEnumerator#hasMoreElements()}.
	 */
	public void testHasMoreElements() {
		assertTrue(enm.hasMoreElements());
	}

	public void testHasMoreElementsWithEmptySource() {
		enm = new ArrayEnumerator(new Object[0]);
		assertFalse(enm.hasMoreElements());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.ArrayEnumerator#nextElement()}.
	 */
	public void testNextElement() {
		assertEquals("A", enm.nextElement());
		assertEquals("B", enm.nextElement());
		assertEquals("C", enm.nextElement());
	}

	public void testNextElementException() {
		enm = new ArrayEnumerator(new Object[0]);
		try {
			enm.nextElement();
			fail("should have thrown exception");
		}
		catch (NoSuchElementException ex) {
			// expected
		}
	}

}
