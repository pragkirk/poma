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

package org.springframework.osgi.config.internal.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * Parsing utility class.
 * 
 * @author Andy Piper
 * @author Costin Leau
 */
public abstract class ParserUtils {

	private static final AttributeCallback STANDARD_ATTRS_CALLBACK = new StandardAttributeCallback();

	private static final AttributeCallback PROPERTY_REF_ATTRS_CALLBACK = new PropertyRefAttributeCallback();

	private static final AttributeCallback PROPERTY_CONV_ATTRS_CALLBACK = new ConventionsCallback();


	/**
	 * Generic attribute callback. Will parse the given callback array, w/o any
	 * standard callback.
	 * 
	 * @param element XML element
	 * @param builder current bean definition builder
	 * @param callbacks array of callbacks (can be null/empty)
	 */
	public static void parseAttributes(Element element, BeanDefinitionBuilder builder, AttributeCallback[] callbacks) {
		NamedNodeMap attributes = element.getAttributes();

		for (int x = 0; x < attributes.getLength(); x++) {
			Attr attr = (Attr) attributes.item(x);

			boolean shouldContinue = true;
			if (!ObjectUtils.isEmpty(callbacks))
				for (int i = 0; i < callbacks.length && shouldContinue; i++) {
					AttributeCallback callback = callbacks[i];
					shouldContinue = callback.process(element, attr, builder);
				}
		}
	}

	/**
	 * Dedicated parsing method that uses the following stack:
	 * <ol>
	 * <li>user given {@link AttributeCallback}s</li>
	 * <li>{@link StandardAttributeCallback}</li>
	 * <li>{@link PropertyRefAttributeCallback}</li>
	 * <li>{@link ConventionCallback}</li>
	 * </ol>
	 * 
	 * 
	 * @param element XML element
	 * @param builder current bean definition builder
	 * @param callbacks array of callbacks (can be null/empty)
	 */
	public static void parseCustomAttributes(Element element, BeanDefinitionBuilder builder,
			AttributeCallback[] callbacks) {
		List list = new ArrayList(8);

		if (!ObjectUtils.isEmpty(callbacks))
			CollectionUtils.mergeArrayIntoCollection(callbacks, list);
		// add standard callback
		list.add(STANDARD_ATTRS_CALLBACK);
		// add property ref
		list.add(PROPERTY_REF_ATTRS_CALLBACK);
		// add convention
		list.add(PROPERTY_CONV_ATTRS_CALLBACK);

		AttributeCallback[] cbacks = (AttributeCallback[]) list.toArray(new AttributeCallback[list.size()]);
		parseAttributes(element, builder, cbacks);
	}

	/**
	 * Derivative for
	 * {@link #parseCustomAttributes(Element, BeanDefinitionBuilder, org.springframework.osgi.internal.config.ParserUtils.AttributeCallback[])}
	 * accepting only one {@link AttributeCallback}.
	 * 
	 * @param element XML element
	 * @param builder current bean definition builder
	 * @param callback attribute callback, can be null
	 */
	public static void parseCustomAttributes(Element element, BeanDefinitionBuilder builder, AttributeCallback callback) {
		AttributeCallback[] callbacks = (callback == null ? new AttributeCallback[0]
				: new AttributeCallback[] { callback });
		parseCustomAttributes(element, builder, callbacks);
	}

	public static AttributeCallback[] mergeCallbacks(AttributeCallback[] callbacksA, AttributeCallback[] callbacksB) {
		if (ObjectUtils.isEmpty(callbacksA))
			if (ObjectUtils.isEmpty(callbacksB))
				return new AttributeCallback[0];
			else
				return callbacksB;
		if (ObjectUtils.isEmpty(callbacksB))
			return callbacksA;

		AttributeCallback[] newCallbacks = new AttributeCallback[callbacksA.length + callbacksB.length];
		System.arraycopy(callbacksA, 0, newCallbacks, 0, callbacksA.length);
		System.arraycopy(callbacksB, 0, newCallbacks, callbacksA.length, callbacksB.length);
		return newCallbacks;
	}
}
