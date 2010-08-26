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

package org.springframework.osgi.samples.console.service;

import java.util.Collection;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * Contract for a simple, OSGi "console". Provides a more convenient and simpler
 * API for interacting with the OSGi framework.
 * 
 * @author Costin Leau
 */
public interface OsgiConsole {

	/**
	 * Returns the id of the console bundle. Used as starting point for the
	 * console analysis.
	 * 
	 * @return default bundle id
	 */
	long getDefaultBundleId();

	/**
	 * Returns an array containing the bundles currently installed in the OSGi
	 * platform.
	 * 
	 * @return array of bundles
	 */
	Bundle[] listBundles();

	/**
	 * Returns the {@link Bundle} object associated with the given id.
	 * 
	 * @param bundleId bundle id
	 * @return bundle with the given id
	 */
	Bundle getBundle(long bundleId);

	/**
	 * Returns the packages exported by the given bundle, as an array of
	 * strings.
	 * 
	 * @param bundle OSGi bundle
	 * @return array of exported packages
	 */
	String[] getExportedPackages(Bundle bundle);

	/**
	 * Returns the packages imported by the given bundle, as an array of
	 * Strings.
	 * 
	 * @param bundle OSGi bundle
	 * @return array of imported packages
	 */
	String[] getImportedPackages(Bundle bundle);

	/**
	 * Returns references to the OSGi services registered by the given bundle.
	 * 
	 * @param bundle OSGi bundle
	 * @return references to the services registered by the given bundle
	 */
	ServiceReference[] getRegisteredServices(Bundle bundle);

	/**
	 * Returns references to the OSGi services used by the given bundle.
	 * 
	 * @param bundle OSGi bundle
	 * @return references to the services registered by the given bundle
	 */
	ServiceReference[] getServicesInUse(Bundle bundle);

	/**
	 * Returns a collection of paths, contained by the given bundle, that match
	 * the given pattern. If the pattern cannot be resolved, a <tt>null</tt>
	 * collection is returned.
	 * 
	 * @param bundle OSGi bundle
	 * @param pattern path search pattern
	 * @return collection of matching paths or null if the given pattern is
	 * invalid
	 */
	Collection<String> search(Bundle bundle, String pattern);
}
