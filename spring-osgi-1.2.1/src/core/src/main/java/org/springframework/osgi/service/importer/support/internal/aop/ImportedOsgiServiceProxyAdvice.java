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

package org.springframework.osgi.service.importer.support.internal.aop;

import org.osgi.framework.ServiceReference;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.osgi.service.importer.ImportedOsgiServiceProxy;
import org.springframework.osgi.service.importer.ServiceReferenceProxy;
import org.springframework.util.Assert;

/**
 * Mix-in implementation for ImportedOsgiServiceProxy.
 * 
 * @author Costin Leau
 * 
 */
public class ImportedOsgiServiceProxyAdvice extends DelegatingIntroductionInterceptor implements
		ImportedOsgiServiceProxy {

	private static final long serialVersionUID = 6455437774724678999L;

	private static final int hashCode = ImportedOsgiServiceProxyAdvice.class.hashCode() * 13;

	private final transient ServiceReferenceProxy reference;


	public ImportedOsgiServiceProxyAdvice(ServiceReference reference) {
		Assert.notNull(reference);
		this.reference = (reference instanceof ServiceReferenceProxy ? (ServiceReferenceProxy) reference
				: new StaticServiceReferenceProxy(reference));
	}

	public ServiceReferenceProxy getServiceReference() {
		return reference;
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof ImportedOsgiServiceProxyAdvice) {
			ImportedOsgiServiceProxyAdvice oth = (ImportedOsgiServiceProxyAdvice) other;
			return (reference.equals(oth.reference));
		}
		else
			return false;
	}

	public int hashCode() {
		return hashCode;
	}
}
