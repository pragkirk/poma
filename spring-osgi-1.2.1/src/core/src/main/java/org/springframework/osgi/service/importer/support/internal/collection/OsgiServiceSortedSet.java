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
import java.util.SortedSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceProxyCreator;


/**
 * OSGi service dynamic collection - allows iterating while the underlying
 * storage is being shrunk/expanded. This collection is read-only - its content
 * is being retrieved dynamically from the OSGi platform.
 * 
 * <p/> This collection and its iterators are thread-safe. That is, multiple
 * threads can access the collection. However, since the collection is
 * read-only, it cannot be modified by the client.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceSortedSet extends OsgiServiceSet implements SortedSet {

	/**
	 * cast the collection to a specialized collection
	 */
	private SortedSet storage;

	private final Comparator comparator;

	public OsgiServiceSortedSet(Filter filter, BundleContext context, ClassLoader classLoader,
			ServiceProxyCreator proxyCreator) {
		this(filter, context, classLoader, null, proxyCreator);
	}

	public OsgiServiceSortedSet(Filter filter, BundleContext context, ClassLoader classLoader, Comparator comparator,
			ServiceProxyCreator proxyCreator) {
		super(filter, context, classLoader, proxyCreator);
		this.comparator = comparator;
	}

	protected DynamicCollection createInternalDynamicStorage() {
		storage = new DynamicSortedSet(comparator);
		return (DynamicCollection) storage;
	}

	public Comparator comparator() {
		return storage.comparator();
	}

	public Object first() {
		mandatoryServiceCheck();
		return storage.first();
	}

	public Object last() {
		mandatoryServiceCheck();
		return storage.last();
	}

	public SortedSet tailSet(Object fromElement) {
		mandatoryServiceCheck();
		return storage.tailSet(fromElement);
	}

	public SortedSet headSet(Object toElement) {
		mandatoryServiceCheck();
		return storage.headSet(toElement);
	}

	public SortedSet subSet(Object fromElement, Object toElement) {
		mandatoryServiceCheck();
		return storage.subSet(fromElement, toElement);
	}
}