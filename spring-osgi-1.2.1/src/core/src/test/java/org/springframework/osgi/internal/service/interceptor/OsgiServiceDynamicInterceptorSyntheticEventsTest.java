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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceDynamicInterceptor;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;

public class OsgiServiceDynamicInterceptorSyntheticEventsTest extends TestCase {

	private ServiceDynamicInterceptor interceptor;

	private MockBundleContext bundleContext;

	private OsgiServiceLifecycleListener listener;

	private ServiceReference ref1, ref2, ref3;

	private Object service1, service2, service3;

	private List bindServices, unbindServices;

	private Object serviceProxy = new Object();


	protected void setUp() throws Exception {

		// generate services references in reverse order to have them increasing service ids
		ref3 = new MockServiceReference();
		ref2 = new MockServiceReference();
		ref1 = new MockServiceReference();

		service1 = "service 1";
		service2 = "service 2";
		service3 = "service 3";

		final Map services = new HashMap();
		services.put(ref1, service1);
		services.put(ref2, service2);
		services.put(ref3, service3);

		bindServices = new ArrayList();
		unbindServices = new ArrayList();

		bundleContext = new MockBundleContext() {

			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[] { ref1, ref2, ref3 };
			}

			public ServiceReference getServiceReference(String clazz) {
				return ref3;
			}

			public Object getService(ServiceReference reference) {
				return services.get(reference);
			}
		};

		bundleContext.getBundle();

		listener = new OsgiServiceLifecycleListener() {

			public void bind(Object service, Map properties) throws Exception {
				bindServices.add(service);
			}

			public void unbind(Object service, Map properties) throws Exception {
				unbindServices.add(service);
			}

		};

		interceptor = new ServiceDynamicInterceptor(bundleContext, null, null, getClass().getClassLoader());
		interceptor.setRequiredAtStartup(false);
		interceptor.setProxy(serviceProxy);
		interceptor.setListeners(new OsgiServiceLifecycleListener[] { listener });
		interceptor.setServiceImporter(new Object());

		interceptor.setRetryTimeout(1);
	}

	protected void tearDown() throws Exception {
		interceptor = null;
		bundleContext = null;
		listener = null;
	}

	public void testGetServices() throws Exception {
		assertSame(ref3, OsgiServiceReferenceUtils.getServiceReference(bundleContext, (String) null));
	}

	public void testOnlyOneSyntheticEventOnRegistrationIfMultipleServicesPresent() throws Exception {
		interceptor.afterPropertiesSet();
		assertEquals(1, bindServices.size());
		assertEquals(0, unbindServices.size());
		assertSame(serviceProxy, bindServices.get(0));
	}

	public void testOnlyOneSyntheticEventOnUnregistrationIfMultipleServicesPresent() throws Exception {
		interceptor.afterPropertiesSet();
		interceptor.destroy();
		assertEquals(1, bindServices.size());
		assertEquals(1, unbindServices.size());
		assertSame(serviceProxy, unbindServices.get(0));
	}
}
