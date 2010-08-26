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

package org.springframework.osgi.context.support;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.osgi.io.OsgiBundleResource;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceRegistration;
import org.springframework.osgi.util.BundleDelegatingClassLoader;

/**
 * @author Costin Leau
 * 
 */
public class AbstractRefreshableOsgiBundleApplicationContextTest extends TestCase {

	private AbstractOsgiBundleApplicationContext context;
	private Bundle bundle;
	private BundleContext bundleCtx;
	private MockControl bundleCtrl, bundleCtxCtrl;


	protected void setUp() throws Exception {
		context = new AbstractOsgiBundleApplicationContext() {

			protected void loadBeanDefinitions(DefaultListableBeanFactory arg0) throws IOException, BeansException {
			}
		};

		bundleCtxCtrl = MockControl.createStrictControl(BundleContext.class);
		bundleCtx = (BundleContext) bundleCtxCtrl.getMock();

		bundleCtrl = MockControl.createNiceControl(Bundle.class);
		bundle = (Bundle) bundleCtrl.getMock();

		bundleCtxCtrl.expectAndReturn(bundleCtx.getBundle(), bundle);

	}

	protected void tearDown() throws Exception {
		context = null;
	}

	public void testBundleContext() throws Exception {

		String location = "osgibundle://someLocation";
		Resource bundleResource = new OsgiBundleResource(bundle, location);

		Dictionary dict = new Properties();
		bundleCtrl.expectAndReturn(bundle.getHeaders(), dict);
		bundleCtrl.expectAndReturn(bundle.getSymbolicName(), "symName", MockControl.ONE_OR_MORE);
		bundleCtrl.replay();
		bundleCtxCtrl.replay();

		context.setBundleContext(bundleCtx);
		assertSame(bundle, context.getBundle());
		assertSame(bundleCtx, context.getBundleContext());

		ClassLoader loader = context.getClassLoader();
		assertTrue(loader instanceof BundleDelegatingClassLoader);

		// do some resource loading
		assertEquals(bundleResource, context.getResource(location));

		bundleCtrl.verify();
		bundleCtxCtrl.verify();
	}

	public void testServicePublicationBetweenRefreshes() throws Exception {
		// [0] = service registration
		// [1] = service unregistration
		
		final int[] counters = new int[] { 0, 0 };

		MockBundleContext mCtx = new MockBundleContext() {

			public ServiceRegistration registerService(String clazz[], Object service, Dictionary properties) {
				counters[0]++;
				return new MockServiceRegistration(clazz, properties) {

					public void unregister() {
						counters[1]++;
					}
				};
			}

		};
		context.setBundleContext(mCtx);

		assertEquals(counters[0], 0);
		assertEquals(counters[1], 0);

		context.refresh();
		assertEquals(counters[0], 1);
		assertEquals(counters[1], 0);

		context.refresh();
		assertEquals(counters[0], 2);
		assertEquals(counters[1], 1);
	}
}