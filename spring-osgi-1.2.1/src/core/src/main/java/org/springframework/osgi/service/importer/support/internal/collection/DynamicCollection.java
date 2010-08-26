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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;

/**
 * Collection which can be increased or reduced at runtime while iterating.
 * Iterators returned by this implementation are consistent - it is guaranteed
 * that {@link Iterator#next()} will obey the result of the previously called
 * {@link Iterator#hasNext()} even though the collection content has been
 * modified.
 * 
 * This collection is thread-safe with the condition that there is at most one
 * writing thread at a point in time. There are no restrains on the number of
 * readers.
 * 
 * @author Costin Leau
 * 
 */
public class DynamicCollection extends AbstractCollection {

	/**
	 * Dynamic <strong>consistent</strong> iterator. This iterator is not
	 * thread-safe with respect to iteration (it should not be shared against
	 * multiple threads) but it is thread safe with respect to the backing
	 * storage which might be modified during the iterator life cycle.
	 * 
	 * @author Costin Leau
	 */
	protected class DynamicIterator implements Iterator {

		/**
		 * Cursor pointing to the element that has to be returned by
		 * {@link #next()} method. This element needs to be synchronized.
		 */
		protected volatile int cursor = 0;

		/**
		 * Temporary object holder. Used in case the last element in the
		 * collection becomes empty after an iterator #hasNext() method was
		 * called but before #next() is invoked since otherwise the iterator
		 * needs to return an element but it cannot.
		 * 
		 * Subclasses (such as lists) should implement their own strategy when
		 * it comes to assign a value to it to accommodate the collection
		 * semantics (ordered vs indexed).
		 * 
		 * This particular field represents the tail of the collection, since no
		 * order or indexing is enforced and the iteration can only go forward.
		 * 
		 * 
		 * The field is assigned when an object is removed and resetted by calls
		 * to hasNext() or next().
		 * 
		 * Thread-safety note: Since this object is affected by the storage
		 * shrinking it needs to be synchronized.
		 */
		protected volatile Object tailGhost = null;

		/**
		 * Lock protecting the cursor and tailGhost which might be affected by
		 * the backing collection shrinking.
		 */
		protected final Object lock = new Object();

		// flag used for enforcing the iterator consistency:
		// null - do not enforce anything
		// true - should not throw exception
		// false - should throw exception
		/**
		 * Iterator variable - not thread-safe/synchronized since only one
		 * thread should use the iterator.
		 */
		protected Boolean hasNext = null;

		/**
		 * Iterator variable - not thread-safe/synchronized since only one
		 * thread should use the iterator.
		 */
		protected boolean removalAllowed = false;


		public boolean hasNext() {
			synchronized (storage) {
				synchronized (iteratorsLock) {
					synchronized (lock) {
						tailGhost = null;
						return unsafeHasNext();
					}
				}
			}
		}

		/**
		 * Updates the hasNext field.
		 * 
		 * Internal unprotected method to avoid nested synchronization blocks.
		 * To execute this code, one needs the storage, iteratorsLock and
		 * iterator lock.
		 * 
		 * @return
		 */
		protected boolean unsafeHasNext() {
			hasNext = (cursor < storage.size() ? Boolean.TRUE : Boolean.FALSE);
			return hasNext.booleanValue();
		}

