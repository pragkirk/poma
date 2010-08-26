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
package org.springframework.osgi.test.internal.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Loads a property file performing key expansion.
 * 
 * Provides simple property substitution, without support for inner or nested
 * placeholders. Also, the algorithm does only one parsing so derivative
 * placeholders are not supported.
 * 
 * 
 * @author Costin Leau
 * 
 */
public abstract class PropertiesUtil {

	private static final String DELIM_START = "${";

	private static final String DELIM_STOP = "}";

	private static final Properties EMPTY_PROPERTIES = new Properties();

	/**
	 * Shortcut method - loads a property object from the given input stream and
	 * applies property expansion.
	 * 
	 * @param resource
	 * @return
	 */
	public static Properties loadAndExpand(Resource resource) {
		Properties props = new Properties();
		if (resource == null)
			return props;

		try {
			props.load(resource.getInputStream());
		}
		catch (IOException ex) {
			return null;
		}
		return expandProperties(props);
	}

	/**
	 * Filter/Eliminate keys that start with the given prefix.
	 * 
	 * @param properties
	 * @param prefix
	 * @return
	 */
	public static Properties filterKeysStartingWith(Properties properties, String prefix) {
		if (!StringUtils.hasText(prefix))
			return EMPTY_PROPERTIES;

		Assert.notNull(properties);

		Properties excluded = new Properties();

		// filter ignore keys out
		for (Enumeration enm = properties.keys(); enm.hasMoreElements();) {
			String key = (String) enm.nextElement();
			if (key.startsWith(prefix)) {
				excluded.put(key, properties.remove(key));
			}
		}

		return excluded;
	}

	/**
	 * Filter/Eliminate keys that have a value that starts with the given
	 * prefix.
	 * 
	 * @param properties
	 * @param prefix
	 * @return
	 */
	public static Properties filterValuesStartingWith(Properties properties, String prefix) {
		if (!StringUtils.hasText(prefix))
			return EMPTY_PROPERTIES;

		Assert.notNull(properties);
		Properties excluded = new Properties();

		for (Enumeration enm = properties.keys(); enm.hasMoreElements();) {
			String key = (String) enm.nextElement();
			String value = properties.getProperty(key);
			if (value.startsWith(prefix)) {
				excluded.put(key, value);
				properties.remove(key);
			}
		}
		return excluded;
	}

	/**
	 * Apply placeholder expansion to the given properties object.
	 * 
	 * Will return a new properties object, containing the expanded entries.
	 * Note that both keys and values will be expanded.
	 * 
	 * @param props
	 * @return
	 */
	public static Properties expandProperties(Properties props) {
		Assert.notNull(props);

		Set entrySet = props.entrySet();

		Properties newProps = new Properties();

		for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
			// first expand the keys
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();

			String resultKey = expandProperty(key, props);
			String resultValue = expandProperty(value, props);

			// replace old entry

			newProps.put(resultKey, resultValue);
		}

		return newProps;
	}

	private static String expandProperty(String prop, Properties properties) throws IllegalArgumentException {

		boolean hasPlaceholder = false;
		String copy = prop;

		StringBuffer result = new StringBuffer();

		int index = 0;
		// dig out the placeholders
		do {
			index = copy.indexOf(DELIM_START);
			if (index >= 0) {
				hasPlaceholder = true;

				// add stuff before the delimiter
				result.append(copy.substring(0, index));
				// remove the delimiter
				copy = copy.substring(index + DELIM_START.length());
				// find ending delim
				int stopIndex = copy.indexOf(DELIM_STOP);
				String token = null;

				if (stopIndex >= 0) {
					// discover token
					token = copy.substring(0, stopIndex);
					// remove ending delimiter
					copy = copy.substring(stopIndex + 1);
					// append the replacement for the token
					result.append(properties.getProperty(token));
				}

				else
					throw new IllegalArgumentException("cannot interpret property " + prop + " due of token [" + copy
							+ "]");

			}
			else {
				hasPlaceholder = false;
				// make sure to append the remaining string
				result.append(copy);
			}

		} while (hasPlaceholder);

		return result.toString();
	}
}
