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

package org.springframework.osgi.io.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

/**
 * Simple utils class for the IO package. This method might contain util methods
 * from other packages since it the IO package needs to be stand-alone.
 * 
 * @author Costin Leau
 * 
 */
public abstract class OsgiUtils {

	private static final String GET_BUNDLE_CONTEXT_METHOD = "getBundleContext";
	private static final String GET_CONTEXT_METHOD = "getContext";


	public static String getPlatformName(BundleContext bundleContext) {
		String vendorProperty = bundleContext.getProperty(Constants.FRAMEWORK_VENDOR);
		String frameworkVersion = bundleContext.getProperty(Constants.FRAMEWORK_VERSION);

		// get system bundle
		Bundle bundle = bundleContext.getBundle(0);
		String name = (String) bundle.getHeaders().get(Constants.BUNDLE_NAME);
		String version = (String) bundle.getHeaders().get(Constants.BUNDLE_VERSION);
		String symName = bundle.getSymbolicName();

		StringBuffer buf = new StringBuffer();
		buf.append(name);
		buf.append(" ");
		buf.append(symName);
		buf.append("|");
		buf.append(version);
		buf.append("{");
		buf.append(frameworkVersion);
		buf.append(" ");
		buf.append(vendorProperty);
		buf.append("}");

		return buf.toString();
	}

	private static boolean isPlatformVendorMatch(BundleContext bundleContext, String vendorString) {
		String vendor = bundleContext.getProperty(Constants.FRAMEWORK_VENDOR);
		if (vendor != null)
			return vendor.indexOf(vendorString) >= -1;
		return false;
	}

	private static boolean isEquinox(BundleContext bundleContext) {
		return isPlatformVendorMatch(bundleContext, "clispe");
	}

	private static boolean isKnopflerfish(BundleContext bundleContext) {
		return isPlatformVendorMatch(bundleContext, "fish");
	}

	private static boolean isFelix(BundleContext bundleContext) {
		return isPlatformVendorMatch(bundleContext, "pache");
	}

	/**
	 * Returns the underlying BundleContext for the given Bundle. This uses
	 * reflection and highly dependent of the OSGi implementation. Should not be
	 * used if OSGi 4.1 is being used.
	 * 
	 * <b>Note:</b> Identical to the util found in Spring-DM core
	 * 
	 * @param bundle OSGi bundle
	 * @return the bundle context for this bundle
	 */
	public static BundleContext getBundleContext(final Bundle bundle) {
		if (bundle == null)
			return null;

		// run into a privileged block
		if (System.getSecurityManager() != null) {
			return (BundleContext) AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					return getBundleContextWithPrivileges(bundle);
				}
			});
		}
		else {
			return getBundleContextWithPrivileges(bundle);
		}
	}

	private static BundleContext getBundleContextWithPrivileges(final Bundle bundle) {
		// try Equinox getContext
		Method meth = ReflectionUtils.findMethod(bundle.getClass(), GET_CONTEXT_METHOD, new Class[0]);

		// fallback to getBundleContext (OSGi 4.1)
		if (meth == null)
			meth = ReflectionUtils.findMethod(bundle.getClass(), GET_BUNDLE_CONTEXT_METHOD, new Class[0]);

		final Method m = meth;

		if (meth != null) {
			ReflectionUtils.makeAccessible(meth);
			return (BundleContext) ReflectionUtils.invokeMethod(m, bundle);
		}

		// fallback to field inspection (KF and Prosyst)
		final BundleContext[] ctx = new BundleContext[1];

		ReflectionUtils.doWithFields(bundle.getClass(), new FieldCallback() {

			public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
				ReflectionUtils.makeAccessible(field);
				ctx[0] = (BundleContext) field.get(bundle);
			}
		}, new FieldFilter() {

			public boolean matches(Field field) {
				return BundleContext.class.isAssignableFrom(field.getType());
			}
		});

		return ctx[0];
	}
}
