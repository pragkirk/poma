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

package org.springframework.osgi.config;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class containing osgi defaults.
 * 
 * @author Costin Leau
 * 
 */
class OsgiDefaultsDefinition {

	private static final String OSGI_NS = "http://www.springframework.org/schema/osgi";

	private static final String DEFAULT_TIMEOUT = "default-timeout";

	private static final String DEFAULT_CARDINALITY = "default-cardinality";

	private static final String TIMEOUT_DEFAULT = "300000";

	private static final String CARDINALITY_DEFAULT = "1..X";

	/** Default value */
	private String timeout = TIMEOUT_DEFAULT;

	/** Default value */
	private String cardinality = CARDINALITY_DEFAULT;


	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getCardinality() {
		return cardinality;
	}

	public void setCardinality(String cardinality) {
		this.cardinality = cardinality;
	}

	/**
	 * Initialize OSGi defaults.
	 * 
	 * @param document XML document
	 * @return initialized {@link OsgiDefaultsDefinition} instance
	 */
	public static OsgiDefaultsDefinition initOsgiDefaults(Document document) {
		Assert.notNull(document);
		return initOsgiDefaults(document.getDocumentElement());
	}

	/**
	 * Initialize OSGi defaults.
	 * 
	 * @param root root document element
	 * @return initialized {@link OsgiDefaultsDefinition} instance
	 */
	public static OsgiDefaultsDefinition initOsgiDefaults(Element root) {
		Assert.notNull(root);

		OsgiDefaultsDefinition defaults = new OsgiDefaultsDefinition();
		String timeout = root.getAttributeNS(OSGI_NS, DEFAULT_TIMEOUT);

		if (StringUtils.hasText(timeout))
			defaults.setTimeout(timeout);

		String cardinality = root.getAttributeNS(OSGI_NS, DEFAULT_CARDINALITY);

		if (StringUtils.hasText(cardinality))
			defaults.setCardinality(cardinality);

		return defaults;
	}
}
