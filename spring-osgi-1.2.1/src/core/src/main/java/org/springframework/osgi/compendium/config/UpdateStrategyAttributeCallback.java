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

package org.springframework.osgi.compendium.config;

import java.util.Locale;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.osgi.config.internal.util.AttributeCallback;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Attribute callback dealing with update strategy attribute.
 * 
 * @author Costin Leau
 */
class UpdateStrategyAttributeCallback implements AttributeCallback {

	private static final String UPDATE_STRATEGY = "update-strategy";
	private static final String UPDATE_STRATEGY_PROP = "updateStrategy";


	public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
		String name = attribute.getLocalName();
		String value = attribute.getValue();

		// make sure the attribute is
		if (UPDATE_STRATEGY.equals(name)) {
			// convert constant to upper case to let Spring do the
			// conversion
			String val = value.toUpperCase(Locale.ENGLISH).replace('-', '_');
			builder.addPropertyValue(UPDATE_STRATEGY_PROP, val);
			return false;
		}

		return true;
	}
}