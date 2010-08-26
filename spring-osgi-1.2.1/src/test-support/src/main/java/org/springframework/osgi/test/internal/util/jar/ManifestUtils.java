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
package org.springframework.osgi.test.internal.util.jar;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Utility class for creating Manifest objects on various criterions.
 * 
 * @author Costin Leau
 * 
 */
public abstract class ManifestUtils {

	/**
	 * Determine the Import-Package value based on the Export-Package entries in
	 * the jars given as Resources.
	 * @param resources
	 * @return
	 */
	public static String[] determineImportPackages(Resource[] resources) {
		Set collection = new LinkedHashSet();
		// for each resource
		for (int i = 0; i < resources.length; i++) {
			Resource resource = resources[i];
			Manifest man = JarUtils.getManifest(resource);
			if (man != null) {
				// read the manifest
				// get the Export-Package
				Attributes attrs = man.getMainAttributes();
				String exportedPackages = attrs.getValue(Constants.EXPORT_PACKAGE);
				// add it to the StringBuffer
				if (StringUtils.hasText(exportedPackages)) {
					collection.addAll(StringUtils.commaDelimitedListToSet(exportedPackages));
				}
			}
		}
		// return the result as string
		String[] array = (String[]) collection.toArray(new String[collection.size()]);

		// clean whitespace just in case
		for (int i = 0; i < array.length; i++) {
			array[i] = StringUtils.trimWhitespace(array[i]);
		}
		return array;
	}
}
