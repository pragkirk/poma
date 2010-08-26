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

import junit.framework.TestCase;

/**
 * @author Costin Leau
 * 
 */
public class OsgiWarDeploymentExceptionTest extends TestCase {

	/**
	 * Test method for
	 * {@link org.springframework.osgi.web.deployer.OsgiWarDeploymentException#OsgiWarDeploymentException()}.
	 */
	public void testOsgiWarDeploymentException() {
		OsgiWarDeploymentException exception = new OsgiWarDeploymentException();
		assertNull(exception.getCause());
		assertNull(exception.getMessage());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.web.deployer.OsgiWarDeploymentException#OsgiWarDeploymentException(java.lang.String, java.lang.Throwable)}.
	 */
	public void testOsgiWarDeploymentExceptionStringThrowable() {
		String msg = "msg";
		Exception ex = new Exception();
		OsgiWarDeploymentException exception = new OsgiWarDeploymentException(msg, ex);
		assertEquals(msg, exception.getMessage());
		assertEquals(ex, exception.getCause());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.web.deployer.OsgiWarDeploymentException#OsgiWarDeploymentException(java.lang.String)}.
	 */
	public void testOsgiWarDeploymentExceptionString() {
		String msg = "msg";
		OsgiWarDeploymentException exception = new OsgiWarDeploymentException(msg);
		assertEquals(msg, exception.getMessage());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.web.deployer.OsgiWarDeploymentException#OsgiWarDeploymentException(java.lang.Throwable)}.
	 */
	public void testOsgiWarDeploymentExceptionThrowable() {
		Exception ex = new Exception();
		OsgiWarDeploymentException exception = new OsgiWarDeploymentException(ex);
		assertSame(ex, exception.getCause());
	}

}
