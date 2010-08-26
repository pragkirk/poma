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

import java.util.Date;

import org.aopalliance.aop.Advice;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.osgi.service.ServiceUnavailableException;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceDynamicInterceptor;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
import org.springframework.osgi.util.BundleDelegatingClassLoader;
import org.springframework.osgi.util.OsgiFilterUtils;
import org.springframework.util.ClassUtils;

/**
 * @author Costin Leau
 * 
 */
public abstract class ServiceProxyTst extends AbstractConfigurableBundleCreatorTests {

	protected String[] getTestBundlesNames() {
		return new String[] { "net.sourceforge.cglib, com.springsource.net.sf.cglib, 2.1.3" };
	}

	protected String getManifestLocation() {
		return null;
	}

	private ServiceRegistration publishService(Object obj) throws Exception {
		return bundleContext.registerService(obj.getClass().getName(), obj, null);
	}

	private Object createProxy(final Class clazz, Advice cardinalityInterceptor) {
		ProxyFactory factory = new ProxyFactory();
		factory.setProxyTargetClass(true);
		factory.setOptimize(true);
		factory.setTargetClass(clazz);

		factory.addAdvice(cardinalityInterceptor);
		factory.setFrozen(true);

		return factory.getProxy(ProxyFactory.class.getClassLoader());
	}

	private Advice createCardinalityAdvice(Class clazz) {
		ClassLoader classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundleContext.getBundle());
		ServiceDynamicInterceptor interceptor = new ServiceDynamicInterceptor(bundleContext, null,
			OsgiFilterUtils.createFilter(OsgiFilterUtils.unifyFilter(clazz, null)), classLoader);
		// fast retry
		interceptor.setRequiredAtStartup(true);
		interceptor.afterPropertiesSet();
		interceptor.getRetryTemplate().reset(1);
		return interceptor;

	}

	public void testCglibLibraryVisibility() {
		// note that cglib is not declared inside this bundle but should be seen
		// by spring-core (which contains the util classes)
		assertTrue(ClassUtils.isPresent("net.sf.cglib.proxy.Enhancer", ProxyFactory.class.getClassLoader()));
	}

	public void testDynamicEndProxy() throws Exception {
		long time = 123456;
		Date date = new Date(time);
		ServiceRegistration reg = publishService(date);
		BundleContext ctx = bundleContext;

		try {
			ServiceReference ref = ctx.getServiceReference(Date.class.getName());
			assertNotNull(ref);
			Date proxy = (Date) createProxy(Date.class, createCardinalityAdvice(Date.class));
			assertEquals(time, proxy.getTime());
			// take down service
			reg.unregister();
			// reference is invalid
			assertNull(ref.getBundle());

			try {
				proxy.getTime();
				fail("should have thrown exception");
			}
			catch (ServiceUnavailableException sue) {
				// service failed
			}

			// rebind the service
			reg = publishService(date);
			// retest the service
			assertEquals(time, proxy.getTime());
		}
		finally {
			if (reg != null)
				try {
					reg.unregister();
				}
				catch (Exception ex) {
					// ignore
				}
		}
	}

}
