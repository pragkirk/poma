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

package org.springframework.osgi.samples.console.web;

import java.util.EnumMap;
import java.util.Map;

/**
 * OSGi bundle search space enumeration.
 * 
 * @author Costin Leau
 * 
 */
public enum SearchSpace {

	BUNDLE("osgibundle:"), JAR("osgibundlejar:"), CLASSPATH("classpath:"), CLASSPATHS("classpath*:");

	// Initialize the toString map
	private static final Map<SearchSpace, String> toStringMap = new EnumMap<SearchSpace, String>(SearchSpace.class);

	static {
		// create toString map
		for (SearchSpace space : SearchSpace.values())
			toStringMap.put(space, space.toString().toLowerCase());
	}

	private final String resourcePrefix;


	private SearchSpace(String resourcePrefix) {
		this.resourcePrefix = resourcePrefix;
	}

	public String resourcePrefix() {
		return resourcePrefix;
	}

	/**
	 * Returns a map of enum<->toString association.
	 * 
	 * @return enum<->toString association map
	 */
	public static Map<SearchSpace, String> toStringMap() {
		return toStringMap;
	}
}
