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

package org.springframework.osgi.iandt.serviceproxy;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.service.importer.support.internal.collection.OsgiServiceCollection;
import org.springframework.osgi.util.BundleDelegatingClassLoader;
import org.springframework.util.ClassUtils;

abstract class ServiceCollectionTest extends BaseIntegrationTest {

	protected String[] getTestBundlesNames() {
		return new String[] { "net.sourceforge.cglib, com.springsource.net.sf.cglib, 2.1.3" };
	}

	protected ServiceRegistration publishService(Object obj) throws Exception {
		return bundleContext.registerService(obj.getClass().getName(), obj, null);
	}

	public void testCGLIBAvailable() throws Exception {
		assertTrue(ClassUtils.isPresent("net.sf.cglib.proxy.Enhancer", DefaultAopProxyFactory.class.getClassLoader()));
	}

	protected Collection createCollection() {
		BundleDelegatingClassLoader classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundleContext.getBundle());

		OsgiServiceCollection collection = new OsgiServiceCollection(null, bundleContext, classLoader, null);
		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(classLoader);
			collection.setRequiredAtStartup(false);
			// collection.setInterfaces(new Class[] { Date.class });
			collection.afterPropertiesSet();
		}
		finally {
			Thread.currentThread().setContextClassLoader(tccl);
		}

		return collection;
	}

	public void testCollectionListener() throws Exception {
		Collection collection = createCollection();

		ServiceReference[] refs = bundleContext.getServiceReferences(null, null);

		assertEquals(refs.length, collection.size());
		int size = collection.size();
		// register a service
		long time = 123456;
		Date date = new Date(time);
		ServiceRegistration reg = publishService(date);
		try {
			assertEquals(size + 1, collection.size());
		}
		finally {
			reg.unregister();
		}

		assertEquals(size, collection.size());
	}

	public void testCollectionContent() throws Exception {
		Collection collection = createCollection();
		ServiceReference[] refs = bundleContext.getServiceReferences(null, null);

		assertEquals(refs.length, collection.size());
		int size = collection.size();

		// register a service
		long time = 123456;
		Date date = new Date(time);
		ServiceRegistration reg = publishService(date);
		try {
			assertEquals(size + 1, collection.size());
			// test service
			Iterator iter = collection.iterator();
			// reach our new service index
			for (int i = 0; i < size; i++) {
				iter.next();
			}
			Object myService = iter.next();
			// be sure to use classes loaded by the same CL
			assertTrue(myService instanceof Date);
			assertEquals(time, ((Date) myService).getTime());
		}
		finally {
			reg.unregister();
		}

		assertEquals(size, collection.size());
	}

}
