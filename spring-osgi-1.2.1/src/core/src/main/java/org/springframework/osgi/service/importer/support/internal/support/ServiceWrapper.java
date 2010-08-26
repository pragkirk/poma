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

package org.springframework.osgi.service.importer.support.internal.support;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;
import org.springframework.util.Assert;

/**
 * Wrapper around a service reference offering identity and equality.
 * 
 * @author Costin Leau
 * 
 */
public class ServiceWrapper implements Comparable {

	private ServiceReference reference;

	private final long serviceId;

	private final int serviceRanking;

	private final String toString;

	/** this should be determined in OSGi 4.1 directly from the Bundle * */
	private BundleContext context;


	public ServiceWrapper(ServiceReference ref) {
		this(ref, OsgiBundleUtils.getBundleContext(ref.getBundle()));
	}

	public ServiceWrapper(ServiceReference ref, BundleContext bundleContext) {
		Assert.notNull(ref, "not null service reference required");
		Assert.notNull(bundleContext, "bundleContext required");

		reference = ref;
		this.context = bundleContext;

		serviceId = OsgiServiceReferenceUtils.getServiceId(ref);
		serviceRanking = OsgiServiceReferenceUtils.getServiceRanking(ref);

		toString = "ServiceWrapper[serviceId=" + serviceId + "|ref=" + reference + "]";
	}

	/**
	 * Constructs a new <code>ServiceWrapper</code> instance. Clone-like
	 * constructor.
	 * 
	 * @param wrapper
	 */
	public ServiceWrapper(ServiceWrapper wrapper) {
		Assert.notNull(wrapper);
		this.reference = wrapper.reference;
		this.serviceId = wrapper.serviceId;
		this.serviceRanking = wrapper.serviceRanking;
		this.toString = wrapper.toString;
		this.context = wrapper.context;
	}

	public boolean isServiceAlive() {
		return (!(context == null || reference.getBundle() == null));
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof ServiceWrapper) {
			return (reference.equals(((ServiceWrapper) obj).reference));
		}
		return false;
	}

	public int hashCode() {
		return ServiceWrapper.class.hashCode() * 13 + (int) serviceId;
	}

	public String toString() {
		return toString;
	}

	public Object getService() {
		return context.getService(reference);
	}

	public ServiceReference getReference() {
		return reference;
	}

	public long getServiceId() {
		return serviceId;
	}

	public int getServiceRanking() {
		return serviceRanking;
	}

	public int compareTo(Object o) {
		return compareTo((ServiceWrapper) o);
	}

	public int compareTo(ServiceWrapper other) {
		return (serviceId < other.serviceId ? -1 : (serviceId == other.serviceId ? 0 : 1));
	}
}