		public Object next() {
			try {
				removalAllowed = true;
				// no enforcement
				if (hasNext == null) {
					synchronized (storage) {
						synchronized (iteratorsLock) {
							synchronized (lock) {
								if (unsafeHasNext())
									return storage.get(cursor++);
								else
									throw new NoSuchElementException();
							}
						}
					}
				}
				// need to return an object no matter what
				else if (hasNext.booleanValue()) {
					synchronized (storage) {
						synchronized (iteratorsLock) {
							synchronized (lock) {
								// if there is an element available, return it
								if (unsafeHasNext()) {
									return storage.get(cursor++);
								}
								else {
									// otherwise return the last one seen
									// return tailGhost;
									return tailGhost;
								}
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
				// no matter what, reset hasNext
				hasNext = null;
				// remove ghost object
				synchronized (lock) {
					tailGhost = null;
				}
			}
		}

		public void remove() {
			// make sure the cursor is valid
			if (removalAllowed) {
				removalAllowed = false;
				int cursorCopy;
				synchronized (lock) {
					cursorCopy = cursor;
				}
				DynamicCollection.this.remove(removalIndex(cursorCopy));

			}
			else
				throw new IllegalStateException();
		}

		protected int removalIndex(int cursor) {
			return cursor - 1;
		}

		/**
		 * Removes the object from the underlying collection. This operation is
		 * relevant to the iterators since it can occur in between
		 * hasNext()/next() calls. If no object is left, next() is forced to
		 * return null which can contradict the hasNext() contract. For such a
		 * case, the iterator is forced to temporarily recall the last available
		 * object.
		 * 
		 * @param index
		 * @param o
		 */
		void removeObject(int index, Object o) {
			synchronized (lock) {
				tailGhost = o;
			}
		}
	}


	/** Lock used by operations that require iterator updates (such as removal) */
	/**
	 * If it interacts with the storage, the *storage* lock needs to be acquired
	 * first
	 */
	protected final Object iteratorsLock = new Object();

	/** actual collection storage */
	/** this list is not-synchronized by default */
	protected final List storage;

	/** map of weak references to the list iterators */
	/**
	 * should have been a list but there is no 'WeakReference'-based
	 * implementation in the JDK
	 */
	protected final Map iterators;


	public DynamicCollection() {
		this(16);
	}

	public DynamicCollection(int size) {
		storage = new ArrayList(size);
		iterators = new WeakHashMap(4);
	}

	public DynamicCollection(Collection c) {
		this(c.size());
		addAll(c);
	}

	public Iterator iterator() {
		Iterator iter = new DynamicIterator();

		synchronized (iteratorsLock) {
			iterators.put(iter, null);
		}

		return iter;
	}

	public void clear() {
		synchronized (storage) {
			storage.clear();
		}
	}

	public int size() {
		synchronized (storage) {
			return storage.size();
		}
	}

	public boolean add(Object o) {
		synchronized (storage) {
			return storage.add(o);
		}
	}

	public boolean addAll(Collection c) {
		synchronized (storage) {
			return storage.addAll(c);
		}
	}

	public boolean contains(Object o) {
		synchronized (storage) {
			return storage.contains(o);
		}
	}

	public boolean containsAll(Collection c) {
		synchronized (storage) {
			return storage.containsAll(c);
		}
	}

	public boolean isEmpty() {
		synchronized (storage) {
			return storage.isEmpty();
		}
	}

	public boolean remove(Object o) {
		synchronized (storage) {
			int index = storage.indexOf(o);

			if (index == -1)
				return false;

			remove(index);
			return true;
		}
	}

	// remove an object from the list using the given index
	// this is required for cases where the underlying storage (a list) might
	// contain duplicates.
	protected Object remove(int index) {
		Object o = null;

		// first acquire storage lock
		synchronized (storage) {
			// then the iterator
			synchronized (iteratorsLock) {

				// update storage
				o = storage.remove(index);

				// update iterators
				for (Iterator iter = iterators.entrySet().iterator(); iter.hasNext();) {
					Map.Entry entry = (Map.Entry) iter.next();
					DynamicIterator dynamicIterator = (DynamicIterator) entry.getKey();

					synchronized (dynamicIterator.lock) {
						if (index < dynamicIterator.cursor) {
							dynamicIterator.cursor--;
						}
						// set ghost object (if objects disappear in between hasNext()/next() calls) 
						else {
							dynamicIterator.removeObject(index, o);
						}
					}
				}
			}
		}

		return o;
	}

	// extra-collection method used by list or sorted set.
	// adds an object to the indicated position forcing an update on the
	// iterators.
	protected void add(int index, Object o) {
		// update iterators (since items are not added at the end
		// anymore)

		// first acquire storage lock
		synchronized (storage) {
			synchronized (iteratorsLock) {
				// update storage
				storage.add(index, o);

				for (Iterator iter = iterators.entrySet().iterator(); iter.hasNext();) {
					Map.Entry entry = (Map.Entry) iter.next();
					DynamicIterator dynamicIterator = (DynamicIterator) entry.getKey();

					synchronized (dynamicIterator.lock) {
						if (index < dynamicIterator.cursor)
							dynamicIterator.cursor++;
					}
				}
			}
		}
	}

	public Object[] toArray() {
		synchronized (storage) {
			return storage.toArray();
		}
	}

	public Object[] toArray(Object[] array) {
		synchronized (storage) {
			return storage.toArray(array);
		}
	}

	public String toString() {
		synchronized (storage) {
			return storage.toString();
		}
	}

	/**
	 * Hook used by wrapping collections to determine the position of the object
	 * being removed while iterating.
	 * 
	 * @param o
	 * @return
	 */
	protected int indexOf(Object o) {
		synchronized (storage) {
			return storage.indexOf(o);
		}
	}
}