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
package org.springframework.osgi.iandt.serviceproxy;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.springframework.osgi.service.importer.support.internal.collection.OsgiServiceList;
import org.springframework.osgi.util.BundleDelegatingClassLoader;

/**
 * @author Costin Leau
 * 
 */
public abstract class ServiceListTst extends ServiceCollectionTest {

	protected Collection createCollection() {
		ClassLoader classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundleContext.getBundle());
		OsgiServiceList col = new OsgiServiceList(null, bundleContext, classLoader, null);
		col.setRequiredAtStartup(false);
		// col.setInterfaces(new Class[] { Date.class });
		col.afterPropertiesSet();
		return col;
	}

	public void testListContent() throws Exception {
		List list = (List) createCollection();

		// test the list iterator
		ListIterator iter = list.listIterator();
		Object b = iter.next();

		assertSame(b, iter.previous());
	}

}
