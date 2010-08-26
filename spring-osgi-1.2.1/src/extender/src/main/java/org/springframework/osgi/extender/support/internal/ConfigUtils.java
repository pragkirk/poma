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

package org.springframework.osgi.extender.support.internal;

import java.util.Dictionary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Utility class for dealing with the extender configuration and OSGi bundle
 * manifest headers.
 * 
 * Defines Spring/OSGi constants and methods for configuring Spring application
 * context.
 * 
 * @author Costin Leau
 * 
 */
public abstract class ConfigUtils {

	private static final Log log = LogFactory.getLog(ConfigUtils.class);

	public static final String EXTENDER_VERSION = "SpringExtender-Version";

	private static final String LEFT_CLOSED_INTERVAL = "[";

	private static final String LEFT_OPEN_INTERVAL = "(";

	private static final String RIGHT_CLOSED_INTERVAL = "]";

	private static final String RIGHT_OPEN_INTERVAL = ")";

	private static final String COMMA = ",";

	public static final String CONFIG_WILDCARD = "*";

	/**
	 * Manifest entry name for configuring Spring application context.
	 */
	public static final String SPRING_CONTEXT_HEADER = "Spring-Context";

	/**
	 * Directive for publishing Spring application context as a service.
	 */
	public static final String DIRECTIVE_PUBLISH_CONTEXT = "publish-context";

	/**
	 * Directive for indicating wait-for time when satisfying mandatory
	 * dependencies defined in seconds
	 */
	public static final String DIRECTIVE_TIMEOUT = "timeout";

	public static final String DIRECTIVE_TIMEOUT_VALUE_NONE = "none";

	/**
	 * Create asynchronously directive.
	 */
	public static final String DIRECTIVE_CREATE_ASYNCHRONOUSLY = "create-asynchronously";

	/**
	 * Wait for dependencies or directly start the context.
	 */
	public static final String DIRECTIVE_WAIT_FOR_DEPS = "wait-for-dependencies";

	/**
	 * {@link #DIRECTIVE_WAIT_FOR_DEPS} default.
	 */
	public static final boolean DIRECTIVE_WAIT_FOR_DEPS_DEFAULT = true;

	public static final String EQUALS = ":=";

	/**
	 * Token used for separating directives inside a header.
	 */
	public static final String DIRECTIVE_SEPARATOR = ";";

	public static final String CONTEXT_LOCATION_SEPARATOR = ",";

	public static final boolean DIRECTIVE_PUBLISH_CONTEXT_DEFAULT = true;

	public static final boolean DIRECTIVE_CREATE_ASYNCHRONOUSLY_DEFAULT = true;

	public static final long DIRECTIVE_TIMEOUT_DEFAULT = 5 * 60; // 5 minutes

	public static final long DIRECTIVE_NO_TIMEOUT = -2L; // Indicates wait forever


	public static boolean matchExtenderVersionRange(Bundle bundle, Version versionToMatch) {
		Assert.notNull(bundle);
		// get version range
		String range = (String) bundle.getHeaders().get(EXTENDER_VERSION);

		boolean trace = log.isTraceEnabled();

		// empty value = empty version = *
		if (!StringUtils.hasText(range))
			return true;

		if (trace)
			log.trace("discovered " + EXTENDER_VERSION + " header w/ value=" + range);

		// do we have a range or not ?
		range = StringUtils.trimWhitespace(range);

		// a range means one comma
		int commaNr = StringUtils.countOccurrencesOf(range, COMMA);

		// no comma, no intervals
		if (commaNr == 0) {
			Version version = Version.parseVersion(range);

			return versionToMatch.equals(version);
		}

		if (commaNr == 1) {

			// sanity check
			if (!((range.startsWith(LEFT_CLOSED_INTERVAL) || range.startsWith(LEFT_OPEN_INTERVAL)) && (range.endsWith(RIGHT_CLOSED_INTERVAL) || range.endsWith(RIGHT_OPEN_INTERVAL)))) {
				throw new IllegalArgumentException("range [" + range + "] is invalid");
			}

			boolean equalMin = range.startsWith(LEFT_CLOSED_INTERVAL);
			boolean equalMax = range.endsWith(RIGHT_CLOSED_INTERVAL);

			// remove interval brackets
			range = range.substring(1, range.length() - 1);

			// split the remaining string in two pieces
			String[] pieces = StringUtils.split(range, COMMA);

			if (trace)
				log.trace("discovered low/high versions : " + ObjectUtils.nullSafeToString(pieces));

			Version minVer = Version.parseVersion(pieces[0]);
			Version maxVer = Version.parseVersion(pieces[1]);

			if (trace)
				log.trace("comparing version " + versionToMatch + " w/ min=" + minVer + " and max=" + maxVer);

			boolean result = true;

			int compareMin = versionToMatch.compareTo(minVer);

			if (equalMin)
				result = (result && (compareMin >= 0));
			else
				result = (result && (compareMin > 0));

			int compareMax = versionToMatch.compareTo(maxVer);

			if (equalMax)
				result = (result && (compareMax <= 0));
			else
				result = (result && (compareMax < 0));

			return result;
		}

		// more then one comma means incorrect range

		throw new IllegalArgumentException("range [" + range + "] is invalid");
	}

	/**
	 * Return the {@value #SPRING_CONTEXT_HEADER} if present from the given
	 * dictionary.
	 * 
	 * @param headers
	 * @return
	 */
	public static String getSpringContextHeader(Dictionary headers) {
		Object header = null;
		if (headers != null)
			header = headers.get(SPRING_CONTEXT_HEADER);
		return (header != null ? header.toString().trim() : null);
	}

