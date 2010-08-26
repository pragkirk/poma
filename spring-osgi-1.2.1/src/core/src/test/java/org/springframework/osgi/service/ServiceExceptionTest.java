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

/**
 * 
 * @author Costin Leau
 * 
 */
public class ServiceExceptionTest extends TestCase {

	public void testServiceException() {
		ServiceException exception = new ServiceException();
		assertNull(exception.getCause());
		assertNull(exception.getMessage());
	}

	public void testServiceExceptionStringThrowable() {
		String msg = "msg";
		Exception ex = new Exception();
		ServiceException exception = new ServiceException(msg, ex);
		assertEquals(msg, exception.getMessage());
		assertEquals(ex, exception.getCause());
	}

	public void testServiceExceptionString() {
		String msg = "msg";
		ServiceException exception = new ServiceException(msg);
		assertEquals(msg, exception.getMessage());
	}

	public void testServiceExceptionThrowable() {
		Exception ex = new Exception();
		ServiceException exception = new ServiceException(ex);
		assertSame(ex, exception.getCause());
	}

}
