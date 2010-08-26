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

package org.springframework.osgi.compendium.internal.cm;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * BeanManaged update class. Performs the update call using a custom method on
 * the given object.
 * 
 * @author Costin Leau
 */
class BeanManagedUpdate implements UpdateCallback {

	private final String methodName;
	// class cache = keeps track of method adapters for each given class
	// the cache becomes useful when dealing with FactoryBean which can returns
	// different class types on each invocation
	private final Map classCache = new WeakHashMap(2);


	public BeanManagedUpdate(String methodName) {
		this.methodName = methodName;
	}

	public void update(Object instance, Map properties) {
		getUpdateMethod(instance).invoke(instance, properties);
	}

	/**
	 * Returns a (lazily created) method adapter that invokes a predefined
	 * method on the given instance.
	 * 
	 * @param instance object instance
	 * @return method update method adapter
	 */
	private UpdateMethodAdapter getUpdateMethod(Object instance) {
		UpdateMethodAdapter adapter;
		Class type = instance.getClass();

		WeakReference adapterReference = (WeakReference) classCache.get(type);
		if (adapterReference != null) {
			adapter = (UpdateMethodAdapter) adapterReference.get();
			if (adapter != null)
				return adapter;
		}
		adapter = new UpdateMethodAdapter(methodName, type);
		classCache.put(type, new WeakReference(adapter));
		return adapter;
	}
}