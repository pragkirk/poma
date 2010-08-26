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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.service.ServiceUnavailableException;
import org.springframework.osgi.service.importer.ServiceProxyDestroyedException;
import org.springframework.util.Assert;

/**
 * Interceptor offering static behaviour around an OSGi service. If the OSGi
 * becomes unavailable, no look up or retries will be executed, the interceptor
 * throwing an exception.
 * 
 * @author Costin Leau
 * 
 */
public class ServiceStaticInterceptor extends ServiceInvoker {

	private static final int hashCode = ServiceStaticInterceptor.class.hashCode() * 13;

	private boolean destroyed = false;

	/** private lock */
	private final Object lock = new Object();

	private final ServiceReference reference;

	private final BundleContext bundleContext;


	public ServiceStaticInterceptor(BundleContext context, ServiceReference reference) {
		Assert.notNull(context);
		Assert.notNull(reference, "a not null service reference is required");
		this.bundleContext = context;
		this.reference = reference;
	}

	protected Object getTarget() {
		synchronized (lock) {
			if (destroyed)
				throw new ServiceProxyDestroyedException();
		}

		// check if the service is alive first
		if (reference.getBundle() != null) {
			// since requesting for a service requires additional work
			// from the OSGi platform
			Object target = bundleContext.getService(reference);
			if (target != null)
				return target;
		}
		// throw exception
		throw new ServiceUnavailableException(reference);
	}

	public ServiceReference getServiceReference() {
		return reference;
	}

	public void destroy() {
		synchronized (lock) {
			// set this flag first to make sure after destruction, the OSGi service is not used any more
			destroyed = true;
		}
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof ServiceStaticInterceptor) {
			ServiceStaticInterceptor oth = (ServiceStaticInterceptor) other;
			return reference.equals(oth.reference) && bundleContext.equals(oth.bundleContext);
		}
		return false;
	}

	public int hashCode() {
		return hashCode;
	}
}