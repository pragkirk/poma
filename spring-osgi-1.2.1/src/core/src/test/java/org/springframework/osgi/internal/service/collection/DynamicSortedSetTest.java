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
package org.springframework.osgi.internal.service.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import junit.framework.TestCase;

import org.springframework.osgi.service.importer.support.internal.collection.DynamicSortedSet;

/**
 * @author Costin Leau
 * 
 */
public class DynamicSortedSetTest extends TestCase {

	private SortedSet dynamicSortedSet;

	private List list;

	private Integer one;

	private Integer two;

	private Integer three;

	protected void setUp() throws Exception {
		dynamicSortedSet = new DynamicSortedSet();

		one = new Integer(1);
		two = new Integer(2);
		three = new Integer(3);

		list = new ArrayList();
		list.add(one);
		list.add(two);
		list.add(three);
		list.add(two);

	}

	protected void tearDown() throws Exception {
		dynamicSortedSet = null;
		list = null;
		one = null;
		two = null;
		three = null;
	}

	public void testAdd() {
		assertTrue(dynamicSortedSet.add(one));
		assertFalse(dynamicSortedSet.add(one));
		assertTrue(dynamicSortedSet.add(three));
		assertFalse(dynamicSortedSet.add(three));
	}

	public void testAddNullForbidden() {
		try {
			dynamicSortedSet.add(null);
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testAddAll() {
		assertTrue(dynamicSortedSet.isEmpty());
		assertTrue(dynamicSortedSet.addAll(list));
		assertEquals(3, dynamicSortedSet.size());
	}

	public void testAddAllOnExistingSet() {
		dynamicSortedSet.add(two);
		assertTrue(dynamicSortedSet.addAll(list));
		assertEquals(3, dynamicSortedSet.size());
	}

	public void testRemove() {
		dynamicSortedSet.add(one);
		dynamicSortedSet.add(two);
		dynamicSortedSet.add(three);

		assertEquals(3, dynamicSortedSet.size());

		assertTrue(dynamicSortedSet.remove(one));
		assertEquals(2, dynamicSortedSet.size());
		assertFalse(dynamicSortedSet.remove(one));

		assertTrue(dynamicSortedSet.remove(three));
		assertEquals(1, dynamicSortedSet.size());
	}

	public void testRemoveNullForbidden() {
		try {
			dynamicSortedSet.remove(null);
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testRemoveAll() {
		dynamicSortedSet.add(two);
		dynamicSortedSet.add(new Integer(4));

		assertTrue(dynamicSortedSet.removeAll(list));
		assertEquals(1, dynamicSortedSet.size());
	}

	public void testFirst() {
		dynamicSortedSet.add(three);
		assertSame(three, dynamicSortedSet.first());
		dynamicSortedSet.add(two);
		assertSame(two, dynamicSortedSet.first());
		dynamicSortedSet.add(one);
		assertSame(one, dynamicSortedSet.first());

		dynamicSortedSet.remove(two);
		dynamicSortedSet.add(two);
		assertSame(one, dynamicSortedSet.first());

	}

	public void testFirstWithEmptySet() {
		assertTrue(dynamicSortedSet.isEmpty());
		try {
			dynamicSortedSet.first();
			fail("should have thrown NoSuchElementException");
		}
		catch (NoSuchElementException nsee) {
			// expected
		}
	}

	public void testLast() {
		dynamicSortedSet.add(one);
		assertSame(one, dynamicSortedSet.last());
		dynamicSortedSet.add(two);
		assertSame(two, dynamicSortedSet.last());
		dynamicSortedSet.add(three);
		assertSame(three, dynamicSortedSet.last());

		dynamicSortedSet.remove(two);
		dynamicSortedSet.add(two);
		assertSame(three, dynamicSortedSet.last());
	}

	public void testLastWithEmptySet() {
		assertTrue(dynamicSortedSet.isEmpty());
		try {
			dynamicSortedSet.last();
			fail("should have thrown NoSuchElementException");
		}
		catch (NoSuchElementException nsee) {
			// expected
		}

	}

}
