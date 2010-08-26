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

import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.core.InfrastructureProxy;
import org.springframework.util.Assert;

/**
 * Mixin implementation for {@link InfrastructureProxy} interface.
 * 
 * @author Costin Leau
 * 
 */
public class InfrastructureOsgiProxyAdvice extends DelegatingIntroductionInterceptor implements InfrastructureProxy {

	private static final long serialVersionUID = -496653472310304413L;

	private static final int hashCode = InfrastructureOsgiProxyAdvice.class.hashCode() * 13;

	private final transient ServiceInvoker invoker;


	public InfrastructureOsgiProxyAdvice(ServiceInvoker serviceInvoker) {
		Assert.notNull(serviceInvoker);
		this.invoker = serviceInvoker;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Returns the OSGi target service.
	 */
	public Object getWrappedObject() {
		return invoker.getTarget();
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof InfrastructureOsgiProxyAdvice) {
			InfrastructureOsgiProxyAdvice oth = (InfrastructureOsgiProxyAdvice) other;
			return (invoker.equals(oth.invoker));
		}
		else
			return false;
	}

	public int hashCode() {
		return hashCode;
	}
}
