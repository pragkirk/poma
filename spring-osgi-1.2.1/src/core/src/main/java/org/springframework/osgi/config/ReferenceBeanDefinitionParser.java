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

import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.osgi.config.internal.util.ParserUtils;
import org.springframework.osgi.config.internal.util.AttributeCallback;
import org.springframework.osgi.service.importer.support.Cardinality;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * &lt;osgi:reference&gt; element parser.
 * 
 * @author Andy Piper
 * @author Costin Leau
 */
class ReferenceBeanDefinitionParser extends AbstractReferenceDefinitionParser {

	/**
	 * Reference attribute callback extension that looks for 'singular'
	 * reference attributes (such as timeout).
	 * 
	 * @author Costin Leau
	 */
	static class TimeoutAttributeCallback implements AttributeCallback {

		boolean isTimeoutSpecified = false;


		public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
			String name = attribute.getLocalName();

			if (TIMEOUT.equals(name)) {
				isTimeoutSpecified = true;
			}

			return true;
		}
	}


	// call properties
	private static final String TIMEOUT_PROP = "timeout";

	// XML attributes/elements
	protected static final String TIMEOUT = "timeout";


	protected Class getBeanClass(Element element) {
		return OsgiServiceProxyFactoryBean.class;
	}

	protected void parseAttributes(Element element, BeanDefinitionBuilder builder, AttributeCallback[] callbacks) {
		// add timeout callback
		TimeoutAttributeCallback timeoutCallback = new TimeoutAttributeCallback();
		super.parseAttributes(element, builder, ParserUtils.mergeCallbacks(callbacks,
			new AttributeCallback[] { timeoutCallback }));

		// look for defaults
		if (!timeoutCallback.isTimeoutSpecified) {
			applyDefaultTimeout(builder, defaults);
		}
	}

	protected String mandatoryCardinality() {
		return Cardinality.C_1__1.getLabel();
	}

	protected String optionalCardinality() {
		return Cardinality.C_0__1.getLabel();
	}

	/**
	 * Apply default definitions to the existing bean definition. In this case,
	 * it means applying the timeout.
	 * 
	 * This method is called when a certain expected element is not present.
	 * 
	 * @param element
	 * @param context
	 * @param builder
	 */
	protected void applyDefaultTimeout(BeanDefinitionBuilder builder, OsgiDefaultsDefinition defaults) {
		builder.addPropertyValue(TIMEOUT_PROP, new TypedStringValue(defaults.getTimeout()));
	}
}