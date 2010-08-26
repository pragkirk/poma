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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.osgi.compendium.internal.cm.ManagedServiceFactoryFactoryBean;
import org.springframework.osgi.config.internal.util.AttributeCallback;
import org.springframework.osgi.config.internal.util.ParserUtils;
import org.springframework.osgi.config.internal.util.ServiceAttributeCallback;
import org.springframework.osgi.config.internal.util.ServiceParsingUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Namespace parser for osgix:managed-service-factory.
 * 
 * @author Costin Leau
 */
class ManagedServiceFactoryDefinitionParser extends AbstractSimpleBeanDefinitionParser {

	private static final String TEMPLATE_PROP = "templateDefinition";
	private static final String LISTENER = "registration-listener";
	private static final String LISTENERS_PROP = "listeners";


	protected Class getBeanClass(Element element) {
		return ManagedServiceFactoryFactoryBean.class;
	}

	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

		// do conversion for some of them (mainly enums) 
		ParserUtils.parseCustomAttributes(element, builder, new AttributeCallback[] { new ServiceAttributeCallback(),
			new UpdateStrategyAttributeCallback() });

		// get nested elements
		NodeList children = element.getChildNodes();

		ManagedList listeners = new ManagedList(children.getLength());
		BeanDefinition nestedDefinition = null;

		for (int i = 0; i < children.getLength(); i++) {
			Node nd = children.item(i);
			if (nd instanceof Element) {
				Element nestedElement = (Element) nd;
				String name = nestedElement.getLocalName();

				// osgi:interface
				if (ServiceParsingUtils.parseInterfaces(element, nestedElement, parserContext, builder))
					;

				// osgi:registration-listener
				else if (LISTENER.equals(name)) {
					listeners.add(ServiceParsingUtils.parseListener(parserContext, nestedElement, builder));
				}

				// nested bean reference/declaration
				else {
					String ns = nestedElement.getNamespaceURI();
					// it's a Spring Bean
					if ((ns == null && name.equals(BeanDefinitionParserDelegate.BEAN_ELEMENT))
							|| ns.equals(BeanDefinitionParserDelegate.BEANS_NAMESPACE_URI)) {
						nestedDefinition = parserContext.getDelegate().parseBeanDefinitionElement(nestedElement).getBeanDefinition();
					}
					// it's non Spring
					else {
						nestedDefinition = parserContext.getDelegate().parseCustomElement(nestedElement);
					}

				}
			}

			// don't pass the properties as a bean definition since Spring tries to do conversion
			// and even if we mark the pv as being converted, the flag gets ignored (SPR-5293)
			builder.addPropertyValue(TEMPLATE_PROP, new BeanDefinition[] { nestedDefinition });
			builder.addPropertyValue(LISTENERS_PROP, listeners);
		}
	}
}