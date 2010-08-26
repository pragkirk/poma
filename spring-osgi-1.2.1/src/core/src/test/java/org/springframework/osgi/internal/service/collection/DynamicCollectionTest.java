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

import java.util.Collection;

import junit.framework.TestCase;

import org.springframework.osgi.service.importer.support.internal.collection.DynamicCollection;

/**
 * Tests regarding the collection behaviour.
 * 
 * @author Costin Leau
 * 
 */
public class DynamicCollectionTest extends TestCase {

	private Collection dynamicCollection;

	protected void setUp() throws Exception {
		dynamicCollection = new DynamicCollection();
	}

	protected void tearDown() throws Exception {
		dynamicCollection = null;
	}

	public void testAdd() {
		assertTrue(dynamicCollection.add(new Object()));
	}

	public void testAddDuplicate() {
		Object obj = new Object();
		assertTrue(dynamicCollection.add(obj));
		assertTrue(dynamicCollection.add(obj));
	}

	public void testRemove() {
		Object obj = new Object();
		assertFalse(dynamicCollection.remove(obj));
		dynamicCollection.add(obj);
		assertTrue(dynamicCollection.remove(obj));
		assertFalse(dynamicCollection.remove(obj));
	}

	public void testRemoveDuplicate() {
		Object obj = new Object();
		assertFalse(dynamicCollection.remove(obj));
		dynamicCollection.add(obj);
		dynamicCollection.add(obj);
		assertTrue(dynamicCollection.remove(obj));
		assertTrue(dynamicCollection.remove(obj));
		dynamicCollection.add(obj);
		assertTrue(dynamicCollection.remove(obj));
	}

}
