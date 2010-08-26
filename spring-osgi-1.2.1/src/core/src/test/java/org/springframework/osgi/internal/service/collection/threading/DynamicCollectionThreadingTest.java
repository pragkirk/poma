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
package org.springframework.osgi.internal.service.collection.threading;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.osgi.service.importer.support.internal.collection.DynamicCollection;

/**
 * Multithreading test for DynamicCollection.
 * 
 * @author Costin Leau
 * 
 */

// S0: T1 -> get iterator, check empty collection
// S1: T2 -> get iterator
// S2: W -> write 2 items
// S3: T2 -> walk the iterator one step
// S4: T1 -> walk the iterator 2 steps
// S5: W -> eliminate one element
// S6: T1 -> check the iterator
// S7: T2 -> check the iterator
public class DynamicCollectionThreadingTest extends BaseThreadingTest {

	private Collection col;

	public void initialize() {
		col = new DynamicCollection();
		assertTrue(col.isEmpty());
		this.setTrace(true);
	}

	public void finish() {
		col = null;
	}

	public void threadWriter() throws Exception {
		// S2
		waitForTick(20);
		col.add(new Object());
		Object willBeRemoved = new Object();
		col.add(willBeRemoved);

		// S5
		waitForTick(50);
		col.remove(willBeRemoved);
	}

	public void threadIter1() throws Exception {
		// S0
		Iterator iter = col.iterator();
		assertFalse(iter.hasNext());

		// S4
		waitForTick(40);

		assertTrue(iter.hasNext());
		iter.next();

		assertTrue(iter.hasNext());
		iter.next();

		// S6
		waitForTick(60);
		assertFalse(iter.hasNext());
	}

	public void threadIter2() throws Exception {
		// S1
		waitForTick(10);
		Iterator iter = col.iterator();
		// S3
		waitForTick(30);
		assertTrue(iter.hasNext());
		iter.next();
		// S7
		waitForTick(70);
		assertFalse(iter.hasNext());
	}
}
