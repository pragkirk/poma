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
import java.util.ListIterator;

import org.springframework.osgi.service.importer.support.internal.collection.OsgiServiceCollection;
import org.springframework.osgi.service.importer.support.internal.collection.OsgiServiceList;

public class OsgiServiceListTest extends AbstractOsgiCollectionTest {

	private OsgiServiceList col;

	private ListIterator iter;

	protected void setUp() throws Exception {
		super.setUp();

		col = (OsgiServiceList) super.col;
		iter = col.listIterator();
	}

	OsgiServiceCollection createCollection() {
		return new OsgiServiceList(null, context, getClass().getClassLoader(), createProxyCreator(new Class[] {
				Wrapper.class, Comparable.class }));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		col = null;
		iter = null;
	}

	public void testAddDuplicates() {
		long time1 = 123;
		Date date1 = new Date(time1);

		addService(date1);
		assertEquals(1, col.size());

		addService(date1);
		assertEquals("duplicate not added", 2, col.size());

		addService(date1);
		assertEquals("duplicate not added", 3, col.size());
	}

	public void testRemoveDuplicate() {
		long time1 = 123;
		Date date1 = new Date(time1);

		addService(date1);
		addService(date1);
		addService(date1);

		assertEquals(3, col.size());

		removeService(date1);
		assertEquals(2, col.size());

		removeService(date1);
		assertEquals(1, col.size());
	}

	public void testListIteratorWhileAdding() {
		long time1 = 123;
		Wrapper date = new DateWrapper(time1);

		addService(date);

		assertEquals(0, iter.nextIndex());
		assertEquals(new Long(time1), ((Wrapper) iter.next()).execute());
		addService(date);
		assertEquals(1, iter.nextIndex());
		assertEquals(new Long(time1), ((Wrapper) iter.next()).execute());
	}

	public void testListIteratorWhileRemoving() {

		long time1 = 123;
		Wrapper date = new DateWrapper(time1);

		addService(date);
		addService(date);

		assertEquals(0, iter.nextIndex());
		Wrapper proxy1 = (Wrapper) iter.next();

		assertEquals(new Long(time1), proxy1.execute());
		removeService(date);

		assertEquals(1, iter.nextIndex());
		assertFalse(iter.hasNext());
		assertTrue(iter.hasPrevious());
		Wrapper proxy2 = ((Wrapper) iter.previous());
		assertEquals(new Long(time1), proxy2.execute());

		assertSame(proxy1, proxy2);

	}
}
