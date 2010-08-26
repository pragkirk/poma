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

package org.springframework.osgi.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.osgi.framework.Filter;

/**
 * FrameworkUtil-like class that tries to create a somewhat valid filter.
 * 
 * Filters objects can be created without an actual OSGi platform running
 * however, the default OSGi implementation delegates the creation to the
 * package indicated by "org.osgi.vendor.framework" property.
 * 
 * In its current implementation, this class requires one of Equinox,
 * Knoplerfish or Felix on its classpath to create the filter object.
 * 
 * 
 * @author Costin Leau
 */
public class MockFrameworkUtil {

	private static final String EQUINOX_CLS = "org.eclipse.osgi.framework.internal.core.FilterImpl";

	private static final String KF_CLS = "org.knopflerfish.framework.FilterImpl";

	private static final String FELIX_CLS = "org.apache.felix.framework.FilterImpl";

	private final Constructor filterConstructor;


	/**
	 * Constructs a new <code>MockFrameworkUtil</code> instance.
	 * 
	 * As opposed to the OSGi approach this class doesn't use statics since it
	 * makes configuration and initialization a lot harder without any
	 * particular benefit.
	 * 
	 */
	MockFrameworkUtil() {
		// detect filter implementation
		ClassLoader cl = getClass().getClassLoader();
		Class filterClz = null;
		// try Equinox
		filterClz = loadClass(cl, EQUINOX_CLS);
		// try KF
		if (filterClz == null)
			filterClz = loadClass(cl, KF_CLS);
		// try Felix
		if (filterClz == null)
			filterClz = loadClass(cl, FELIX_CLS);

		if (filterClz == null)
			// nothing is found, bail out
			throw new IllegalStateException("cannot find Equinox, Knopflerfish or Felix on the classpath");

		try {
			filterConstructor = filterClz.getConstructor(new Class[] { String.class });
		}
		catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("found invalid filter class " + filterClz);
		}
	}

	private Class loadClass(ClassLoader loader, String className) {
		try {
			return loader.loadClass(className);
		}
		catch (ClassNotFoundException e) {
			// swallow exception
		}

		return null;
	}

	/**
	 * Create a mock filter that is _might_ be valid. This method does not throw
	 * an checked exception and will always return a filter implementation.
	 * 
	 * @param filter OSGi filter given as a String.
	 * @return actual OSGi filter using the underlying OSGi platform
	 */
	public Filter createFilter(String filter) {
		try {
			return (Filter) filterConstructor.newInstance(new Object[] { filter });
		}
		catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}