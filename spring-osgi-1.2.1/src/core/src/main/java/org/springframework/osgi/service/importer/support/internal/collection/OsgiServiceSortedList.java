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
package org.springframework.osgi.service.importer.support.internal.collection;

import java.util.Comparator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceProxyCreator;

/**
 * Ordered list similar to a SortedSet with the difference, that it accepts
 * duplicates.
 * 
 * @see Comparable
 * @see Comparator
 * @see java.util.SortedSet
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceSortedList extends OsgiServiceList {

	private final Comparator comparator;

	/**
	 * @param filter
	 * @param context
	 * @param classLoader
	 */
	public OsgiServiceSortedList(Filter filter, BundleContext context, ClassLoader classLoader,
			ServiceProxyCreator proxyCreator) {
		this(filter, context, classLoader, null, proxyCreator);
	}

	public OsgiServiceSortedList(Filter filter, BundleContext context, ClassLoader classLoader, Comparator comparator,
			ServiceProxyCreator proxyCreator) {
		super(filter, context, classLoader, proxyCreator);
		this.comparator = comparator;
	}

	protected DynamicCollection createInternalDynamicStorage() {
		storage = new DynamicSortedList(comparator);
		return (DynamicCollection) storage;
	}

	public Comparator comparator() {
		return comparator;
	}

}
