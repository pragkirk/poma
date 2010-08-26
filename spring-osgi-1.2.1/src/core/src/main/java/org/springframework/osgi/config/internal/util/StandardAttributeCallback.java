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

package org.springframework.osgi.config.internal.util;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Standard attribute callback. Deals with ID, DEPENDS-ON and LAZY-INIT
 * attribute.
 * 
 * @author Costin Leau
 */
public class StandardAttributeCallback implements AttributeCallback {

	public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
		String name = attribute.getLocalName();

		if (BeanDefinitionParserDelegate.ID_ATTRIBUTE.equals(name)) {
			return false;
		}

		if (BeanDefinitionParserDelegate.DEPENDS_ON_ATTRIBUTE.equals(name)) {
			builder.getBeanDefinition().setDependsOn(
				(StringUtils.tokenizeToStringArray(attribute.getValue(),
					BeanDefinitionParserDelegate.BEAN_NAME_DELIMITERS)));
			return false;
		}
		if (BeanDefinitionParserDelegate.LAZY_INIT_ATTRIBUTE.equals(name)) {
			builder.setLazyInit(Boolean.getBoolean(attribute.getValue()));
			return false;
		}
		return true;
	}
}
