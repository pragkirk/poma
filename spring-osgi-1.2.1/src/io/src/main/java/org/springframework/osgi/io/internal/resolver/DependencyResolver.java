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

package org.springframework.osgi.io.internal.resolver;

import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Simple interface offering utility methods for OSGi dependencies, mainly
 * bundles. This class suplements the {@link PackageAdmin} service by offering
 * information on importing, not just exporting.
 * 
 * @author Costin Leau
 */
public interface DependencyResolver {

	/**
	 * Returns the bundles imported by the given bundle. It's up to the
	 * implementation to consider required bundles, bundle class-path and
	 * dynamic imports.
	 * 
	 * <p/> The returned array should not contain duplicates (each imported
	 * bundle should be present exactly once).
	 * 
	 * <p/> In general it is not expected to have knowledge about runtime
	 * loading (such as dynamic imports).
	 * 
	 * @param bundle OSGi bundle for which imported bundles will be determined
	 * @return a not-null array of importing bundles
	 */
	ImportedBundle[] getImportedBundles(Bundle bundle);
}
