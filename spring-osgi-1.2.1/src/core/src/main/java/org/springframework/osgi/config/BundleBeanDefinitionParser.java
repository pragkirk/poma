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
 *
 */

package org.springframework.osgi.config;

import java.util.Locale;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.osgi.bundle.BundleFactoryBean;
import org.springframework.osgi.config.internal.util.ParserUtils;
import org.springframework.osgi.config.internal.util.AttributeCallback;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * BundleFactoryBean definition.
 * 
 * @author Andy Piper
 * @author Costin Leau
 */
class BundleBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	static class BundleActionCallback implements AttributeCallback {

		public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
			String name = attribute.getLocalName();
			if (ACTION.equals(name)) {
				builder.addPropertyValue(ACTION_PROP, parseAction(parent, attribute));
				return false;
			}

			if (DESTROY_ACTION.equals(name)) {
				builder.addPropertyValue(DESTROY_ACTION_PROP, parseAction(parent, attribute));
				return false;
			}

			return true;
		}

		// do upper case to make sure the constants match
		private Object parseAction(Element parent, Attr attribute) {
			return attribute.getValue().toUpperCase(Locale.ENGLISH);
		}
	};

	private static final String ACTION = "action";

	private static final String DESTROY_ACTION = "destroy-action";

	// class properties

	private static final String ACTION_PROP = "action";

	private static final String DESTROY_ACTION_PROP = "destroyAction";

	private static final String BUNDLE_PROP = "bundle";

	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		BundleActionCallback callback = new BundleActionCallback();

		ParserUtils.parseCustomAttributes(element, builder, new AttributeCallback[] { callback });

		// parse nested definition (in case there is any)

		if (element.hasChildNodes()) {
			NodeList nodes = element.getChildNodes();
			boolean foundElement = false;
			for (int i = 0; i < nodes.getLength() && !foundElement; i++) {
				Node nd = nodes.item(i);
				if (nd instanceof Element) {
					foundElement = true;
					Object obj = parserContext.getDelegate().parsePropertySubElement((Element) nd,
							builder.getBeanDefinition());
					builder.addPropertyValue(BUNDLE_PROP, obj);
				}
			}
		}

		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
	}

	protected Class getBeanClass(Element element) {
		return BundleFactoryBean.class;
	}
}