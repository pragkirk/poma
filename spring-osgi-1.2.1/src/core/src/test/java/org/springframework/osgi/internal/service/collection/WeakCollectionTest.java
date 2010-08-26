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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import junit.framework.TestCase;

import org.springframework.osgi.GCTests;

/**
 * @author Costin Leau
 * 
 */
public class WeakCollectionTest extends TestCase {

	public void testWeakList() {
		List list = new ArrayList();

		// add some weak references
		for (int i = 0; i < 20; i++) {
			list.add(new WeakReference(new Object()));
		}

		GCTests.assertGCed(new WeakReference(new Object()));
		for (int i = 0; i < list.size(); i++) {
			assertNull(((WeakReference) list.get(i)).get());

		}
	}

	public void testWeakHashMap() {
		Map weakMap = new WeakHashMap();

		for (int i = 0; i < 10; i++) {
			weakMap.put(new Object(), null);
		}

		GCTests.assertGCed(new WeakReference(new Object()));

		Set entries = weakMap.entrySet();
		
		for (Iterator iter = entries.iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			assertNull(entry.getKey());
		}

	}
}
