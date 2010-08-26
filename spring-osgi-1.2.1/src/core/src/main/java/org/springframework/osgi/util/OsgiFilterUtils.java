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

import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Utility class for creating OSGi filters. This class allows filter creation
 * and concatenation from common parameters such as class names.
 * 
 * @author Costin Leau
 */
public abstract class OsgiFilterUtils {

	private static final char FILTER_BEGIN = '(';

	private static final char FILTER_END = ')';

	private static final String FILTER_AND_CONSTRAINT = "(&";

	private static final String EQUALS = "=";


	/**
	 * Adds the given class as an 'and'(&amp;) {@link Constants#OBJECTCLASS}
	 * constraint to the given filter. At least one parameter must be valid
	 * (non-<code>null</code>).
	 * 
	 * @param clazz class name (can be <code>null</code>)
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return OSGi filter containing the {@link Constants#OBJECTCLASS}
	 * constraint and the given filter
	 */
	public static String unifyFilter(String clazz, String filter) {
		return unifyFilter(new String[] { clazz }, filter);
	}

	/**
	 * Adds the given class to the given filter. At least one parameter must be
	 * valid (non-<code>null</code>).
	 * 
	 * @param clazz fully qualified class name (can be <code>null</code>)
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return an OSGi filter concatenating the given parameters
	 * @see #unifyFilter(String, String)
	 */
	public static String unifyFilter(Class clazz, String filter) {
		if (clazz != null)
			return unifyFilter(clazz.getName(), filter);
		return unifyFilter((String) null, filter);
	}

	/**
	 * Adds the given classes to the given filter. At least one parameter must
	 * be valid (non-<code>null</code>).
	 * 
	 * @param classes array of fully qualified class names (can be
	 * <code>null</code>/empty)
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return an OSGi filter concatenating the given parameters
	 * @see #unifyFilter(String[], String)
	 */
	public static String unifyFilter(Class[] classes, String filter) {
		if (ObjectUtils.isEmpty(classes))
			return unifyFilter(new String[0], filter);

		String classNames[] = new String[classes.length];
		for (int i = 0; i < classNames.length; i++) {
			if (classes[i] != null)
				classNames[i] = classes[i].getName();
		}
		return unifyFilter(classNames, filter);
	}

	/**
	 * Adds the given classes as an 'and'(&amp;) {@link Constants#OBJECTCLASS}
	 * constraint to the given filter. At least one parameter must be valid
	 * (non-<code>null</code>).
	 * 
	 * @param classes array of fully qualified class names (can be
	 * <code>null</code>/empty)
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return an OSGi filter concatenating the given parameters
	 */
	public static String unifyFilter(String[] classes, String filter) {
		return unifyFilter(Constants.OBJECTCLASS, classes, filter);
	}

	/**
	 * Concatenates the given strings with an 'and'(&amp;) constraint under the
	 * given key to the given filter. At least one of the items/filter
	 * parameters must be valid (non-<code>null</code>).
	 * 
	 * @param key the key under which the items are being concatenated
	 * (required)
	 * @param items an array of strings concatenated to the existing filter
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return an OSGi filter concatenating the given parameters
	 */
	public static String unifyFilter(String key, String[] items, String filter) {
		boolean filterHasText = StringUtils.hasText(filter);

		if (items == null)
			items = new String[0];

		// number of valid (not-null) classes
		int itemName = items.length;

		for (int i = 0; i < items.length; i++) {
			if (items[i] == null)
				itemName--;
		}

		if (itemName == 0)
			// just return the filter
			if (filterHasText)
				return filter;
			else
				throw new IllegalArgumentException("at least one parameter has to be not-null");

		Assert.hasText(key, "key is required");

		// do a simple filter check - starts with ( and ends with )
		if (filterHasText && !(filter.charAt(0) == FILTER_BEGIN && filter.charAt(filter.length() - 1) == FILTER_END)) {
			throw new IllegalArgumentException("invalid filter: " + filter);
		}

		// the item will be added in a sub-filter which does searching only
		// after the key. For classes these will look like:
		// 
		// i.e.
		// (&(objectClass=java.lang.Object)(objectClass=java.lang.Cloneable))
		//
		// this sub filter will be added with a & constraint to the given filter
		// if
		// that one exists
		// i.e. (&(&(objectClass=MegaObject)(objectClass=SuperObject))(<given
		// filter>))

		StringBuffer buffer = new StringBuffer();

		// a. big & constraint
		// (&
		if (filterHasText)
			buffer.append(FILTER_AND_CONSTRAINT);

		boolean moreThenOneClass = itemName > 1;

		// b. create key sub filter (only if we have more then one class
		// (&(&
		if (moreThenOneClass) {
			buffer.append(FILTER_AND_CONSTRAINT);
		}

		// parse the classes and add the item name under the given key
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				// (objectClass=
				buffer.append(FILTER_BEGIN);
				buffer.append(key);
				buffer.append(EQUALS);
				// <actual value>
				buffer.append(items[i]);
				// )
				buffer.append(FILTER_END);
			}
		}

		// c. close the classes sub filter
		// )
		if (moreThenOneClass) {
			buffer.append(FILTER_END);
		}

		// d. add the rest of the filter
		if (filterHasText) {
			buffer.append(filter);
			// e. close the big filter
			buffer.append(FILTER_END);
		}

		return buffer.toString();

	}

	/**
	 * Validates the given String as a OSGi filter.
	 * 
	 * @param filter OSGi filter
	 * @return true if the filter is valid, false otherwise
	 */
	public static boolean isValidFilter(String filter) {
		try {
			createFilter(filter);
			return true;
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}

	/**
	 * Creates an OSGi {@link Filter} from the given String. Translates the
	 * {@link InvalidSyntaxException} checked exception into an unchecked
	 * {@link IllegalArgumentException}.
	 * 
	 * @param filter OSGi filter given as a String
	 * @return OSGi filter (as <code>Filter</code>)
	 */
	public static Filter createFilter(String filter) {
		Assert.hasText(filter, "invalid filter");
		try {
			return FrameworkUtil.createFilter(filter);
		}
		catch (InvalidSyntaxException ise) {
			throw (RuntimeException) new IllegalArgumentException("invalid filter: " + ise.getFilter()).initCause(ise);
		}
	}
}