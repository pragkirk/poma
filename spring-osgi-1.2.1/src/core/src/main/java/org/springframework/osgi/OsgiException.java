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
package org.springframework.osgi;

/**
 * Root of the Spring DM exception hierarchy.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiException extends RuntimeException {

	private static final long serialVersionUID = -2484573525557843394L;

	/**
	 * Default constructor using no specified cause or detailed message.
	 */
	public OsgiException() {
		super();
	}

	/**
	 * Constructs a new <code>OsgiException</code> instance.
	 * 
	 * @param message detailed message
	 * @param cause exception cause
	 */
	public OsgiException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new <code>OsgiException</code> instance.
	 * 
	 * @param message detailed message
	 */
	public OsgiException(String message) {
		super(message);
	}

	/**
	 * Constructs a new <code>OsgiException</code> instance.
	 * 
	 * @param cause exception cause
	 */
	public OsgiException(Throwable cause) {
		super(cause);
	}

}
