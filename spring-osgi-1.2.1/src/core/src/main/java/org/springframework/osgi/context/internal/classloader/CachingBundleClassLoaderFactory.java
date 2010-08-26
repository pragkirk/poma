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

package org.springframework.osgi.context.internal.classloader;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.osgi.framework.Bundle;
import org.springframework.osgi.util.BundleDelegatingClassLoader;

/**
 * Default implementation for {@link BundleClassLoaderFactory}.
 * 
 * @author Costin Leau
 * 
 */
class CachingBundleClassLoaderFactory implements BundleClassLoaderFactory {

	private static final String DELIMITER = "|";
	/**
	 * bundleToClassLoader cache
	 * 
	 * since a bundle can be refreshed, the map will return a map of class
	 * loaders
	 */
	private final Map cache = new WeakHashMap();


	public ClassLoader createClassLoader(Bundle bundle) {
		ClassLoader loader = null;
		// create a bundle identity object
		Object key = createKeyFor(bundle);

		Map loaders = null;
		// get associated class loaders (if any)
		synchronized (cache) {
			loaders = (Map) cache.get(bundle);
			if (loaders == null) {
				loaders = new HashMap(4);
				loader = createBundleClassLoader(bundle);
				loaders.put(key, new WeakReference(loader));
				return loader;
			}
		}
		// check the associated loaders
		synchronized (loaders) {
			WeakReference reference = (WeakReference) loaders.get(key);
			if (reference != null)
				loader = (ClassLoader) reference.get();
			// loader not found (or already recycled)
			if (loader == null) {
				loader = createBundleClassLoader(bundle);
				loaders.put(key, new WeakReference(loader));
			}
			return loader;
		}
	}

	/**
	 * Creates a key for the given bundle. This is needed since the bundle can
	 * be updated or refreshed and thus can have different class loaders during
	 * its lifetime which are not reflected in its identity. Additionally, the
	 * given key will behave the same across the OSGi implementations and
	 * provide weak reference semantics.
	 * 
	 * @param bundle OSGi bundle
	 * @return key generated for the given bundle
	 */
	private Object createKeyFor(Bundle bundle) {
		StringBuffer buffer = new StringBuffer();
		// add the bundle id first
		buffer.append(bundle.getBundleId());
		// followed by its update time (in hex to reduce its length)
		buffer.append(DELIMITER);
		buffer.append(Long.toHexString(bundle.getLastModified()));
		// plus the bundle class name (just to be triple sure)
		buffer.append(DELIMITER);
		buffer.append(bundle.getClass().getName());
		return buffer.toString();
	}

	private ClassLoader createBundleClassLoader(Bundle bundle) {
		return BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle);
	}
}
