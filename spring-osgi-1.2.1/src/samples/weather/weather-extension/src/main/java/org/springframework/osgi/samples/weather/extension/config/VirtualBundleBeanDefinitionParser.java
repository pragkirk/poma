/*
 * Copyright 2006 the original author or authors.
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
package org.springframework.osgi.samples.weather.extension.config;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.osgi.samples.weather.extension.bundle.PackageSpecification;
import org.springframework.osgi.samples.weather.extension.bundle.VirtualBundleFactoryBean;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * Parser for VirtualBundleFactoryBean
 * 
 * @author Andy Piper
 */
class VirtualBundleBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {
	public static final String PACKAGE_ELEMENT = "package";

	public static final String ID_NAME = "name";

	public static final String ID_VERSION = "version";

	public static final String DEPENDS_ON = "depends-on";

	protected void postProcess(BeanDefinitionBuilder beanDefinition, Element element) {
		Element e = DomUtils.getChildElementByTagName(element, "exports");
		if (e != null) {
			beanDefinition.addPropertyValue("exports", extractPackageSet(e));
		}
		e = DomUtils.getChildElementByTagName(element, "imports");
		if (e != null) {
			beanDefinition.addPropertyValue("imports", extractPackageSet(e));
		}

		String dependsOn = element.getAttribute(DEPENDS_ON);

		if (StringUtils.hasText(dependsOn)) {
			beanDefinition.getBeanDefinition().setDependsOn(
				StringUtils.tokenizeToStringArray(dependsOn, BeanDefinitionParserDelegate.BEAN_NAME_DELIMITERS));
		}

		// e = DomUtils.getChildElementByTagName(element, "dynamic-imports");
		// if (e != null) {
		// // builder.addPropertyValue("dynamicImports", extractPackageSet(e));
		// }
	}

	protected boolean isEligibleAttribute(String attributeName) {
		return !DEPENDS_ON.equalsIgnoreCase(attributeName) && super.isEligibleAttribute(attributeName);
	}

	protected Class getBeanClass(Element element) {
		return VirtualBundleFactoryBean.class;
	}

	private Set extractPackageSet(Element e) {
		List propEles = DomUtils.getChildElementsByTagName(e, PACKAGE_ELEMENT);
		HashSet packages = new HashSet();
		for (Iterator it = propEles.iterator(); it.hasNext();) {
			Element propEle = (Element) it.next();

			NamedNodeMap attributes = propEle.getAttributes();
			PackageSpecification p = new PackageSpecification();
			for (int x = 0; x < attributes.getLength(); x++) {
				Attr attribute = (Attr) attributes.item(x);
				String name = attribute.getLocalName();
				if (ID_NAME.equals(name)) {
					p.setName(attribute.getValue());
				}
				else if (ID_VERSION.equals(name)) {
					p.setVersion(attribute.getValue());
				}
			}
			packages.add(p);
		}
		return packages;
	}

}
