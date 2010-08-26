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

import junit.framework.TestCase;

import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockFilter;
import org.springframework.osgi.mock.MockServiceReference;

/**
 * 
 * @author Costin Leau
 * 
 */
public class ServiceUnavailableExceptionTest extends TestCase {

	public void testServiceUnavailableExceptionFilter() {
		Filter filter = new MockFilter();
		ServiceUnavailableException exception = new ServiceUnavailableException(filter);
		assertFalse(filter.toString().equals(exception.getMessage()));
	}

	public void testServiceUnavailableExceptionNullFilter() {
		ServiceUnavailableException exception = new ServiceUnavailableException((Filter) null);
		assertNotNull(exception.getMessage());
	}

	public void testServiceUnavailableExceptionString() {
		String msg = "msg";
		ServiceUnavailableException exception = new ServiceUnavailableException(msg);
		assertFalse(msg.equals(exception.getMessage()));
	}

	public void testServiceUnavailableExceptionNullString() {
		ServiceUnavailableException exception = new ServiceUnavailableException((String) null);
		assertNotNull(exception.getMessage());
	}

	public void testServiceUnavailableExceptionServiceReference() {
		ServiceReference sr = new MockServiceReference();
		ServiceUnavailableException exception = new ServiceUnavailableException(sr);
		assertNotNull(exception.getMessage());
	}

	public void testServiceUnavailableExceptionNullServiceReference() {
		ServiceUnavailableException exception = new ServiceUnavailableException((ServiceReference) null);
		assertNotNull(exception.getMessage());
	}

}
