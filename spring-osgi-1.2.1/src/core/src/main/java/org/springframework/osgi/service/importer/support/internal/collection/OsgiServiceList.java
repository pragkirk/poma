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

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

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
public class OsgiServiceList extends OsgiServiceCollection implements List, RandomAccess {

	protected class OsgiServiceListIterator implements ListIterator {

		// dynamic iterator
		private final ListIterator iter;


		public OsgiServiceListIterator(int index) {
			iter = storage.listIterator(index);
		}

		public Object next() {
			mandatoryServiceCheck();
			return iter.next();
		}

		public Object previous() {
			mandatoryServiceCheck();
			return iter.previous();
		}

		//
		// index operations
		//
		public boolean hasNext() {
			mandatoryServiceCheck();
			return iter.hasNext();
		}

		public boolean hasPrevious() {
			mandatoryServiceCheck();
			return iter.hasPrevious();
		}

		public int nextIndex() {
			mandatoryServiceCheck();
			return iter.nextIndex();
		}

		public int previousIndex() {
			mandatoryServiceCheck();
			return iter.previousIndex();
		}

		//
		// read-only operations
		//
		public void add(Object o) {
			throw new UnsupportedOperationException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void set(Object o) {
			throw new UnsupportedOperationException();
		}

	};


	/**
	 * cast the collection to a specialized collection
	 */
	protected List storage;


	public OsgiServiceList(Filter filter, BundleContext context, ClassLoader classLoader,
			ServiceProxyCreator proxyCreator) {
		super(filter, context, classLoader, proxyCreator);
	}

	protected DynamicCollection createInternalDynamicStorage() {
		storage = new DynamicList();
		return (DynamicList) storage;
	}

	public Object get(int index) {
		mandatoryServiceCheck();
		return storage.get(index);
	}

	public int indexOf(Object o) {
		// FIXME: implement this
		throw new UnsupportedOperationException();
	}

	public int lastIndexOf(Object o) {
		// FIXME: implement this
		throw new UnsupportedOperationException();
	}

	public ListIterator listIterator() {
		return listIterator(0);
	}

	public ListIterator listIterator(final int index) {
		return new OsgiServiceListIterator(index);
	}

	public List subList(int fromIndex, int toIndex) {
		// FIXME: implement this
		// note: the trick here is to return a list which is backed up by this
		// one (i.e. read-only)
		throw new UnsupportedOperationException();
	}

	//
	// WRITE operations forbidden
	//
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	public Object set(int index, Object o) {
		throw new UnsupportedOperationException();
	}

	public void add(int index, Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(int index, Collection c) {
		throw new UnsupportedOperationException();
	}
}