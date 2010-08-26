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
package org.springframework.osgi.internal.service.interceptor;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInvocation;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.service.ServiceUnavailableException;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceStaticInterceptor;

/**
 * @author Costin Leau
 * 
 */
public class OsgiServiceStaticInterceptorTest extends TestCase {

	private ServiceStaticInterceptor interceptor;

	private Object service;

	private ClassLoader classLoader = getClass().getClassLoader();

	protected void setUp() throws Exception {
		service = new Object();

		ServiceReference reference = new MockServiceReference();

		BundleContext ctx = new MockBundleContext() {
			public Object getService(ServiceReference reference) {
				return service;
			}
		};

		interceptor = new ServiceStaticInterceptor(ctx, reference);
	}

	protected void tearDown() throws Exception {
		service = null;
		interceptor = null;
	}

	public void testNullWrapper() throws Exception {
		try {
			interceptor = new ServiceStaticInterceptor(null, null);
			fail("expected exception");
		}
		catch (RuntimeException ex) {
			// expected
		}
	}

	public void testInvocationOnService() throws Throwable {
		Object target = new Object();
		Method m = target.getClass().getDeclaredMethod("hashCode", null);

		MethodInvocation invocation = new MockMethodInvocation(m);
		assertEquals(new Integer(service.hashCode()), interceptor.invoke(invocation));
	}

	public void testInvocationWhenServiceNA() throws Throwable {
		// service n/a
		ServiceReference reference = new MockServiceReference() {
			public Bundle getBundle() {
				return null;
			}
		};

		interceptor = new ServiceStaticInterceptor(new MockBundleContext(), reference);

		Object target = new Object();
		Method m = target.getClass().getDeclaredMethod("hashCode", null);

		MethodInvocation invocation = new MockMethodInvocation(m);
		try {
			interceptor.invoke(invocation);
			fail("should have thrown exception");
		}
		catch (ServiceUnavailableException ex) {
			// expected
		}
	}
}
