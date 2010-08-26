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

import org.osgi.framework.Bundle;
import org.springframework.osgi.util.BundleDelegatingClassLoader;
import org.springframework.util.Assert;

/**
 * Simple factory for generating Bundle/AOP-suitable class loaders used
 * internally by Spring-DM for generating proxies. The factory acts as a generic
 * facade for framework components hiding the implementation details (or the
 * changes in strategy).
 * 
 * <p/> Internally the factory will try to use a cache to avoid creating
 * unneeded class loader (even if lightweight) to avoid polluting the JDK/CGLIB
 * class loader maps.
 * 
 * @author Costin Leau
 */
public abstract class ClassLoaderFactory {

	/** plug-able, private, class loader factory */
	private static InternalAopClassLoaderFactory aopClassLoaderFactory = new CachingAopClassLoaderFactory();
	/** plug-able, private, bundle loader factory */
	private static BundleClassLoaderFactory bundleClassLoaderFactory = new CachingBundleClassLoaderFactory();


	/**
	 * Returns the standard, extended AOP class loader based on the given class
	 * loader.
	 * 
	 * @param classLoader base class loader
	 * @return AOP class loader created using the given argument
	 */
	public static ChainedClassLoader getAopClassLoaderFor(ClassLoader classLoader) {
		Assert.notNull(classLoader);
		return aopClassLoaderFactory.createClassLoader(classLoader);
	}

	/**
	 * Returns the wrapped class loader for the given bundle.
	 * 
	 * <p/> This method is similar to {@link #getAopClassLoaderFor(ClassLoader)}
	 * but considers the {@link BundleDelegatingClassLoader} associated with a
	 * bundle. Namely, the implementation will check if there is a wrapping
	 * class loader associated with the given bundle, creating one if none if
	 * found.
	 * 
	 * <p/> Useful when creating importers/exporters programmatically.
	 * 
	 * @param bundle OSGi bundle
	 * @return associated wrapping class loader
	 */
	public static ClassLoader getBundleClassLoaderFor(Bundle bundle) {
		Assert.notNull(bundle);
		return bundleClassLoaderFactory.createClassLoader(bundle);
	}
}
