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

package org.springframework.osgi.service.importer.support;

import java.io.Serializable;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.core.InfrastructureProxy;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.service.importer.ImportedOsgiServiceProxy;

/**
 * Unit test regarding the importers proxies and spring infrastructure proxy.
 * 
 * @author Costin Leau
 */
public class InfrastructureProxyTest extends TestCase {

	private StaticServiceProxyCreator proxyCreator;

	private final Class[] classes = new Class[] { Serializable.class, Comparable.class };


	private StaticServiceProxyCreator createProxyCreator(BundleContext ctx, Class[] classes) {
		ClassLoader cl = getClass().getClassLoader();
		if (ctx == null) {
			ctx = new MockBundleContext();
		}
		return new StaticServiceProxyCreator(classes, cl, cl, ctx, ImportContextClassLoader.UNMANAGED, true);
	}

	protected void setUp() throws Exception {
		proxyCreator = createProxyCreator(null, classes);
	}

	protected void tearDown() throws Exception {
		proxyCreator = null;
	}

	public void testCreatedProxy() throws Exception {
		MockServiceReference ref = new MockServiceReference();

		Object proxy = proxyCreator.createServiceProxy(ref).proxy;
		assertTrue(proxy instanceof ImportedOsgiServiceProxy);
		assertTrue(proxy instanceof InfrastructureProxy);
	}

	public void testTargetProxy() throws Exception {
		final MockServiceReference ref = new MockServiceReference();
		final Object service = new Object();

		MockBundleContext ctx = new MockBundleContext() {

			public ServiceReference getServiceReference(String clazz) {
				return ref;
			}

			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[] { ref };
			}

			public Object getService(ServiceReference reference) {
				return (reference == ref ? service : super.getService(reference));
			}
		};

		proxyCreator = createProxyCreator(ctx, classes);
		InfrastructureProxy proxy = (InfrastructureProxy) proxyCreator.createServiceProxy(ref).proxy;
		assertEquals(service, proxy.getWrappedObject());
		InfrastructureProxy anotherProxy = (InfrastructureProxy) proxyCreator.createServiceProxy(new MockServiceReference()).proxy;
		assertFalse(proxy.equals(anotherProxy));
		assertFalse(anotherProxy.getWrappedObject().equals(proxy.getWrappedObject()));
	}
}
