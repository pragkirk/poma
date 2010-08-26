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

package org.springframework.osgi.util.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockServiceReference;

public class ServiceReferenceBasedMapTest extends TestCase {

	private ServiceReference reference;
	private Map map;


	protected void setUp() throws Exception {
		reference = new MockServiceReference();
		createMap();
	}

	protected void tearDown() throws Exception {
		reference = null;
		map = null;
	}

	private void createMap() {
		map = new ServiceReferenceBasedMap(reference);
	}

	public void testClear() {
		try {
			map.clear();
			fail("map is read-only; expected exception");
		}
		catch (Exception ex) {
		}
	}

	public void testContainsKeyObject() {
		Properties prop = new Properties();
		prop.setProperty("joe", "satriani");
		reference = new MockServiceReference(new MockBundle(), prop, null);
		createMap();
		assertTrue(map.containsKey("joe"));
	}

	public void testContainsValueObject() {
		Properties prop = new Properties();
		prop.setProperty("joe", "satriani");
		reference = new MockServiceReference(new MockBundle(), prop, null);
		createMap();
		assertTrue(map.containsValue("satriani"));
	}

	public void testEntrySet() {
		Properties prop = new Properties();
		prop.setProperty("joe", "satriani");
		reference = new MockServiceReference(new MockBundle(), prop, null);
		createMap();
		Set entries = map.entrySet();
		assertNotNull(entries);

		for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			assertTrue(map.containsKey(entry.getKey()));
			assertEquals(entry.getValue(), map.get(entry.getKey()));
		}
	}

	public void testGetObject() {
		Properties prop = new Properties();
		prop.setProperty("joe", "satriani");
		reference = new MockServiceReference(new MockBundle(), prop, null);
		createMap();
		assertEquals("satriani", map.get("joe"));
	}

	public void testPutObjectObject() {
		try {
			map.put(new Object(), new Object());
			fail("map is read-only; expected exception");
		}
		catch (Exception ex) {
		}
	}

	public void testPutAllMap() {
		try {
			map.putAll(new HashMap());
			fail("map is read-only; expected exception");
		}
		catch (Exception ex) {
		}
	}

	public void testRemoveObject() {
		try {
			map.remove(new Object());
			fail("map is read-only; expected exception");
		}
		catch (Exception ex) {
		}
	}
}
