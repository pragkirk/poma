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
 *
 */

package org.springframework.osgi.util;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.osgi.framework.Bundle;
import org.springframework.util.Assert;

/**
 * ClassLoader backed by an OSGi bundle. Provides the ability to use a separate
 * class loader as fall back.
 * 
 * Contains facilities for tracing class loading behaviour so that issues can be
 * easily resolved.
 * 
 * For debugging please see {@link DebugUtils}.
 * 
 * @author Adrian Colyer
 * @author Andy Piper
 * @author Costin Leau
 */
public class BundleDelegatingClassLoader extends ClassLoader {

	/** use degradable logger */
	private static final Log log = LogUtils.createLogger(BundleDelegatingClassLoader.class);

	private final ClassLoader bridge;

	private final Bundle backingBundle;


	/**
	 * Factory method for creating a class loader over the given bundle.
	 * 
	 * @param aBundle bundle to use for class loading and resource acquisition
	 * @return class loader adapter over the given bundle
	 */
	public static BundleDelegatingClassLoader createBundleClassLoaderFor(Bundle aBundle) {
		return createBundleClassLoaderFor(aBundle, null);
	}

	/**
	 * Factory method for creating a class loader over the given bundle and with
	 * a given class loader as fall-back. In case the bundle cannot find a class
	 * or locate a resource, the given class loader will be used as fall back.
	 * 
	 * @param bundle bundle used for class loading and resource acquisition
	 * @param bridge class loader used as fall back in case the bundle cannot
	 * load a class or find a resource. Can be <code>null</code>
	 * @return class loader adapter over the given bundle and class loader
	 */
	public static BundleDelegatingClassLoader createBundleClassLoaderFor(final Bundle bundle, final ClassLoader bridge) {
		return (BundleDelegatingClassLoader) AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				return new BundleDelegatingClassLoader(bundle, bridge);
			}
		});
	}

	/**
	 * Private constructor.
	 * 
	 * Constructs a new <code>BundleDelegatingClassLoader</code> instance.
	 * 
	 * @param bundle
	 * @param bridgeLoader
	 */
	protected BundleDelegatingClassLoader(Bundle bundle, ClassLoader bridgeLoader) {
		super(null);
		Assert.notNull(bundle, "bundle should be non-null");
		this.backingBundle = bundle;
		this.bridge = bridgeLoader;
	}

	protected Class findClass(String name) throws ClassNotFoundException {
		try {
			return this.backingBundle.loadClass(name);
		}
		catch (ClassNotFoundException cnfe) {
			DebugUtils.debugClassLoading(backingBundle, name, null);
			throw new ClassNotFoundException(name + " not found from bundle [" + backingBundle.getSymbolicName() + "]",
				cnfe);
		}
		catch (NoClassDefFoundError ncdfe) {
			// This is almost always an error
			// This is caused by a dependent class failure,
			// so make sure we search for the right one.
			String cname = ncdfe.getMessage().replace('/', '.');
			DebugUtils.debugClassLoading(backingBundle, cname, name);
			NoClassDefFoundError e = new NoClassDefFoundError(name + " not found from bundle ["
					+ OsgiStringUtils.nullSafeNameAndSymName(backingBundle) + "]");
			e.initCause(ncdfe);
			throw e;
		}
	}

	protected URL findResource(String name) {
		boolean trace = log.isTraceEnabled();

		if (trace)
			log.trace("Looking for resource " + name);
		URL url = this.backingBundle.getResource(name);

		if (trace && url != null)
			log.trace("Found resource " + name + " at " + url);
		return url;
	}

	protected Enumeration findResources(String name) throws IOException {
		boolean trace = log.isTraceEnabled();

		if (trace)
			log.trace("Looking for resources " + name);

		Enumeration enm = this.backingBundle.getResources(name);

		if (trace && enm != null && enm.hasMoreElements())
			log.trace("Found resource " + name + " at " + this.backingBundle.getLocation());

		return enm;
	}

	public URL getResource(String name) {
		URL resource = findResource(name);
		if (bridge != null && resource == null) {
			resource = bridge.getResource(name);
		}
		return resource;
	}

	protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class clazz = null;
		try {
			clazz = findClass(name);
		}
		catch (ClassNotFoundException cnfe) {
			if (bridge != null)
				clazz = bridge.loadClass(name);
			else
				throw cnfe;
		}
		if (resolve) {
			resolveClass(clazz);
		}
		return clazz;
	}

	public String toString() {
		return "BundleDelegatingClassLoader for [" + OsgiStringUtils.nullSafeNameAndSymName(backingBundle) + "]";
	}

	/**
	 * Returns the bundle to which this class loader delegates calls to.
	 * 
	 * @return the backing bundle
	 */
	public Bundle getBundle() {
		return backingBundle;
	}

}
