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

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.springframework.util.Assert;

/**
 * Simple {@link ServiceReference} proxy which simply does delegation, without
 * any extra features. It's main purpose is to allow the consistent behaviour
 * between dynamic and static proxies.
 * 
 * @author Costin Leau
 * 
 */
class StaticServiceReferenceProxy extends BaseServiceReferenceProxy {

	private static final int HASH_CODE = StaticServiceReferenceProxy.class.hashCode() * 13;

	private final ServiceReference target;


	/**
	 * Constructs a new <code>StaticServiceReferenceProxy</code> instance.
	 * 
	 * @param target service reference
	 */
	public StaticServiceReferenceProxy(ServiceReference target) {
		Assert.notNull(target);
		this.target = target;
	}

	public Bundle getBundle() {
		return target.getBundle();
	}

	public Object getProperty(String key) {
		return target.getProperty(key);
	}

	public String[] getPropertyKeys() {
		return target.getPropertyKeys();
	}

	public Bundle[] getUsingBundles() {
		return target.getUsingBundles();
	}

	public boolean isAssignableTo(Bundle bundle, String className) {
		return target.isAssignableTo(bundle, className);
	}

	public ServiceReference getTargetServiceReference() {
		return target;
	}

	public boolean equals(Object obj) {
		if (obj instanceof StaticServiceReferenceProxy) {
			StaticServiceReferenceProxy other = (StaticServiceReferenceProxy) obj;
			return (target.equals(other.target));
		}
		return false;
	}

	public int hashCode() {
		return HASH_CODE + target.hashCode();
	}

	public int compareTo(Object other) {
		return COMPARATOR.compare(target, other);
	}
}