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
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import junit.framework.TestCase;

import org.springframework.osgi.service.importer.support.internal.collection.DynamicSortedSet;

/**
 * 
 * @author Costin Leau
 * 
 */
public class DynamicSortedSetIteratorTest extends TestCase {

	private SortedSet dynamicSortedSet;

	private List list;

	private Integer one;

	private Integer two;

	private Integer three;

	private Iterator iter;

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

		iter = dynamicSortedSet.iterator();
	}

	protected void tearDown() throws Exception {
		dynamicSortedSet = null;
		list = null;
		one = null;
		two = null;
		three = null;
		iter = null;
	}

	public void testIteratingWhileAdding() {
		assertFalse(iter.hasNext());

		assertTrue(dynamicSortedSet.add(two));

		assertTrue(iter.hasNext());
		assertSame(two, iter.next());

		// added before two
		dynamicSortedSet.add(one);
		assertFalse(iter.hasNext());
	}

	public void testIteratingWhileAddingSeveralElements() {
		assertFalse(iter.hasNext());

		dynamicSortedSet.add(two);
		dynamicSortedSet.add(three);

		assertTrue(iter.hasNext());
		assertSame(two, iter.next());

		// added before two
		dynamicSortedSet.add(one);
		assertTrue(iter.hasNext());
		assertSame(three, iter.next());
	}

	public void testDoubleIteratingWithAdding() {
		assertFalse(iter.hasNext());
		Iterator it = dynamicSortedSet.iterator();

		dynamicSortedSet.add(two);

		assertTrue(iter.hasNext());
		assertSame(two, iter.next());
		dynamicSortedSet.add(one);

		assertFalse(iter.hasNext());
		assertSame(one, it.next());
		assertSame(two, it.next());
	}

	public void testIteratingWhileRemoving() {
		dynamicSortedSet.add(two);
		dynamicSortedSet.add(three);

		assertSame(two, iter.next());
		dynamicSortedSet.remove(two);
		assertSame(three, iter.next());
	}

	public void testDoubleIteratingWhileRemoving() {
		Iterator it = dynamicSortedSet.iterator();
		dynamicSortedSet.add(one);
		dynamicSortedSet.add(three);
		dynamicSortedSet.add(two);

		assertSame(one, iter.next());
		assertSame(two, iter.next());
		dynamicSortedSet.remove(two);
		assertSame(one, it.next());

		dynamicSortedSet.remove(three);
		assertFalse(iter.hasNext());
		assertFalse(it.hasNext());
	}

}
