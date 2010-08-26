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

import java.util.Hashtable;

import junit.framework.TestCase;

import org.osgi.framework.Filter;

/**
 * @author Costin Leau
 * 
 */
public class MockFilterTest extends TestCase {

	Filter mock;

	protected void setUp() throws Exception {
		mock = new MockFilter();
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockFilter#match(org.osgi.framework.ServiceReference)}.
	 */
	public void testMatchServiceReference() {
		assertFalse(mock.match(new MockServiceReference()));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockFilter#match(java.util.Dictionary)}.
	 */
	public void testMatchDictionary() {
		assertFalse(mock.match(new Hashtable()));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.mock.MockFilter#matchCase(java.util.Dictionary)}.
	 */
	public void testMatchCase() {
		assertFalse(mock.matchCase(new Hashtable()));
	}

}
