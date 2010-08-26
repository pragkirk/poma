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

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.osgi.service.importer.support.CollectionType;


/**
 * Namespace handler for Osgi definitions.
 * 
 * @author Hal Hildebrand
 * @author Andy Piper
 * @author Costin Leau
 */
class OsgiNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		//
		// Importer definitions
		//

		// a. single reference
		registerBeanDefinitionParser("reference", new ReferenceBeanDefinitionParser());

		registerBeanDefinitionParser("list", new CollectionBeanDefinitionParser() {

			protected CollectionType collectionType() {
				return CollectionType.LIST;
			}
		});

		registerBeanDefinitionParser("set", new CollectionBeanDefinitionParser() {

			protected CollectionType collectionType() {
				return CollectionType.SET;
			}
		});

		//
		// Exporter
		//
		registerBeanDefinitionParser("service", new ServiceBeanDefinitionParser());

		//
		// Bundle FB
		//
		registerBeanDefinitionParser("bundle", new BundleBeanDefinitionParser());
	}
}
