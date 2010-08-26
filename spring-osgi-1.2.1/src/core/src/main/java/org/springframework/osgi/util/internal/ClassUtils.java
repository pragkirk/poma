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

package org.springframework.osgi.util.internal;

import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.core.JdkVersion;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * Class utility used internally. Contains mainly class inheritance mechanisms
 * used when creating OSGi service proxies.
 * 
 * @author Costin Leau
 * 
 */
public abstract class ClassUtils {

	private static class ReadOnlySetFromMap implements Set {

		private final Set keys;


		public ReadOnlySetFromMap(Map lookupMap) {
			keys = lookupMap.keySet();
		}

		public boolean add(Object o) {
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Collection c) {
			throw new UnsupportedOperationException();
		}

		public void clear() {
			throw new UnsupportedOperationException();
		}

		public boolean contains(Object o) {
			return keys.contains(o);
		}

		public boolean containsAll(Collection c) {
			return keys.containsAll(c);
		}

		public boolean isEmpty() {
			return keys.isEmpty();
		}

		public Iterator iterator() {
			return keys.iterator();
		}

		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		public boolean removeAll(Collection c) {
			throw new UnsupportedOperationException();
		}

		public boolean retainAll(Collection c) {
			throw new UnsupportedOperationException();
		}

		public int size() {
			return keys.size();
		}

		public Object[] toArray() {
			return keys.toArray();
		}

		public Object[] toArray(Object[] array) {
			return keys.toArray(array);
		}

		public String toString() {
			return keys.toString();
		}

		public int hashCode() {
			return keys.hashCode();
		}

		public boolean equals(Object o) {
			return o == this || keys.equals(o);
		}
	}


	/**
	 * Include only the interfaces inherited from superclasses or implemented by
	 * the current class.
	 */
	public static final int INCLUDE_INTERFACES = 1;

	/**
	 * Include only the class hierarchy (interfaces are excluded).
	 */
	public static final int INCLUDE_CLASS_HIERARCHY = 2;

	/**
	 * Include all inherited classes (classes or interfaces).
	 */
	public static final int INCLUDE_ALL_CLASSES = INCLUDE_INTERFACES | INCLUDE_CLASS_HIERARCHY;

	/** Whether the backport-concurrent library is present on the classpath */
	// the CollectionFactory classloader is used since this creates the map
	// internally
	private static final boolean backportConcurrentAvailable = org.springframework.util.ClassUtils.isPresent(
		"edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap", CollectionFactory.class.getClassLoader());

	/**
	 * List of special class loaders, outside OSGi, that might be used by the
	 * user through boot delegation. read-only.
	 */
	public static final List knownNonOsgiLoaders;

	/**
	 * Set of special class loaders, outside OSGi, that might be used by the
	 * user through boot delegation. read-only.
	 */
	public static final Set knownNonOsgiLoadersSet;

