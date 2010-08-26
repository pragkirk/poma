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

package org.springframework.osgi.iandt.ns;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Bogus handler. Creates a bean definition to test whether the handler
 * providing the namespace can actually start its own context. It registers two
 * beans named: "nsDate" and "nsBean".
 * 
 * @author Costin Leau
 */
public class BogusHandler implements NamespaceHandler {

	public static volatile boolean initialized = false;


	public BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder definition, ParserContext parserContext) {
		return null;
	}

	public void init() {
		initialized = true;
		System.out.println("BogusHandler initialized");
	}

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionRegistry registry = parserContext.getRegistry();
		
		registry.registerBeanDefinition("nsDate", BeanDefinitionBuilder.genericBeanDefinition("java.util.Date").getBeanDefinition());
		registry.registerBeanDefinition("nsBean", BeanDefinitionBuilder.genericBeanDefinition("java.awt.Rectangle").getBeanDefinition());
		return null;
	}
}
