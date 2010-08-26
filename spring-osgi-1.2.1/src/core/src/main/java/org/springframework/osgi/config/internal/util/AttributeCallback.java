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
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Wrapper callback used for parsing attributes (one at a time) that have are
 * non standard (ID, LAZY-INIT, DEPENDS-ON).
 * 
 * @author Costin Leau
 */
public interface AttributeCallback {

	/**
	 * Process the given attribute using the contextual element and bean
	 * builder. Normally, the callback will interact with the bean definition
	 * and set some properties. <p/> If the callback has intercepted an
	 * attribute, it can stop the invocation of the rest of the callbacks on the
	 * stack by returning false.
	 * 
	 * @param parent parent element
	 * @param attribute current intercepted attribute
	 * @param builder builder holding the current bean definition
	 * @return true if the rest of the callbacks should be called or false
	 *         otherwise.
	 */
	boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder);
}