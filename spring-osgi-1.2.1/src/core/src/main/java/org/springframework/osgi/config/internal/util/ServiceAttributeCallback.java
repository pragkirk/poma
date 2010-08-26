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

import java.util.Locale;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * &lt;service&gt; attribute callback.
 * 
 * @author Costin Leau
 */
public class ServiceAttributeCallback implements AttributeCallback {

	private static final String AUTOEXPORT = "auto-export";
	private static final String AUTOEXPORT_PROP = "autoExport";
	private static final String INTERFACE = "interface";
	private static final String INTERFACES_PROP = "interfaces";
	private static final String CCL_PROP = "contextClassLoader";
	private static final String CONTEXT_CLASSLOADER = "context-class-loader";
	private static final String REF = "ref";


	public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder bldr) {
		String name = attribute.getLocalName();

		if (INTERFACE.equals(name)) {
			bldr.addPropertyValue(INTERFACES_PROP, attribute.getValue());
			return false;
		}
		else if (REF.equals(name)) {
			return false;
		}

		else if (AUTOEXPORT.equals(name)) {
			// convert constant to upper case to let Spring do the
			// conversion
			String label = attribute.getValue().toUpperCase(Locale.ENGLISH).replace('-', '_');
			bldr.addPropertyValue(AUTOEXPORT_PROP, label);
			return false;
		}

		else if (CONTEXT_CLASSLOADER.equals(name)) {
			// convert constant to upper case to let Spring do the
			// conversion

			String value = attribute.getValue().toUpperCase(Locale.ENGLISH).replace('-', '_');
			bldr.addPropertyValue(CCL_PROP, value);
			return false;
		}

		return true;
	}
}