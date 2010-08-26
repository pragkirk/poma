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
 * Synchronized, swapping {@link ServiceReference} implementation that delegates
 * to an underlying implementation which can be swapped at runtime.
 * 
 * <strong>Note:</strong> this class is thread-safe.
 * 
 * @author Costin Leau
 * 
 */
class SwappingServiceReferenceProxy extends BaseServiceReferenceProxy {

	private static final int HASH_CODE = SwappingServiceReferenceProxy.class.hashCode() * 13;

	private ServiceReference delegate;


	synchronized ServiceReference swapDelegates(ServiceReference newDelegate) {
		Assert.notNull(newDelegate);
		ServiceReference old = this.delegate;
		this.delegate = newDelegate;

		return old;
	}

	public synchronized Bundle getBundle() {
		return (delegate == null ? null : delegate.getBundle());
	}

	public synchronized Object getProperty(String key) {
		return (delegate == null ? null : delegate.getProperty(key));
	}

	public synchronized String[] getPropertyKeys() {
		return (delegate == null ? new String[0] : delegate.getPropertyKeys());
	}

	public synchronized Bundle[] getUsingBundles() {
		return (delegate == null ? new Bundle[0] : delegate.getUsingBundles());
	}

	public synchronized boolean isAssignableTo(Bundle bundle, String className) {
		return (delegate == null ? false : delegate.isAssignableTo(bundle, className));
	}

	public synchronized ServiceReference getTargetServiceReference() {
		return delegate;
	}

	public synchronized boolean equals(Object obj) {
		if (obj instanceof SwappingServiceReferenceProxy) {
			SwappingServiceReferenceProxy other = (SwappingServiceReferenceProxy) obj;
			return (delegate == null ? other.delegate == null : delegate.equals(other.delegate));
		}
		return false;
	}

	public synchronized int hashCode() {
		return HASH_CODE + (delegate == null ? 0 : delegate.hashCode());
	}

	public synchronized int compareTo(Object other) {
		if (other instanceof SwappingServiceReferenceProxy)
			return COMPARATOR.compare(delegate, ((SwappingServiceReferenceProxy) other).delegate);
		return COMPARATOR.compare(delegate, other);
	}
}