	/**
	 * Return the directive value as a String. If the directive does not exist
	 * or is invalid (wrong format) a null string will be returned.
	 * 
	 * @param header
	 * @param directive
	 * @return
	 */
	public static String getDirectiveValue(String header, String directive) {
		Assert.notNull(header, "not-null header required");
		Assert.notNull(directive, "not-null directive required");
		String[] directives = StringUtils.tokenizeToStringArray(header, DIRECTIVE_SEPARATOR);

		for (int i = 0; i < directives.length; i++) {
			String[] splittedDirective = StringUtils.delimitedListToStringArray(directives[i].trim(), EQUALS);
			if (splittedDirective.length == 2 && splittedDirective[0].equals(directive))
				return splittedDirective[1];
		}

		return null;
	}

	/**
	 * Shortcut method to retrieve directive values. Used internally by the
	 * dedicated getXXX.
	 * 
	 * @param directiveName
	 * @return
	 */
	private static String getDirectiveValue(Dictionary headers, String directiveName) {
		String header = getSpringContextHeader(headers);
		if (header != null) {
			String directive = getDirectiveValue(header, directiveName);
			if (directive != null)
				return directive;
		}
		return null;
	}

	/**
	 * Returns true if the given directive is present or false otherwise.
	 * 
	 * @param headers
	 * @param directiveName
	 * @return
	 */
	public static boolean isDirectiveDefined(Dictionary headers, String directiveName) {
		String header = getSpringContextHeader(headers);
		if (header != null) {
			String directive = getDirectiveValue(header, directiveName);
			return (directive != null);
		}
		return false;
	}

	/**
	 * Shortcut for finding the boolean value for
	 * {@link #DIRECTIVE_PUBLISH_CONTEXT} directive using the given headers.
	 * Assumes the headers belong to a Spring powered bundle.
	 * 
	 * @param headers
	 * @return
	 */
	public static boolean getPublishContext(Dictionary headers) {
		String value = getDirectiveValue(headers, DIRECTIVE_PUBLISH_CONTEXT);
		return (value != null ? Boolean.valueOf(value).booleanValue() : DIRECTIVE_PUBLISH_CONTEXT_DEFAULT);
	}

	/**
	 * Shortcut for finding the boolean value for
	 * {@link #DIRECTIVE_CREATE_ASYNCHRONOUSLY} directive using the given
	 * headers.
	 * 
	 * Assumes the headers belong to a Spring powered bundle.
	 * 
	 * @param headers
	 * @return
	 */
	public static boolean getCreateAsync(Dictionary headers) {
		String value = getDirectiveValue(headers, DIRECTIVE_CREATE_ASYNCHRONOUSLY);
		return (value != null ? Boolean.valueOf(value).booleanValue() : DIRECTIVE_CREATE_ASYNCHRONOUSLY_DEFAULT);
	}

	/**
	 * Shortcut for finding the boolean value for {@link #DIRECTIVE_TIMEOUT}
	 * directive using the given headers.
	 * 
	 * Assumes the headers belong to a Spring powered bundle. Returns the
	 * timeout (in seconds) for which the application context should wait to
	 * have its dependencies satisfied.
	 * 
	 * @param headers
	 * @return
	 */
	public static long getTimeOut(Dictionary headers) {
		String value = getDirectiveValue(headers, DIRECTIVE_TIMEOUT);

		if (value != null) {
			if (DIRECTIVE_TIMEOUT_VALUE_NONE.equalsIgnoreCase(value)) {
				return DIRECTIVE_NO_TIMEOUT;
			}
			return Long.valueOf(value).longValue();
		}

		return DIRECTIVE_TIMEOUT_DEFAULT;
	}

	/**
	 * Shortcut for finding the boolean value for
	 * {@link #DIRECTIVE_WAIT_FOR_DEPS} directive using the given headers.
	 * Assumes the headers belong to a Spring powered bundle.
	 * 
	 * @param headers
	 * @return
	 */
	public static boolean getWaitForDependencies(Dictionary headers) {
		String value = getDirectiveValue(headers, DIRECTIVE_WAIT_FOR_DEPS);

		return (value != null ? Boolean.valueOf(value).booleanValue() : DIRECTIVE_WAIT_FOR_DEPS_DEFAULT);
	}

	/**
	 * Returns the location headers (if any) specified by the Spring-Context
	 * header (if available). The returned Strings can be sent to a
	 * {@link org.springframework.core.io.ResourceLoader} for loading the
	 * configurations.
	 * 
	 * @param headers bundle headers
	 * @return array of locations specified (if any)
	 */
	public static String[] getHeaderLocations(Dictionary headers) {
		String header = getSpringContextHeader(headers);

		String[] ctxEntries;
		if (StringUtils.hasText(header) && !(';' == header.charAt(0))) {
			// get the config locations
			String locations = StringUtils.tokenizeToStringArray(header, DIRECTIVE_SEPARATOR)[0];
			// parse it into individual token
			ctxEntries = StringUtils.tokenizeToStringArray(locations, CONTEXT_LOCATION_SEPARATOR);

			// replace * with a 'digestable' location
			for (int i = 0; i < ctxEntries.length; i++) {
				if (CONFIG_WILDCARD.equals(ctxEntries[i]))
					ctxEntries[i] = OsgiBundleXmlApplicationContext.DEFAULT_CONFIG_LOCATION;
			}
		}
		else {
			ctxEntries = new String[0];
		}

		return ctxEntries;
	}
}
