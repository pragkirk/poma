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
import org.springframework.core.Conventions;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Callback relying on 'Spring' conventions. Normally this is the last callback
 * in the stack trying to convert the property name and then setting it on the
 * builder.
 * 
 * @see Conventions#attributeNameToPropertyName(String)
 * @see BeanDefinitionBuilder#addPropertyValue(String, Object)
 * 
 * @author Costin Leau
 */
public class ConventionsCallback implements AttributeCallback {

	public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
		String name = attribute.getLocalName();
		String propertyName = Conventions.attributeNameToPropertyName(name);
		builder.addPropertyValue(propertyName, attribute.getValue());
		return true;
	}
}
