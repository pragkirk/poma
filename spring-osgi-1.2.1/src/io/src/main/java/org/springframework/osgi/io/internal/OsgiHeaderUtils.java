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

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.springframework.util.StringUtils;

/**
 * Utility class for handling various header operations such as splitting a
 * manifest header into packages or extracting the version from a certain entry.
 * 
 * @author Costin Leau
 * 
 */
public abstract class OsgiHeaderUtils {

	private static final char ROUND_BRACKET_CHAR = '(';

	private static final char SQUARE_BRACKET_CHAR = '[';

	private static final char QUOTE_CHAR = '\"';

	private static final char COMMA_CHAR = ',';

	private static final String SEMI_COLON = ";";
	private static final String DOUBLE_QUOTE = "\"";
	private static final String DEFAULT_VERSION = "0.0.0";


	public static String[] getBundleClassPath(Bundle bundle) {
		return getHeaderAsTrimmedStringArray(bundle, Constants.BUNDLE_CLASSPATH);
	}

	public static String[] getRequireBundle(Bundle bundle) {
		return getHeaderWithAttributesAsTrimmedStringArray(bundle, Constants.REQUIRE_BUNDLE);
	}

	private static String[] getHeaderAsTrimmedStringArray(Bundle bundle, String header) {
		if (bundle == null || !StringUtils.hasText(header))
			return new String[0];

		String headerContent = (String) bundle.getHeaders().get(header);
		String[] entries = StringUtils.commaDelimitedListToStringArray(headerContent);
		for (int i = 0; i < entries.length; i++) {
			entries[i] = entries[i].trim();
		}

		return entries;
	}

	private static String[] getHeaderWithAttributesAsTrimmedStringArray(Bundle bundle, String header) {
		if (bundle == null || !StringUtils.hasText(header))
			return new String[0];

		String headerContent = (String) bundle.getHeaders().get(header);

		if (!StringUtils.hasText(headerContent))
			return new String[0];

		// consider , as a delimiter only if a quote is not encountered
		List tokens = new ArrayList(2);

		StringBuffer token = new StringBuffer();
		boolean ignoreComma = false;
		for (int stringIndex = 0; stringIndex < headerContent.length(); stringIndex++) {
			char currentChar = headerContent.charAt(stringIndex);
			if (currentChar == COMMA_CHAR) {
				if (ignoreComma) {
					token.append(currentChar);
				}
				else {
					tokens.add(token.toString().trim());
					token.delete(0, token.length());
					ignoreComma = false;
				}
			}
			else {
				if (currentChar == QUOTE_CHAR) {
					ignoreComma = !ignoreComma;
				}
				token.append(currentChar);
			}
		}
		tokens.add(token.toString().trim());
		return (String[]) tokens.toArray(new String[tokens.size()]);
	}

	/**
	 * Parses the required bundle entry to determine the bundle symbolic name
	 * and version.
	 * 
	 * @param string required bundle entry
	 * @return returns an array of strings with 2 entries, the first being the
	 * bundle sym name, the second the version (or 0.0.0 if nothing is
	 * specified).
	 */
	public static String[] parseRequiredBundleString(String entry) {
		String[] value = new String[2];

		// determine the bundle symbolic name
		int index = entry.indexOf(SEMI_COLON);

		// there is at least one flag so extract the sym name
		if (index > 0) {
			value[0] = entry.substring(0, index);
		}
		// no flag, short circuit
		else {
			value[0] = entry;
			value[1] = DEFAULT_VERSION;
			return value;
		}

		// look for bundle-version
		index = entry.indexOf(Constants.BUNDLE_VERSION_ATTRIBUTE);
		if (index > 0) {
			// skip the =
			int firstQuoteIndex = index + Constants.BUNDLE_VERSION_ATTRIBUTE.length() + 1;
			// check if the version is quoted
			boolean isQuoted = entry.charAt(firstQuoteIndex) == QUOTE_CHAR;

			// no quote means automatically no range
			if (!isQuoted) {
				int nextAttribute = entry.indexOf(SEMI_COLON, firstQuoteIndex);
				value[1] = (nextAttribute > -1 ? entry.substring(firstQuoteIndex, nextAttribute)
						: entry.substring(firstQuoteIndex));
			}
			else {
				// check if a range or a number is specified
				char testChar = entry.charAt(firstQuoteIndex + 1);
				boolean isRange = (testChar == SQUARE_BRACKET_CHAR || testChar == ROUND_BRACKET_CHAR);
				int secondQuoteStartIndex = (isRange ? firstQuoteIndex + 4 : firstQuoteIndex + 1);

				int numberStart = (isRange ? firstQuoteIndex + 2 : firstQuoteIndex + 1);
				int numberEnd = entry.indexOf(DOUBLE_QUOTE, secondQuoteStartIndex) - (isRange ? 1 : 0);

				value[1] = entry.substring(numberStart, numberEnd);
				// if it's a range, append the interval notation
				if (isRange) {
					value[1] = entry.charAt(firstQuoteIndex + 1) + value[1] + entry.charAt(numberEnd);
				}
			}
		}
		else {
			value[1] = DEFAULT_VERSION;
		}

		return value;
	}
}
