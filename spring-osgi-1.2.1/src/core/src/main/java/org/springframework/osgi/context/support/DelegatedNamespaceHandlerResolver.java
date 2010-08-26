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

package org.springframework.osgi.context.support;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.util.Assert;

/**
 * Delegated XML namespace handler resolver.
 * 
 * @author Costin Leau
 * 
 */
class DelegatedNamespaceHandlerResolver implements NamespaceHandlerResolver {

	/** logger */
	private static final Log log = LogFactory.getLog(DelegatedNamespaceHandlerResolver.class);

	private final Map resolvers = new LinkedHashMap(2);


	public void addNamespaceHandler(NamespaceHandlerResolver resolver, String resolverToString) {
		Assert.notNull(resolver);
		resolvers.put(resolver, resolverToString);
	}

	public NamespaceHandler resolve(String namespaceUri) {
		boolean trace = log.isTraceEnabled();

		for (Iterator iterator = resolvers.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			NamespaceHandlerResolver handlerResolver = (NamespaceHandlerResolver) entry.getKey();
			if (trace)
				log.trace("Trying to resolve namespace [" + namespaceUri + "] through resolver " + entry.getValue());
			NamespaceHandler handler = handlerResolver.resolve(namespaceUri);

			String resolvedMsg = (handler != null ? "" : "not ");
			if (trace)
				log.trace("Namespace [" + namespaceUri + "] was " + resolvedMsg + "resolved through handler resolver "
						+ entry.getValue());

			if (handler != null) {
				return handler;
			}

		}
		return null;
	}
}
