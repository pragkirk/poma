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

package org.springframework.osgi.service;

import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;

/**
 * Exception indicating that an OSGi service became unavailable/unregistered.
 * Normally this exception is used to indicate that no suitable replacement is
 * found (in case service rebinding is actually performed).
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
public class ServiceUnavailableException extends ServiceException {

	private static final long serialVersionUID = -3479837278220329490L;


	/**
	 * Constructs a new <code>ServiceUnavailableException</code> instance.
	 * 
	 * @param filter service filter
	 */
	public ServiceUnavailableException(Filter filter) {
		super("service matching filter=[" + filter + "] unavailable");
	}

	/**
	 * Constructs a new <code>ServiceUnavailableException</code> instance.
	 * 
	 * @param filter service filter (passed as a string)
	 */
	public ServiceUnavailableException(String filter) {
		super("service matching filter=[" + filter + "] unavailable");
	}

	/**
	 * Constructs a new <code>ServiceUnavailableException</code> instance.
	 * 
	 * @param reference service reference
	 */
	public ServiceUnavailableException(ServiceReference reference) {
		super("service with id=["
				+ (reference == null ? "null" : "" + OsgiServiceReferenceUtils.getServiceId(reference))
				+ "] unavailable");
	}

}
