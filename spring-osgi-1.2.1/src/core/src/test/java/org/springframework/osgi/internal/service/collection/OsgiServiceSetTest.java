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

import java.util.Dictionary;
import java.util.Iterator;
import java.util.Properties;

import org.osgi.framework.Constants;
import org.springframework.osgi.service.importer.support.internal.collection.OsgiServiceCollection;
import org.springframework.osgi.service.importer.support.internal.collection.OsgiServiceSet;

/**
 * @author Costin Leau
 * 
 */
public class OsgiServiceSetTest extends AbstractOsgiCollectionTest {

	private Iterator iter;

	private Dictionary serviceProps;

	protected void setUp() throws Exception {
		super.setUp();
		iter = col.iterator();

		serviceProps = new Properties();
		// set the id to test uniqueness
		serviceProps.put(Constants.SERVICE_ID, new Long(13));

	}

	protected void tearDown() throws Exception {
		super.tearDown();
		col = null;
		iter = null;
	}

	OsgiServiceCollection createCollection() {
		return new OsgiServiceSet(null, context, getClass().getClassLoader(), createProxyCreator(new Class[] {
				Wrapper.class, Comparable.class }));
	}

	public void testAddDuplicates() {
		long time1 = 123;

		Wrapper date = new DateWrapper(time1);

		assertEquals(0, col.size());

		addService(date, serviceProps);
		assertEquals(1, col.size());

		addService(date, serviceProps);
		assertEquals("set accepts duplicate services", 1, col.size());
	}

	public void testAddEqualServiceInstances() {
		long time = 123;
		Wrapper date1 = new DateWrapper(time);
		Wrapper date2 = new DateWrapper(time);

		assertEquals(date1, date2);

		assertEquals(0, col.size());

		addService(date1, serviceProps);
		assertEquals(1, col.size());
		addService(date2, serviceProps);
		assertEquals("set accepts duplicate services", 1, col.size());
	}

	public void testAddEqualServiceInstancesWithIterator() {
		long time = 123;
		Wrapper date1 = new DateWrapper(time);
		Wrapper date2 = new DateWrapper(time);

		assertEquals(date1, date2);

		assertEquals(0, col.size());

		assertFalse(iter.hasNext());
		addService(date1, serviceProps);
		assertTrue(iter.hasNext());
		assertEquals(date1.execute(), ((Wrapper) iter.next()).execute());
		assertFalse(iter.hasNext());
		addService(date1, serviceProps);
		assertFalse("set accepts duplicate services", iter.hasNext());
	}

	public void testRemoveDuplicates() {
		long time1 = 123;
		Wrapper date = new DateWrapper(time1);
		Wrapper date2 = new DateWrapper(time1 * 2);

		assertEquals(0, col.size());
		addService(date);
		assertEquals(1, col.size());
		addService(date2);
		assertEquals(2, col.size());

		removeService(date2);
		assertEquals(1, col.size());
		removeService(date2);
		assertEquals(1, col.size());
	}
}
