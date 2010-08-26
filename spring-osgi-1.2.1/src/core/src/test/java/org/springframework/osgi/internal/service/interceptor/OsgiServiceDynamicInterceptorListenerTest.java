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

import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceDynamicInterceptor;

/**
 * Test for the listener rebinding behavior. Makes sure the bind/unbind contract
 * is properly respected.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceDynamicInterceptorListenerTest extends TestCase {

	private ServiceDynamicInterceptor interceptor;

	private OsgiServiceLifecycleListener listener;

	private MockBundleContext bundleContext;

	private ServiceReference[] refs;


	protected void setUp() throws Exception {
		listener = new SimpleTargetSourceLifecycleListener();

		refs = new ServiceReference[] { new MockServiceReference() };

		bundleContext = new MockBundleContext() {

			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return refs;
			}
		};

		interceptor = new ServiceDynamicInterceptor(bundleContext, null, null, getClass().getClassLoader());
		interceptor.setListeners(new OsgiServiceLifecycleListener[] { listener });
		interceptor.setRequiredAtStartup(false);
		interceptor.setProxy(new Object());
		interceptor.setServiceImporter(new Object());

		interceptor.setRetryTimeout(1);

		SimpleTargetSourceLifecycleListener.BIND = 0;
		SimpleTargetSourceLifecycleListener.UNBIND = 0;
	}

	protected void tearDown() throws Exception {
		interceptor = null;
		listener = null;
		bundleContext = null;
	}

	public void testBind() {
		assertEquals(0, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);

		interceptor.afterPropertiesSet();

		assertEquals(1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);
	}

	public void testUnbind() {
		interceptor.afterPropertiesSet();

		assertEquals(1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);

		ServiceListener sl = (ServiceListener) bundleContext.getServiceListeners().iterator().next();

		// save old ref and invalidate it so new services are not found
		ServiceReference oldRef = refs[0];
		refs = null;

		sl.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, oldRef));

		assertEquals(1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(1, SimpleTargetSourceLifecycleListener.UNBIND);
	}

	public void testRebindWhenNewServiceAppears() {
		interceptor.afterPropertiesSet();

		ServiceListener sl = (ServiceListener) bundleContext.getServiceListeners().iterator().next();

		Dictionary props = new Hashtable();
		// increase service ranking
		props.put(Constants.SERVICE_RANKING, new Integer(10));

		ServiceReference ref = new MockServiceReference(null, props, null);

		ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, ref);

		assertEquals(1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);

		sl.serviceChanged(event);

		assertEquals(2, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);
	}

	public void testRebindWhenServiceGoesDownButAReplacementIsFound() {
		interceptor.afterPropertiesSet();

		assertEquals(1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);

		ServiceListener sl = (ServiceListener) bundleContext.getServiceListeners().iterator().next();

		// unregister the old service
		sl.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, refs[0]));

		// a new one is found since the mock context will return one again
		assertEquals(2, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);
	}

}