	// add the known, non-OSGi class loaders
	// note that the order is important
	static {
		// start with the framework class loader
		// then get all its parents (normally the this should be fwk -> (*) -> app -> ext -> boot)
		// where (*) represents some optional loaders for cases where the framework is embedded

		final Map lookupMap = CollectionFactory.createConcurrentMap(8);
		final List lookupList = Collections.synchronizedList(new ArrayList());

		AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {

				ClassLoader classLoader = getFwkClassLoader();
				addNonOsgiClassLoader(classLoader, lookupList, lookupMap);

				// get the system class loader
				classLoader = ClassLoader.getSystemClassLoader();
				addNonOsgiClassLoader(classLoader, lookupList, lookupMap);

				return null;
			}
		});

		// wrap the fields as read-only collections
		knownNonOsgiLoaders = Collections.unmodifiableList(lookupList);
		knownNonOsgiLoadersSet = new ReadOnlySetFromMap(lookupMap);

	}


	public static ClassLoader getFwkClassLoader() {
		return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				return Bundle.class.getClassLoader();
			}
		});
	}

	/**
	 * Special static method used during the class initialization.
	 * 
	 * @param classLoader non OSGi class loader
	 */
	private static void addNonOsgiClassLoader(ClassLoader classLoader, List list, Map map) {
		while (classLoader != null) {
			synchronized (list) {
				if (!map.containsKey(classLoader)) {
					list.add(classLoader);
					map.put(classLoader, Boolean.TRUE);
				}
			}
			classLoader = classLoader.getParent();
		}
	}


	/**
	 * Simple class loading abstraction working on both ClassLoader and Bundle
	 * classes.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private static class ClassLoaderBridge {

		private final Bundle bundle;

		private final ClassLoader classLoader;


		public ClassLoaderBridge(Bundle bundle) {
			Assert.notNull(bundle);
			this.bundle = bundle;
			this.classLoader = null;
		}

		public ClassLoaderBridge(ClassLoader classLoader) {
			Assert.notNull(classLoader);
			this.classLoader = classLoader;
			this.bundle = null;
		}

		public Class loadClass(String className) throws ClassNotFoundException {
			return (bundle == null ? classLoader.loadClass(className) : bundle.loadClass(className));
		}

		public boolean canSee(String className) {
			return (bundle == null ? org.springframework.util.ClassUtils.isPresent(className, classLoader) : isPresent(
				className, bundle));
		}
	}


	/**
	 * Return an array of parent classes for the given class. The mode paramater
	 * indicates whether only interfaces should be included, classes or both.
	 * 
	 * This method is normally used for publishing services and determing the
	 * {@link org.osgi.framework.Constants#OBJECTCLASS} property.
	 * 
	 * <p/> Note: this method does class expansion returning parent as well as
	 * children classes.
	 * 
	 * </p>
	 * 
	 * @see #INCLUDE_ALL_CLASSES
	 * @see #INCLUDE_CLASS_HIERARCHY
	 * @see #INCLUDE_INTERFACES
	 * 
	 * @param clazz
	 * @param mode
	 * 
	 * @return array of classes extended or implemented by the given class
	 */
	public static Class[] getClassHierarchy(Class clazz, int mode) {
		Class[] classes = null;

		if (clazz != null && isModeValid(mode)) {

			Set composingClasses = new LinkedHashSet();
			boolean includeClasses = includesMode(mode, INCLUDE_CLASS_HIERARCHY);
			boolean includeInterfaces = includesMode(mode, INCLUDE_INTERFACES);

			Class clz = clazz;
			do {
				if (includeClasses) {
					composingClasses.add(clz);
				}

				if (includeInterfaces) {
					CollectionUtils.mergeArrayIntoCollection(getAllInterfaces(clz), composingClasses);
				}

				clz = clz.getSuperclass();
			} while (clz != null && clz != Object.class);

			classes = (Class[]) composingClasses.toArray(new Class[composingClasses.size()]);
		}
		return (classes == null ? new Class[0] : classes);
	}

	/**
	 * Sugar method, determining the class hierarchy of a given class and then
	 * filtering based on the given classloader. If a null classloader, the one
	 * of the given class will be used.
	 * 
	 * @param clazz
	 * @param mode
	 * @param loader
	 * @return
	 */
	public static Class[] getVisibleClassHierarchy(Class clazz, int mode, ClassLoader loader) {
		if (clazz == null)
			return new Class[0];

		return getVisibleClasses(getClassHierarchy(clazz, mode), getClassLoader(clazz));
	}

	/**
	 * 'Sugar' method that determines the class hierarchy of the given class,
	 * returning only the classes visible to the given bundle.
	 * 
	 * @param clazz the class for which the hierarchy has to be determined
	 * @param mode discovery mode
	 * @param bundle bundle used for class visibility
	 * @return array of visible classes part of the hierarchy
	 */
	public static Class[] getVisibleClassHierarchy(Class clazz, int mode, Bundle bundle) {
		return getVisibleClasses(getClassHierarchy(clazz, mode), bundle);
	}

	/**
	 * Given an array of classes, eliminate the ones that cannot be loaded by
	 * the given classloader.
	 * 
	 * @return
	 */
	public static Class[] getVisibleClasses(Class[] classes, ClassLoader classLoader) {
		return getVisibleClasses(classes, new ClassLoaderBridge(classLoader));
	}

	/**
	 * Given an array of classes, eliminate the ones that cannot be loaded by
	 * the given bundle.
	 * 
	 * @return
	 */
	public static Class[] getVisibleClasses(Class[] classes, Bundle bundle) {
		return getVisibleClasses(classes, new ClassLoaderBridge(bundle));
	}

	private static Class[] getVisibleClasses(Class[] classes, ClassLoaderBridge loader) {
		if (ObjectUtils.isEmpty(classes))
			return classes;

		Set classSet = new LinkedHashSet(classes.length);
		CollectionUtils.mergeArrayIntoCollection(classes, classSet);

		// filter class collection based on visibility
		for (Iterator iter = classSet.iterator(); iter.hasNext();) {
			Class clzz = (Class) iter.next();
			if (!loader.canSee(clzz.getName())) {
				iter.remove();
			}
		}
		return (Class[]) classSet.toArray(new Class[classSet.size()]);
	}

	/**
	 * Get all interfaces implemented by the given class. This method returns
	 * both parent and children interfaces (i.e. Map and SortedMap).
	 * 
	 * @param clazz
	 * @return all interfaces implemented by the given class.
	 */
	public static Class[] getAllInterfaces(Class clazz) {
		Assert.notNull(clazz);
		return getAllInterfaces(clazz, new LinkedHashSet(8));
	}

	/**
	 * Recursive implementation for getting all interfaces.
	 * 
	 * @param clazz
	 * @param interfaces
	 * @return
	 */
	private static Class[] getAllInterfaces(Class clazz, Set interfaces) {
		Class[] intfs = clazz.getInterfaces();
		CollectionUtils.mergeArrayIntoCollection(intfs, interfaces);

		for (int i = 0; i < intfs.length; i++) {
			getAllInterfaces(intfs[i], interfaces);
		}

		return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
	}

	/**
	 * Check the present of a class inside a bundle. This method returns true if
	 * the given bundle can load the given class or false otherwise.
	 * 
	 * @param className
	 * @param bundle
	 * @return
	 */
	public static boolean isPresent(String className, Bundle bundle) {
		Assert.hasText(className);
		Assert.notNull(bundle);

		try {
			bundle.loadClass(className);
			return true;
		}
		catch (Exception cnfe) {
			return false;
		}
	}

	/**
	 * Return the classloader for the given class. This method deals with JDK
	 * classes which return by default, a null classloader.
	 * 
	 * @param clazz
	 * @return
	 */
	public static ClassLoader getClassLoader(Class clazz) {
		Assert.notNull(clazz);
		ClassLoader loader = clazz.getClassLoader();
		return (loader == null ? ClassLoader.getSystemClassLoader() : loader);
	}

	/**
	 * Test if testedMode includes an expected mode.
	 * 
	 * @param testedMode
	 * @param mode
	 * @return
	 */
	private static boolean includesMode(int testedMode, int mode) {
		return (testedMode & mode) == mode;
	}

	/**
	 * Test if a mode is valid.
	 * 
	 * @param mode
	 * @return
	 */
	private static boolean isModeValid(int mode) {
		return (mode >= INCLUDE_INTERFACES && mode <= INCLUDE_ALL_CLASSES);
	}

	/**
	 * Return an array of class string names for the given classes.
	 * 
	 * @param array
	 * @return
	 */
	public static String[] toStringArray(Class[] array) {
		if (ObjectUtils.isEmpty(array))
			return new String[0];

		String[] strings = new String[array.length];

		for (int i = 0; i < array.length; i++) {
			strings[i] = array[i].getName();
		}

		return strings;
	}

	/**
	 * Check the present of appropriate concurrent collection in the classpath.
	 * This means backport-concurrent on Java 1.4, or Java5+.
	 * 
	 * @return true if a ConcurrentHashMap is available on the classpath.
	 */
	public static boolean concurrentLibAvailable() {
		return (backportConcurrentAvailable || JdkVersion.isAtLeastJava15());
	}

	/**
	 * Determining if multiple classes(not interfaces) are specified, without
	 * any relation to each other. Interfaces will simply be ignored.
	 * 
	 * @param classes an array of classes
	 * @return true if at least two classes unrelated to each other are found,
	 *         false otherwise
	 */
	public static boolean containsUnrelatedClasses(Class[] classes) {
		if (ObjectUtils.isEmpty(classes))
			return false;

		Class clazz = null;
		// check if is more then one class specified
		for (int i = 0; i < classes.length; i++) {
			if (!classes[i].isInterface()) {
				if (clazz == null)
					clazz = classes[i];
				// check relationship
				else {
					if (clazz.isAssignableFrom(classes[i]))
						// clazz is a parent, switch with the child
						clazz = classes[i];
					else if (!classes[i].isAssignableFrom(clazz))
						return true;

				}
			}
		}

		// everything is in order
		return false;
	}

	/**
	 * Parse the given class array and eliminate parents of existing classes.
	 * Useful when creating proxies to minimize the number of implemented
	 * interfaces and redundant class information.
	 * 
	 * @see #containsUnrelatedClasses(Class[])
	 * @see #configureFactoryForClass(ProxyFactory, Class[])
	 * 
	 * @param classes array of classes
	 * @return a new array without superclasses
	 */
	public static Class[] removeParents(Class[] classes) {
		if (ObjectUtils.isEmpty(classes))
			return new Class[0];

		List clazz = new ArrayList(classes.length);
		for (int i = 0; i < classes.length; i++) {
			clazz.add(classes[i]);
		}

		// remove null elements
		while (clazz.remove(null)) {
		}

		// only one class is allowed
		// there can be multiple interfaces
		// parents of classes inside the array are removed

		boolean dirty;
		do {
			dirty = false;
			for (int i = 0; i < clazz.size(); i++) {
				Class currentClass = (Class) clazz.get(i);
				for (int j = 0; j < clazz.size(); j++) {
					if (i != j) {
						if (currentClass.isAssignableFrom((Class) clazz.get(j))) {
							clazz.remove(i);
							i--;
							dirty = true;
							break;
						}
					}
				}
			}
		} while (dirty);

		return (Class[]) clazz.toArray(new Class[clazz.size()]);
	}

	/**
	 * Based on the given class, properly instruct the ProxyFactory proxies. For
	 * additional sanity checks on the passed classes, check the methods below.
	 * 
	 * @see #containsUnrelatedClasses(Class[])
	 * @see #removeParents(Class[])
	 * 
	 * @param factory
	 * @param classes
	 */
	public static void configureFactoryForClass(ProxyFactory factory, Class[] classes) {
		if (ObjectUtils.isEmpty(classes))
			return;

		for (int i = 0; i < classes.length; i++) {
			Class clazz = classes[i];

			if (clazz.isInterface()) {
				factory.addInterface(clazz);
			}
			else {
				factory.setTargetClass(clazz);
				factory.setProxyTargetClass(true);
			}
		}
	}

	/**
	 * Load classes with the given name, using the given classloader.
	 * {@link ClassNotFoundException} exceptions are being ignored. The return
	 * class array will not contain duplicates.
	 * 
	 * @param classNames array of classnames to load (in FQN format)
	 * @param classLoader classloader used for loading the classes
	 * @return an array of classes (can be smaller then the array of class
	 *         names) w/o duplicates
	 */
	public static Class[] loadClasses(String[] classNames, ClassLoader classLoader) {
		if (ObjectUtils.isEmpty(classNames))
			return new Class[0];

		Assert.notNull(classLoader, "classLoader is required");
		Set classes = new LinkedHashSet(classNames.length);

		for (int i = 0; i < classNames.length; i++) {
			try {
				classes.add(classLoader.loadClass(classNames[i]));
			}
			catch (ClassNotFoundException ex) {
				// ignore
			}
		}

		return (Class[]) classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Exclude classes from the given array, which match the given modifier.
	 * 
	 * @see Modifier
	 * 
	 * @param classes array of classes (can be null)
	 * @param modifier class modifier
	 * @return array of classes (w/o duplicates) which does not have the given
	 *         modifier
	 */
	public static Class[] excludeClassesWithModifier(Class[] classes, int modifier) {
		if (ObjectUtils.isEmpty(classes))
			return new Class[0];

		Set clazzes = new LinkedHashSet(classes.length);

		for (int i = 0; i < classes.length; i++) {
			if ((modifier & classes[i].getModifiers()) == 0)
				clazzes.add(classes[i]);
		}
		return (Class[]) clazzes.toArray(new Class[clazzes.size()]);
	}

	/**
	 * Returns the first matching class from the given array, that doens't
	 * belong to common libraries such as the JDK or OSGi API. Useful for
	 * filtering OSGi services by type to prevent class cast problems.
	 * 
	 * <p/> No sanity checks are done on the given array class.
	 * 
	 * @param classes array of classes
	 * @return a 'particular' (non JDK/OSGi) class if one is found. Else the
	 *         first available entry is returned.
	 */
	public static Class getParticularClass(Class[] classes) {
		for (int i = 0; i < classes.length; i++) {
			Class clazz = classes[i];
			ClassLoader loader = clazz.getClassLoader();
			// quick boot/system check
			if (loader != null) {
				// consider known loaders 
				if (!knownNonOsgiLoadersSet.contains(loader)) {
					return clazz;
				}
			}
		}

		return classes[0];
	}
}