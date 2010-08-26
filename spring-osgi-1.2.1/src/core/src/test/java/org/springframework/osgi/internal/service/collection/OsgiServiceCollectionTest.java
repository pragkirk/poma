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

import java.util.Date;
import java.util.Iterator;

import org.springframework.osgi.service.importer.support.internal.collection.OsgiServiceCollection;

/**
 * Mock test for OsgiServiceCollection.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceCollectionTest extends AbstractOsgiCollectionTest {

	private Iterator iter;


	protected void setUp() throws Exception {
		super.setUp();
		iter = col.iterator();
	}

	OsgiServiceCollection createCollection() {
		return new OsgiServiceCollection(null, context, getClass().getClassLoader(), createProxyCreator(new Class[] {
			Wrapper.class, Comparable.class }));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		col = null;
		iter = null;
	}

	public void testAddServiceBySize() throws Exception {
		assertEquals(0, col.size());

		Date service1 = new Date();
		addService(service1);

		assertEquals(1, col.size());

		Date service2 = new Date();
		addService(service2);

		assertEquals(2, col.size());
	}

	public void testAddServiceByIterating() throws Exception {
		DateWrapper service = new DateWrapper(123);
		addService(service);

		assertTrue(iter.hasNext());
		assertEquals(service.execute(), ((Wrapper) iter.next()).execute());

		assertFalse(iter.hasNext());

		DateWrapper date2 = new DateWrapper(321);
		addService(date2);
		assertTrue(iter.hasNext());
		assertEquals(date2.execute(), ((Wrapper) iter.next()).execute());

		assertFalse(iter.hasNext());
	}

	public void testRemoveService() {
		assertEquals(0, col.size());

		Date service1 = new Date();
		addService(service1);

		assertEquals(1, col.size());

		removeService(service1);
		assertEquals(0, col.size());

	}

	public void testRemoveServiceWhileIterating() throws Exception {
		long time1 = 123;
		DateWrapper date1 = new DateWrapper(time1);

		long time2 = 321;
		DateWrapper date2 = new DateWrapper(time2);
		addService(date1);
		addService(date2);

		assertTrue(iter.hasNext());
		assertEquals(new Long(time1), ((Wrapper) iter.next()).execute());

		assertTrue(iter.hasNext());
		removeService(date2);
		assertFalse(iter.hasNext());
	}
}
