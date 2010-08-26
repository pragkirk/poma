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

import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.osgi.io.OsgiBundleResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Utility class used for IO resources.
 * 
 * @author Costin Leau
 * 
 */
public abstract class OsgiResourceUtils {

	public static final String EMPTY_PREFIX = "";

	public static final String PREFIX_DELIMITER = ":";

	public static final String FOLDER_DELIMITER = "/";

	// PREFIXES TYPES

	// non-osgi prefixes (file, http)
	public static final int PREFIX_TYPE_UNKNOWN = -1;

	// no prefix
	public static final int PREFIX_TYPE_NOT_SPECIFIED = 0x00000000;

	// osgibundlejar:
	public static final int PREFIX_TYPE_BUNDLE_JAR = 0x00000001;

	// osgibundle:
	public static final int PREFIX_TYPE_BUNDLE_SPACE = 0x00000010;

	// classpath:
	public static final int PREFIX_TYPE_CLASS_SPACE = 0x00000100;

	// classpath*:
	public static final int PREFIX_TYPE_CLASS_ALL_SPACE = 0x00000200;


	/**
	 * Return the path prefix if there is any or {@link #EMPTY_PREFIX}
	 * otherwise.
	 * 
	 * @param path
	 * @return
	 */
	public static String getPrefix(String path) {
		if (path == null)
			return EMPTY_PREFIX;
		int index = path.indexOf(PREFIX_DELIMITER);
		return ((index > 0) ? path.substring(0, index + 1) : EMPTY_PREFIX);
	}

	/**
	 * Return the search type to be used for the give string based on the
	 * prefix.
	 * 
	 * @param path
	 * @return
	 */
	public static int getSearchType(String path) {
		Assert.notNull(path);
		int type = PREFIX_TYPE_NOT_SPECIFIED;
		String prefix = getPrefix(path);

		// no prefix is treated just like osgibundle:
		if (!StringUtils.hasText(prefix))
			type = PREFIX_TYPE_NOT_SPECIFIED;
		else if (prefix.startsWith(OsgiBundleResource.BUNDLE_URL_PREFIX))
			type = PREFIX_TYPE_BUNDLE_SPACE;
		else if (prefix.startsWith(OsgiBundleResource.BUNDLE_JAR_URL_PREFIX))
			type = PREFIX_TYPE_BUNDLE_JAR;
		else if (prefix.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX))
			type = PREFIX_TYPE_CLASS_SPACE;
		else if (prefix.startsWith(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX))
			type = PREFIX_TYPE_CLASS_ALL_SPACE;

		else
			type = PREFIX_TYPE_UNKNOWN;

		return type;
	}

	public static boolean isClassPathType(int type) {
		return (type == PREFIX_TYPE_CLASS_SPACE || type == PREFIX_TYPE_CLASS_ALL_SPACE);
	}

	public static String stripPrefix(String path) {
		// strip prefix
		int index = path.indexOf(PREFIX_DELIMITER);
		return (index > -1 ? path.substring(index + 1) : path);

	}

	public static Resource[] convertURLArraytoResourceArray(URL[] urls) {
		if (urls == null) {
			return new Resource[0];
		}

		// convert this into a resource array
		Resource[] res = new Resource[urls.length];
		for (int i = 0; i < urls.length; i++) {
			res[i] = new UrlResource(urls[i]);
		}
		return res;
	}

	public static Resource[] convertURLEnumerationToResourceArray(Enumeration enm) {
		Set resources = new LinkedHashSet(4);
		while (enm != null && enm.hasMoreElements()) {
			resources.add(new UrlResource((URL) enm.nextElement()));
		}
		return (Resource[]) resources.toArray(new Resource[resources.size()]);
	}

	/**
	 * Similar to /path/path1/ -> /path/, /path/file -> /path/
	 * 
	 * @return
	 */
	public static String findUpperFolder(String path) {
		if (path.length() < 2)
			return path;

		String newPath = path;
		// if it's a folder
		if (path.endsWith(FOLDER_DELIMITER)) {
			newPath = path.substring(0, path.length() - 1);
		}

		int index = newPath.lastIndexOf(FOLDER_DELIMITER);
		if (index > 0)
			return newPath.substring(0, index + 1);

		else
			// fallback to defaults
			return path;
	}
}
