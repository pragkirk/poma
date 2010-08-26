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
import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.aop.framework.ProxyFactory;

/**
 * Default implementation for {@link InternalAopClassLoaderFactory}. Uses an
 * internal {@link WeakHashMap} to cache aop class loaders to prevent duplicated
 * copies.
 * 
 * @author Costin Leau
 */
class CachingAopClassLoaderFactory implements InternalAopClassLoaderFactory {

	private static final String CGLIB_CLASS = "net.sf.cglib.proxy.Enhancer";
	/** CGLIB class (if it's present) */
	private final Class cglibClass;

	/** class loader -> aop class loader cache */
	private final Map cache = new WeakHashMap();


	CachingAopClassLoaderFactory() {
		// load CGLIB through Spring-AOP
		ClassLoader springAopClassLoader = ProxyFactory.class.getClassLoader();
		Class clazz = null;
		try {
			clazz = springAopClassLoader.loadClass(CGLIB_CLASS);
		}
		catch (ClassNotFoundException cnfe) {
			// assume cglib is not present
		}
		cglibClass = clazz;
	}

	public ChainedClassLoader createClassLoader(ClassLoader classLoader) {
		// search key (should be fast as the default classloader (BundleDelegatingClassLoader) has identity equality/hashcode)
		synchronized (cache) {
			ChainedClassLoader aopClassLoader = null;
			WeakReference loaderReference = (WeakReference) cache.get(classLoader);

			if (loaderReference != null) {
				aopClassLoader = (ChainedClassLoader) loaderReference.get();
			}

			// no associated class loader found, create one and put it in the cache
			if (aopClassLoader == null) {
				// use the given class loader, spring-aop, cglib (if available) and then spring-dm core class loader (for its infrastructure interfaces)
				if (cglibClass != null) {
					aopClassLoader = new ChainedClassLoader(new ClassLoader[] { classLoader,
						ProxyFactory.class.getClassLoader(), cglibClass.getClassLoader(),
						CachingAopClassLoaderFactory.class.getClassLoader() });
				}
				else {
					aopClassLoader = new ChainedClassLoader(new ClassLoader[] { classLoader,
						ProxyFactory.class.getClassLoader(), CachingAopClassLoaderFactory.class.getClassLoader() });
				}

				// save the class loader as a weak reference (since it refers to the given class loader)
				cache.put(classLoader, new WeakReference(aopClassLoader));
			}
			return aopClassLoader;
		}
	}
}
