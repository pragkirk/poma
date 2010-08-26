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

package org.springframework.osgi.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

/**
 * Utility class for OSGi {@link Bundle}s. Provides convenience methods for
 * interacting with a Bundle object.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 * @see OsgiStringUtils
 */
public abstract class OsgiBundleUtils {

	/**
	 * Returns the underlying BundleContext for the given Bundle. This uses
	 * reflection and highly dependent of the OSGi implementation. Should not be
	 * used if OSGi 4.1 is being used.
	 * 
	 * @param bundle OSGi bundle
	 * @return the bundle context for this bundle
	 */
	public static BundleContext getBundleContext(final Bundle bundle) {
		if (bundle == null)
			return null;

		// try Equinox getContext
		Method meth = ReflectionUtils.findMethod(bundle.getClass(), "getContext", new Class[0]);

		// fallback to getBundleContext (OSGi 4.1)
		if (meth == null)
			meth = ReflectionUtils.findMethod(bundle.getClass(), "getBundleContext", new Class[0]);

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

	/**
	 * Indicates if the given bundle is active or not.
	 * 
	 * @param bundle OSGi bundle
	 * @return true if the given bundle is active, false otherwise
	 * @see Bundle#ACTIVE
	 */
	public static boolean isBundleActive(Bundle bundle) {
		Assert.notNull(bundle, "bundle is required");
		return (bundle.getState() == Bundle.ACTIVE);
	}

	/**
	 * Indicates if the given bundle is resolved or not.
	 * 
	 * @param bundle OSGi bundle
	 * @return true if the given bundle is resolved, false otherwise
	 * @see Bundle#RESOLVED
	 */
	public static boolean isBundleResolved(Bundle bundle) {
		Assert.notNull(bundle, "bundle is required");
		return (bundle.getState() >= Bundle.RESOLVED);
	}

	/**
	 * Indicates if the given bundle is a fragment or not.
	 * 
	 * @param bundle OSGi bundle
	 * @return true if the given bundle is a fragment, false otherwise
	 * @see Constants#FRAGMENT_HOST
	 */
	public static boolean isFragment(Bundle bundle) {
		Assert.notNull(bundle, "bundle is required");
		return bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null;
	}

	/**
	 * Indicates if the given bundle is the system bundle or not.
	 * 
	 * @param bundle OSGi bundle
	 * @return true if the given bundle is a fragment, false otherwise
	 */
	public static boolean isSystemBundle(Bundle bundle) {
		Assert.notNull(bundle);
		return (bundle.getBundleId() == 0);
	}

	/**
	 * Returns the given bundle version.
	 * 
	 * @param bundle OSGi bundle
	 * @return bundle version
	 * @see Constants#BUNDLE_VERSION
	 */
	public static Version getBundleVersion(Bundle bundle) {
		return getHeaderAsVersion(bundle, Constants.BUNDLE_VERSION);
	}

	/**
	 * Finds an install bundle based by its symbolic name.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @param symbolicName bundle symbolic name
	 * @return bundle matching the symbolic name (<code>null</code> if none
	 * is found)
	 */
	public static Bundle findBundleBySymbolicName(BundleContext bundleContext, String symbolicName) {
		Assert.notNull(bundleContext, "bundleContext is required");
		Assert.hasText(symbolicName, "a not-null/not-empty symbolicName isrequired");

		Bundle[] bundles = bundleContext.getBundles();
		for (int i = 0; i < bundles.length; i++) {
			if (symbolicName.equals(bundles[i].getSymbolicName())) {
				return bundles[i];
			}
		}
		return null;
	}

	/**
	 * Returns the version for a given bundle manifest header.
	 * 
	 * @param bundle OSGi bundle
	 * @param header bundle manifest header
	 * @return the header value
	 */
	public static Version getHeaderAsVersion(Bundle bundle, String header) {
		Assert.notNull(bundle);
		return Version.parseVersion((String) bundle.getHeaders().get(header));
	}
}
