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

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Delegated XML entity resolver.
 * 
 * @author Costin Leau
 * 
 */
class DelegatedEntityResolver implements EntityResolver {

	/** logger */
	private static final Log log = LogFactory.getLog(DelegatedEntityResolver.class);

	private final Map resolvers = new LinkedHashMap(2);


	public void addEntityResolver(EntityResolver resolver, String resolverToString) {
		Assert.notNull(resolver);
		resolvers.put(resolver, resolverToString);
	}

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		boolean trace = log.isTraceEnabled();

		for (Iterator iterator = resolvers.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			EntityResolver entityResolver = (EntityResolver) entry.getKey();
			if (trace)
				log.trace("Trying to resolve entity [" + publicId + "|" + systemId + "] through resolver "
						+ entry.getValue());
			InputSource entity = entityResolver.resolveEntity(publicId, systemId);

			String resolvedMsg = (entity != null ? "" : "not ");
			if (trace)
				log.trace("Entity [" + publicId + "|" + systemId + "] was " + resolvedMsg
						+ "resolved through entity resolver " + entry.getValue());

			if (entity != null) {
				return entity;
			}
		}
		return null;
	}
}
