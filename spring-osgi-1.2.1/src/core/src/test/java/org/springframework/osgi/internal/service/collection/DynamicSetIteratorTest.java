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
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.osgi.service.importer.support.internal.collection.DynamicSet;

public class DynamicSetIteratorTest extends TestCase {

	private Set dynamicSet;

	private Iterator iter;

	private Integer one;

	private Integer two;

	private Integer three;

	protected void setUp() throws Exception {
		dynamicSet = new DynamicSet();
		iter = dynamicSet.iterator();

		one = new Integer(1);
		two = new Integer(2);
		three = new Integer(3);

	}

	protected void tearDown() throws Exception {
		dynamicSet = null;
		iter = null;
		one = null;
		two = null;
		three = null;
	}

	public void testIteratingWhileAdding() {
		assertFalse(iter.hasNext());
		dynamicSet.add(one);
		assertTrue(iter.hasNext());
		assertSame(one, iter.next());

		dynamicSet.add(two);
		assertTrue(iter.hasNext());
		dynamicSet.add(three);
		assertSame(two, iter.next());
		assertSame(three, iter.next());
	}

	public void testIteratingWhileAddingDuplicate() {
		assertFalse(iter.hasNext());
		dynamicSet.add(one);
		assertTrue(iter.hasNext());
		assertSame(one, iter.next());
		dynamicSet.add(one);
		assertFalse(iter.hasNext());
	}

	public void testIteratingWhileRemovingValidItem() {
		assertFalse(iter.hasNext());
		dynamicSet.add(one);
		dynamicSet.add(two);
		dynamicSet.add(three);
		assertTrue(iter.hasNext());
		dynamicSet.remove(one);
		assertSame(two, iter.next());
		dynamicSet.add(one);
		assertTrue(iter.hasNext());
		assertSame(three, iter.next());
	}

	public void testIteratingWhileAddingCollection() {
		assertFalse(iter.hasNext());
		List list = new ArrayList();
		list.add(two);
		list.add(two);
		list.add(three);

		dynamicSet.add(three);
		assertTrue(iter.hasNext());

		dynamicSet.addAll(list);
		assertSame(three, iter.next());
		assertSame(two, iter.next());
		assertFalse(iter.hasNext());
	}

}
