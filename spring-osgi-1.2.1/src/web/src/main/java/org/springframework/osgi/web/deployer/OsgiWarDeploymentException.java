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

package org.springframework.osgi.web.deployer;

import org.springframework.osgi.OsgiException;

/**
 * Exception thrown when the deployment or undeployment process of an OSGi war
 * fails.
 * 
 * @author Costin Leau
 */
public class OsgiWarDeploymentException extends OsgiException {

	private static final long serialVersionUID = 1888946061050974077L;


	/**
	 * Constructs a new <code>OsgiWarDeploymentException</code> instance.
	 * 
	 */
	public OsgiWarDeploymentException() {
		super();
	}

	/**
	 * Constructs a new <code>OsgiWarDeploymentException</code> instance.
	 * 
	 * @param message
	 * @param cause
	 */
	public OsgiWarDeploymentException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new <code>OsgiWarDeploymentException</code> instance.
	 * 
	 * @param message
	 */
	public OsgiWarDeploymentException(String message) {
		super(message);
	}

	/**
	 * Constructs a new <code>OsgiWarDeploymentException</code> instance.
	 * 
	 * @param cause
	 */
	public OsgiWarDeploymentException(Throwable cause) {
		super(cause);
	}
}
