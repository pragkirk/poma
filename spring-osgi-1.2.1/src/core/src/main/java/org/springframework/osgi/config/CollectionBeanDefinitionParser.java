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

package org.springframework.osgi.config;

import java.util.Comparator;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.osgi.config.internal.util.AttributeCallback;
import org.springframework.osgi.config.internal.util.ParserUtils;
import org.springframework.osgi.service.importer.support.Cardinality;
import org.springframework.osgi.service.importer.support.CollectionType;
import org.springframework.osgi.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.springframework.osgi.service.importer.support.internal.collection.comparator.ServiceReferenceComparator;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * &lt;osgi:list&gt;, &lt;osgi:set&gt;, element parser.
 * 
 * @author Costin Leau
 * 
 */
abstract class CollectionBeanDefinitionParser extends AbstractReferenceDefinitionParser {

	/**
	 * Greedy proxy attribute callback.
	 * 
	 * @author Costin Leau
	 */
	static class GreedyProxyingAttributeCallback implements AttributeCallback {

		public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
			String name = attribute.getLocalName();
			if (GREEDY_PROXYING.equals(name)) {
				builder.addPropertyValue(GREEDY_PROXYING_PROPERTY, attribute.getValue());
				return false;
			}
			return true;
		}
	}


	private static final String NESTED_COMPARATOR = "comparator";

	private static final String INLINE_COMPARATOR_REF = "comparator-ref";

	private static final String COLLECTION_TYPE_PROP = "collectionType";

	private static final String COMPARATOR_PROPERTY = "comparator";

	private static final String SERVICE_ORDER = "service";

	private static final String SERVICE_REFERENCE_ORDER = "service-reference";

	private static final String GREEDY_PROXYING = "greedy-proxying";

	private static final String GREEDY_PROXYING_PROPERTY = "greedyProxying";

	private static final Comparator SERVICE_REFERENCE_COMPARATOR = new ServiceReferenceComparator();

	private static final String NATURAL = "natural";

	private static final String BASIS = "basis";


	protected Class getBeanClass(Element element) {
		return OsgiServiceCollectionProxyFactoryBean.class;
	}

	protected String mandatoryCardinality() {
		return Cardinality.C_1__N.getLabel();
	}

	protected String optionalCardinality() {
		return Cardinality.C_0__N.getLabel();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Add support for 'greedy-proxying' attribute.
	 */
	protected void parseAttributes(Element element, BeanDefinitionBuilder builder, AttributeCallback[] callbacks) {
		// add timeout callback
		GreedyProxyingAttributeCallback greedyProxyingCallback = new GreedyProxyingAttributeCallback();
		super.parseAttributes(element, builder, ParserUtils.mergeCallbacks(callbacks,
			new AttributeCallback[] { greedyProxyingCallback }));
	}

	protected void parseNestedElements(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		super.parseNestedElements(element, context, builder);
		parseComparator(element, context, builder);
	}

	/**
	 * Parse &lt;comparator&gt; element.
	 * 
	 * @param element
	 * @param context
	 * @param builder
	 */
	protected void parseComparator(Element element, ParserContext context, BeanDefinitionBuilder builder) {
		boolean hasComparatorRef = element.hasAttribute(INLINE_COMPARATOR_REF);

		// check nested comparator
		Element comparatorElement = DomUtils.getChildElementByTagName(element, NESTED_COMPARATOR);

		Object nestedComparator = null;

		// comparator definition present
		if (comparatorElement != null) {
			// check duplicate nested and inline bean definition
			if (hasComparatorRef)
				context.getReaderContext().error(
					"nested comparator declaration is not allowed if " + INLINE_COMPARATOR_REF
							+ " attribute has been specified", comparatorElement);

			NodeList nl = comparatorElement.getChildNodes();

			// take only elements
			for (int i = 0; i < nl.getLength(); i++) {
				Node nd = nl.item(i);
				if (nd instanceof Element) {
					Element beanDef = (Element) nd;
					String name = beanDef.getLocalName();
					// check if we have a 'natural' tag (known comparator
					// definitions)
					if (NATURAL.equals(name))
						nestedComparator = parseNaturalComparator(beanDef);
					else
						// we have a nested definition
						nestedComparator = context.getDelegate().parsePropertySubElement(beanDef,
							builder.getBeanDefinition());
				}
			}

			// set the reference to the nested comparator reference
			if (nestedComparator != null)
				builder.addPropertyValue(COMPARATOR_PROPERTY, nestedComparator);
		}

		// set collection type
		// based on the existence of the comparator
		// we treat the case where the comparator is natural which means the
		// comparator
		// instance is null however, we have to force a sorted collection to be
		// used
		// so that the object natural ordering is used.

		if (comparatorElement != null || hasComparatorRef) {
			if (CollectionType.LIST.equals(collectionType()))
				builder.addPropertyValue(COLLECTION_TYPE_PROP, CollectionType.SORTED_LIST);

			if (CollectionType.SET.equals(collectionType()))
				builder.addPropertyValue(COLLECTION_TYPE_PROP, CollectionType.SORTED_SET);
		}
		else
			builder.addPropertyValue(COLLECTION_TYPE_PROP, collectionType());

	}

	/**
	 * Parse &lt;osgi:natural&gt; element.
	 * 
	 * @param element
	 * @return
	 */
	protected Comparator parseNaturalComparator(Element element) {
		Comparator comparator = null;
		NamedNodeMap attributes = element.getAttributes();
		for (int x = 0; x < attributes.getLength(); x++) {
			Attr attribute = (Attr) attributes.item(x);
			String name = attribute.getLocalName();
			String value = attribute.getValue();

			if (BASIS.equals(name)) {

				if (SERVICE_REFERENCE_ORDER.equals(value))
					return SERVICE_REFERENCE_COMPARATOR;

				// no comparator means relying on Comparable interface of the
				// services
				else if (SERVICE_ORDER.equals(value))
					return null;
			}

		}

		return comparator;
	}

	/**
	 * Hook used for indicating the main collection type (set/list) on which
	 * this parser applies.
	 * 
	 * @return service collection type
	 */
	protected abstract CollectionType collectionType();
}
