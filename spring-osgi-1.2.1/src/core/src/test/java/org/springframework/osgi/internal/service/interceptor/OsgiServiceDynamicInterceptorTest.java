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
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockFilter;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.service.ServiceUnavailableException;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceDynamicInterceptor;

/**
 * @author Costin Leau
 * 
 */
public class OsgiServiceDynamicInterceptorTest extends TestCase {

	private ServiceDynamicInterceptor interceptor;

	private ServiceReference reference, ref2, ref3;

	private Object service, serv2, serv3;

	private String serv2Filter;

	private String nullFilter;

	private ServiceListener listener;

	private BundleContext ctx;


	protected void setUp() throws Exception {
		service = new Object();
		serv2 = new Object();
		serv3 = new Object();

		reference = new MockServiceReference();
		ref2 = new MockServiceReference();
		ref3 = new MockServiceReference();

		serv2Filter = "serv2";
		nullFilter = "null";

		// special mock context
		// 1. will return no service for the null Filter ("null")
		// 2. will return ref2 for filter serv2Filter

		// the same goes with getService
		ctx = new MockBundleContext() {

			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				if (serv2Filter.equals(filter))
					return new ServiceReference[] { ref2 };
				else if (nullFilter.equals(filter)) {
					return null;
				}
				return new ServiceReference[] { reference };
			}

			public Object getService(ServiceReference ref) {
				if (reference == ref) {
					return service;
				}
				if (ref2 == ref) {
					return serv2;
				}

				if (ref3 == ref) {
					return serv3;
				}

				// simulate a non available service
				return null;
			}

			public void addServiceListener(ServiceListener list, String filter) throws InvalidSyntaxException {
				listener = list;
			}
		};

		createInterceptor(null);
	}

	protected void tearDown() throws Exception {
		service = null;
		interceptor = null;
		listener = null;
	}

	private void createInterceptor(Filter filter) {
		interceptor = new ServiceDynamicInterceptor(ctx, null, filter, getClass().getClassLoader());

		interceptor.setRequiredAtStartup(false);

		interceptor.setRetryTimeout(1);
		interceptor.setProxy(new Object());
		interceptor.setServiceImporter(new Object());

		interceptor.afterPropertiesSet();
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.service.interceptor.ServiceDynamicInterceptor#OsgiServiceDynamicInterceptor()}.
	 */
	public void testOsgiServiceDynamicInterceptor() {
		assertNotNull(interceptor.getRetryTemplate());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.service.interceptor.ServiceDynamicInterceptor#lookupService()}.
	 */
	public void testLookupService() throws Throwable {
		Object serv = interceptor.getTarget();
		assertSame(service, serv);
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.service.interceptor.ServiceDynamicInterceptor#doInvoke(java.lang.Object, org.aopalliance.intercept.MethodInvocation)}.
	 */
	public void testDoInvoke() throws Throwable {
		Object target = new Object();
		Method m = target.getClass().getDeclaredMethod("hashCode", null);

		MethodInvocation invocation = new MockMethodInvocation(m);
		assertEquals(new Integer(service.hashCode()), interceptor.invoke(invocation));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.service.interceptor.ServiceDynamicInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)}.
	 */
	public void testInvocationWhenServiceNA() throws Throwable {
		// service n/a

		Object target = new Object();
		Method m = target.getClass().getDeclaredMethod("hashCode", null);

		MethodInvocation invocation = new MockMethodInvocation(m);
		ServiceReference oldRef = reference;
		reference = null;

		try {
			interceptor.invoke(invocation);
			fail("should have thrown exception");
		}
		catch (ServiceUnavailableException ex) {
			// expected
		}

		// service is up
		reference = oldRef;

		assertEquals(new Integer(service.hashCode()), interceptor.invoke(invocation));
	}

	public void testInvocationTimeoutWhenServiceNA() throws Throwable {
		// service n/a

		Object target = new Object();
		Method m = target.getClass().getDeclaredMethod("hashCode", null);

		MethodInvocation invocation = new MockMethodInvocation(m);
		createInterceptor(new MockFilter(nullFilter));
		ServiceEvent event = new ServiceEvent(ServiceEvent.UNREGISTERING, reference);
		listener.serviceChanged(event);
		interceptor.getRetryTemplate().reset(3000);
		long now = System.currentTimeMillis();
		try {
			interceptor.invoke(invocation);
			fail("should have thrown exception");
		}
		catch (ServiceUnavailableException ex) {
			// expected
		}

		// service is up
		interceptor.getRetryTemplate().reset(1);

		assertTrue("Call did not block for 3000ms, actually blocked for " + (System.currentTimeMillis() - now) + "ms",
			(System.currentTimeMillis() - now) >= 3000);
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.service.interceptor.ServiceDynamicInterceptor#getTarget()}.
	 */
	public void testGetTarget() throws Throwable {
		// add service
		ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, reference);
		listener.serviceChanged(event);

		Object target = interceptor.getTarget();
		assertSame("target not properly discovered", service, target);
	}

	public void testGetTargetWhenMultipleServicesAreAvailable() throws Throwable {
		// add service
		ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, reference);
		listener.serviceChanged(event);

		event = new ServiceEvent(ServiceEvent.REGISTERED, ref2);
		listener.serviceChanged(event);

		Object target = interceptor.getTarget();
		assertSame("target not properly discovered", service, target);

		createInterceptor(new MockFilter(serv2Filter));
		event = new ServiceEvent(ServiceEvent.UNREGISTERING, reference);
		listener.serviceChanged(event);

		try {
			target = interceptor.getTarget();
		}
		catch (ServiceUnavailableException sue) {
			fail("target not rebound after service is down");
		}

		assertSame("wrong service rebound", serv2, target);
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.service.interceptor.ServiceDynamicInterceptor#afterPropertiesSet()}.
	 */
	public void testAfterPropertiesSet() {
		assertNotNull("should have initialized listener", listener);
	}

	/**
	 * HSH - Mandatory cardinality is enforced by the extender in the wait-for
	 * semantic regarding dependent services of cardinality {1..}
	 * 
	 * public void testMandatoryCardinality() { MockBundleContext ctx = new
	 * MockBundleContext() { public ServiceReference[]
	 * getServiceReferences(String clazz, String filter) throws
	 * InvalidSyntaxException { return null; } }; interceptor = new
	 * OsgiServiceDynamicInterceptor(ctx, ImportContextClassLoader.UNMANAGED);
	 * interceptor.setFilter(new MockFilter()); RetryTemplate template = new
	 * RetryTemplate(); template.setRetryNumbers(1); template.setWaitTime(10);
	 * interceptor.setRetryTemplate(template); try {
	 * interceptor.afterPropertiesSet(); fail("expected exception"); } catch
	 * (ServiceUnavailableException sue) { // expected } }
	 */
}
