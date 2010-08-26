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

import java.util.Properties;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.osgi.compendium.cm.ConfigAdminPropertiesFactoryBean;
import org.w3c.dom.Element;

/**
 * Simple namespace parser for osgix:cm-properties. Extends Single bean
 * definition parser (instead of the simpleBeanDefParser) to properly filter
 * attributes based on the declared namespace.
 * 
 * @author Costin Leau
 */
class ConfigPropertiesDefinitionParser extends AbstractSimpleBeanDefinitionParser {

	private static final String PROPERTIES_PROP = "properties";


	protected Class getBeanClass(Element element) {
		return ConfigAdminPropertiesFactoryBean.class;
	}

	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		// parse attributes using conventions
		super.doParse(element, parserContext, builder);

		// parse nested element (if any)
		Properties parsedProps = parserContext.getDelegate().parsePropsElement(element);
		if (!parsedProps.isEmpty()) {
			if (builder.getRawBeanDefinition().getPropertyValues().contains(PROPERTIES_PROP)) {
				parserContext.getReaderContext().error(
					"Property '" + PROPERTIES_PROP
							+ "' is defined more then once. Only one approach may be used per property.", element);

			}
			builder.addPropertyValue(PROPERTIES_PROP, parsedProps);
		}
	}
}
