/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.osgi.config.internal.util.ParserUtils;
import org.springframework.osgi.config.internal.util.ServiceAttributeCallback;
import org.springframework.osgi.config.internal.util.ServiceParsingUtils;
import org.springframework.osgi.service.exporter.support.OsgiServiceFactoryBean;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * BeanDefinitionParser for service element found in the osgi namespace.
 * 
 * @author Costin Leau
 * @author Hal Hildebrand
 * @author Andy Piper
 */
class ServiceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	// bean properties
	private static final String TARGET_BEAN_NAME_PROP = "targetBeanName";
	private static final String TARGET_PROP = "target";
	private static final String LISTENERS_PROP = "listeners";

	// XML elements
	private static final String LISTENER = "registration-listener";
	private static final String REF = "ref";


	protected Class getBeanClass(Element element) {
		return OsgiServiceFactoryBean.class;
	}

	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		// parse attributes
		ParserUtils.parseCustomAttributes(element, builder, new ServiceAttributeCallback());

		// determine nested/referred beans
		Object target = null;
		if (element.hasAttribute(REF))
			target = new RuntimeBeanReference(element.getAttribute(REF));

		// element is considered parent
		NodeList nl = element.getChildNodes();

		ManagedList listeners = new ManagedList();

		// parse all sub elements
		// we iterate through them since we have to 'catch' the possible nested
		// bean which has an unknown name local name

		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				Element subElement = (Element) node;
				String name = subElement.getLocalName();

				// osgi:interface
				if (ServiceParsingUtils.parseInterfaces(element, subElement, parserContext, builder))
					;
				// osgi:service-properties
				else if (ServiceParsingUtils.parseServiceProperties(element, subElement, parserContext, builder))
					;
				// osgi:registration-listener
				else if (LISTENER.equals(name)) {
					listeners.add(ServiceParsingUtils.parseListener(parserContext, subElement, builder));
				}

				// nested bean reference/declaration
				else
					target = parseBeanReference(element, subElement, parserContext, builder);
			}
		}

		// if we have a named bean use target_bean_name (so we postpone the service creation)
		if (target instanceof RuntimeBeanReference) {
			builder.addPropertyValue(TARGET_BEAN_NAME_PROP, ((RuntimeBeanReference) target).getBeanName());
		}
		else {
			// add target (can be either an object instance or a bean
			// definition)
			builder.addPropertyValue(TARGET_PROP, target);
		}

		// add listeners
		builder.addPropertyValue(LISTENERS_PROP, listeners);
	}

	// parse nested bean definition
	private Object parseBeanReference(Element parent, Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		// check shortcut on the parent
		if (parent.hasAttribute(REF))
			parserContext.getReaderContext().error(
				"nested bean definition/reference cannot be used when attribute 'ref' is specified", parent);
		return parserContext.getDelegate().parsePropertySubElement(element, builder.getBeanDefinition());
	}

	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}
}