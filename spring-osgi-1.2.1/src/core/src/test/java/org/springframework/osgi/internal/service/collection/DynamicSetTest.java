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
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.osgi.service.importer.support.internal.collection.DynamicSet;

/**
 * @author Costin Leau
 * 
 */
public class DynamicSetTest extends TestCase {
	private Set dynamicSet;

	private Object obj;

	private List list;

	protected void setUp() throws Exception {
		dynamicSet = new DynamicSet();
		obj = new Object();
		list = new ArrayList();
		list.add(obj);
		list.add(obj);

	}

	protected void tearDown() throws Exception {
		dynamicSet = null;
		list = null;
		obj = null;
	}

	public void testNoDuplicatesWhileAdding() {
		Object obj = new Object();
		assertTrue(dynamicSet.add(obj));
		assertFalse(dynamicSet.add(obj));
	}

	public void testNoDuplicatesAfterRemovalAndAdding() {
		Object obj = new Object();
		assertTrue(dynamicSet.add(obj));
		assertTrue(dynamicSet.remove(obj));
		assertTrue(dynamicSet.add(obj));
	}

	public void testNullAllowed() {
		assertTrue(dynamicSet.add(null));
		assertFalse(dynamicSet.add(null));
	}

	public void testAddAllWithCollectionThatContainsDuplicates() {
		dynamicSet.addAll(list);
		assertTrue(dynamicSet.contains(obj));
		assertFalse(dynamicSet.add(obj));
	}

	public void testContainsAllWithCollectionsThatContainsDuplicates() {
		assertFalse(dynamicSet.containsAll(list));
		dynamicSet.add(obj);
		assertTrue(dynamicSet.containsAll(list));
	}

	public void testRetainAllWithCollectionThatContainsDuplicates() {
		assertFalse(dynamicSet.retainAll(list));
		dynamicSet.add(obj);
		assertFalse(dynamicSet.retainAll(list));
		dynamicSet.add(new Object());
		assertTrue(dynamicSet.retainAll(list));
	}

	public void testRemoveAllWithCollectionThatContainsDuplicates() {
		assertFalse(dynamicSet.removeAll(list));
		dynamicSet.add(obj);
		assertTrue(dynamicSet.removeAll(list));
		assertTrue(dynamicSet.isEmpty());
	}

}
