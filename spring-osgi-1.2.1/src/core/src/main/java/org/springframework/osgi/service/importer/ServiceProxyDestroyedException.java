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

package org.springframework.osgi.service.importer;

import org.springframework.osgi.service.ServiceException;

/**
 * Exception indicating the accessed OSGi service proxy has been destroyed.
 * 
 * Usually this exception is thrown when certain operations (normally those that
 * involve accessing the proxy target service ) are called on an OSGi service
 * proxy that has been destroyed.
 * 
 * @author Costin Leau
 */
public class ServiceProxyDestroyedException extends ServiceException {

	private static final long serialVersionUID = 1773620969162174421L;


	/**
	 * Constructs a new <code>ServiceProxyDestroyedException</code> instance.
	 * 
	 */
	public ServiceProxyDestroyedException() {
		super("service proxy has been destroyed");
	}

	/**
	 * Constructs a new <code>ServiceProxyDestroyedException</code> instance.
	 * 
	 * @param message
	 * @param cause
	 */
	public ServiceProxyDestroyedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new <code>ServiceProxyDestroyedException</code> instance.
	 * 
	 * @param message
	 */
	public ServiceProxyDestroyedException(String message) {
		super(message);
	}

	/**
	 * Constructs a new <code>ServiceProxyDestroyedException</code> instance.
	 * 
	 * @param cause
	 */
	public ServiceProxyDestroyedException(Throwable cause) {
		super(cause);
	}
}
