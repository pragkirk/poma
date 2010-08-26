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
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * Subclass offering a List extension for a DynamicCollection. This allows not
 * just forward, but also backwards iteration through the
 * <code>ListIterator</list>.
 * 
 * @author Costin Leau
 *
 */
public class DynamicList extends DynamicCollection implements List, RandomAccess {

	/**
	 * List iterator.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private class DynamicListIterator extends DynamicIterator implements ListIterator {

		/**
		 * Similar to {@link DynamicIterator#tailGhost} in functionality but
		 * representing the last seen object in the head of the collection.
		 */
		protected volatile Object headGhost = null;

		// flag used for enforcing the iterator consistency:
		// null - do not enforce anything
		// true - should not throw exception
		// false - should throw exception
		/**
		 * Iterator variable - not thread-safe/synchronized since only one
		 * thread should use the iterator.
		 */
		protected Boolean hasPrevious = null;

		/**
		 * Boolean field used by the {@link #set(Object)} and {@link #remove()}operation.
		 * True indicates next() was called, and false previous().
		 */
		private boolean previousOperationCalled = true;


		private DynamicListIterator(int index) {
			super.cursor = index;
		}

		public void add(Object o) {
			removalAllowed = false;
			synchronized (storage) {
				synchronized (lock) {
					DynamicList.this.add(cursor, o);
				}
			}
		}

		/**
		 * Updates the hasPrevious field.
		 * 
		 * Internal unprotected method to avoid nested synchronization blocks.
		 * To execute this code, one needs the storage, iteratorsLock and
		 * iterator lock.
		 * 
		 * @return
		 */
		private boolean unsafeHasPrevious() {
			hasPrevious = (cursor - 1 >= 0 ? Boolean.TRUE : Boolean.FALSE);
			return hasPrevious.booleanValue();
		}

		public boolean hasPrevious() {
			synchronized (lock) {
				headGhost = null;
				return unsafeHasPrevious();
			}
		}

		public int nextIndex() {
			synchronized (lock) {
				return cursor;
			}
		}

		public Object next() {
			previousOperationCalled = true;
			return super.next();
		}

		public Object previous() {
			try {
				removalAllowed = true;
				previousOperationCalled = false;
				// no enforcement
				if (hasPrevious == null) {
					synchronized (storage) {
						synchronized (lock) {
							if (unsafeHasPrevious())
								return storage.get(--cursor);
							else
								throw new NoSuchElementException();
						}
					}
				}
				// need to return an object no matter what
				else if (hasPrevious.booleanValue()) {
					synchronized (storage) {
						synchronized (lock) {
							// if there is an element available, return it
							if (unsafeHasPrevious()) {
								return storage.get(--cursor);
							}
							else {
								// otherwise return the last one seen
								return headGhost;
							}
						}
					}
				}
				// should throw exception no matter what
				else {
					throw new NoSuchElementException();
				}
			}
			finally {
				// no matter what, reset hasPrevious
				hasPrevious = null;
				// remove ghost object
				synchronized (lock) {
					headGhost = null;
				}
			}
		}

		public int previousIndex() {
			synchronized (lock) {
				return (cursor - 1);
			}
		}

		public void set(Object o) {
			if (!removalAllowed)
				throw new IllegalStateException();
			synchronized (storage) {
				synchronized (lock) {
					int index = (previousOperationCalled ? cursor - 1 : cursor);
					if (index < 0) {
						index = 0;
					}
					else {
						int length = storage.size();
						if (index > length) {
							index = length;
						}
					}
					storage.set(index, o);
				}
			}
		}

		protected int removalIndex(int cursor) {
			int index = (previousOperationCalled ? cursor - 1 : cursor);
			if (index < 0) {
				index = 0;
			}
			else {
				int length;
				synchronized (storage) {
					length = storage.size();
				}
				if (index > length) {
					index = length;
				}
			}
			return index;
		}
	}


	public DynamicList() {
		super();
	}

	public DynamicList(Collection c) {
		super(c);
	}

	public DynamicList(int size) {
		super(size);
	}

	public void add(int index, Object o) {
		super.add(index, o);
	}

	public boolean addAll(int index, Collection c) {
		synchronized (storage) {
			return storage.addAll(index, c);
		}
	}

	public Object get(int index) {
		synchronized (storage) {
			return storage.get(index);
		}
	}

	public int indexOf(Object o) {
		synchronized (storage) {
			return storage.indexOf(o);
		}
	}

	public int lastIndexOf(Object o) {
		synchronized (storage) {
			return storage.lastIndexOf(o);
		}
	}

	public ListIterator listIterator() {
		ListIterator iter = new DynamicListIterator(0);

		synchronized (iterators) {
			iterators.put(iter, null);
		}

		return iter;
	}

	public ListIterator listIterator(int index) {
		return new DynamicListIterator(index);
	}

	public Object remove(int index) {
		return super.remove(index);
	}

	public Object set(int index, Object o) {
		synchronized (storage) {
			return storage.set(index, o);
		}
	}

	// TODO: test behavior to see if the returned list properly behaves under
	// dynamic circumstances
	public List subList(int fromIndex, int toIndex) {
		synchronized (storage) {
			return storage.subList(fromIndex, toIndex);
		}
	}
}