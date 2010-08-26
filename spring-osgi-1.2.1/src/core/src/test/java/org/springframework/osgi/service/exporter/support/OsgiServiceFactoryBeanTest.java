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
 *
 * Created on 26-Jan-2006 by Adrian Colyer
 */

package org.springframework.osgi.service.exporter.support;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceRegistration;

/**
 * @author Costin Leau
 */
public class OsgiServiceFactoryBeanTest extends TestCase {

	private OsgiServiceFactoryBean exporter;

	private ConfigurableBeanFactory beanFactory;

	private MockControl beanFactoryControl;

	private BundleContext bundleContext;

	private MockControl ctxCtrl;

	private BundleContext ctx;


	protected void setUp() throws Exception {
		exporter = new OsgiServiceFactoryBean();
		beanFactoryControl = MockControl.createControl(ConfigurableBeanFactory.class);
		beanFactory = (ConfigurableBeanFactory) this.beanFactoryControl.getMock();
		bundleContext = new MockBundleContext();
		ctxCtrl = MockControl.createControl(BundleContext.class);
		ctx = (BundleContext) ctxCtrl.getMock();

		exporter.setBeanFactory(beanFactory);
		exporter.setBundleContext(bundleContext);
	}

	protected void tearDown() throws Exception {
		exporter = null;
		bundleContext = null;
		ctxCtrl = null;
		ctx = null;
	}

	public void testInitWithoutBundleContext() throws Exception {
		exporter.setBundleContext(null);
		exporter.setTarget(new Object());

		try {
			this.exporter.afterPropertiesSet();
			fail("Expecting IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testInitWithoutBeanFactory() throws Exception {
		exporter.setBeanFactory(null);
		exporter.setTarget(new Object());

		try {
			this.exporter.afterPropertiesSet();
			fail("Expecting IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testInitWithoutTargetOrTargetReference() throws Exception {
		try {
			this.exporter.afterPropertiesSet();
			fail("Expecting IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testInitWithTargetAndTargetRerefence() throws Exception {
		exporter.setTarget(new Object());
		exporter.setTargetBeanName("costin");
		beanFactoryControl.expectAndReturn(beanFactory.isSingleton("costin"), false);
		beanFactoryControl.expectAndReturn(beanFactory.containsBean("costin"), true);
		beanFactoryControl.expectAndReturn(beanFactory.getType("costin"), Object.class);
		beanFactoryControl.replay();
		try {
			this.exporter.afterPropertiesSet();
			fail("Expecting IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testInitWithOnlyJustTarget() throws Exception {
		exporter.setTarget(new Object());
		exporter.setInterfaces(new Class[] { Object.class });
		exporter.afterPropertiesSet();
	}

	public void testAutoDetectClassesForPublishingDisabled() throws Exception {
		exporter.setAutoExport(AutoExport.DISABLED);
		Class[] clazz = AutoExport.DISABLED.getExportedClasses(Integer.class);
		assertNotNull(clazz);
		assertEquals(0, clazz.length);
	}

	public void testAutoDetectClassesForPublishingInterfaces() throws Exception {
		exporter.setAutoExport(AutoExport.INTERFACES);
		Class[] clazz = AutoExport.INTERFACES.getExportedClasses(HashMap.class);
		Class[] expected = new Class[] { Cloneable.class, Serializable.class, Map.class };

		assertTrue(compareArrays(expected, clazz));
	}

	public void testAutoDetectClassesForPublishingClassHierarchy() throws Exception {
		exporter.setAutoExport(AutoExport.CLASS_HIERARCHY);
		Class[] clazz = AutoExport.CLASS_HIERARCHY.getExportedClasses(HashMap.class);
		Class[] expected = new Class[] { HashMap.class, AbstractMap.class };
		assertTrue(compareArrays(expected, clazz));
	}

	public void testAutoDetectClassesForPublishingAll() throws Exception {
		exporter.setAutoExport(AutoExport.ALL_CLASSES);
		Class[] clazz = AutoExport.ALL_CLASSES.getExportedClasses(HashMap.class);
		Class[] expected = new Class[] { Map.class, Cloneable.class, Serializable.class, HashMap.class,
			AbstractMap.class };
		assertTrue(compareArrays(expected, clazz));
	}

	public void testRegisterServiceWithNullClasses() throws Exception {
		try {
			exporter.registerService(null, new Properties());
			fail("Expected to throw IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testRegisterServiceWOClasses() throws Exception {
		try {
			exporter.registerService(new Class[0], new Properties());
			fail("Expected to throw IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testRegisterService() throws Exception {
		Class[] clazz = new Class[] { Serializable.class, HashMap.class, Cloneable.class, Map.class,
			LinkedHashMap.class };

		String[] names = new String[clazz.length];

		for (int i = 0; i < clazz.length; i++) {
			names[i] = clazz[i].getName();
		}

		final Properties props = new Properties();
		final ServiceRegistration reg = new MockServiceRegistration();

		exporter.setBundleContext(new MockBundleContext() {

			public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
				assertTrue(service instanceof ServiceFactory);
				return reg;
			}
		});

		Object proxy = MockControl.createControl(ServiceFactory.class).getMock();
		exporter.setTarget(proxy);
		exporter.setInterfaces(new Class[] { ServiceFactory.class });
		String beanName = "boo";
		exporter.setTargetBeanName(beanName);

		beanFactoryControl.expectAndReturn(beanFactory.isSingleton(beanName), false);
		beanFactoryControl.expectAndReturn(beanFactory.containsBean(beanName), true);
		beanFactoryControl.expectAndReturn(beanFactory.getType(beanName), proxy.getClass());
		beanFactoryControl.replay();

		exporter.afterPropertiesSet();
		assertSame(reg, exporter.registerService(clazz, props));
	}

	public void testUnregisterWithNullServiceReg() throws Exception {
		exporter.unregisterService(null);
	}

	public void testUnregisterService() throws Exception {
		MockControl ctrl = MockControl.createControl(ServiceRegistration.class);
		ServiceRegistration reg = (ServiceRegistration) ctrl.getMock();

		reg.unregister();
		ctrl.replay();
		exporter.unregisterService(reg);
		ctrl.verify();
	}

	public void testUnregisterServiceAlreadyUnregistered() throws Exception {
		MockControl ctrl = MockControl.createControl(ServiceRegistration.class);
		ServiceRegistration reg = (ServiceRegistration) ctrl.getMock();

		reg.unregister();
		ctrl.setDefaultThrowable(new IllegalStateException());
		ctrl.replay();
		exporter.unregisterService(reg);
		ctrl.verify();
	}

	public void testLazyBeanServiceWithUsualBean() throws Exception {
		final ServiceRegistration reg = new MockServiceRegistration();
		final ServiceFactory[] factory = new ServiceFactory[1];

		Object service = new Object();

		BundleContext ctx = new MockBundleContext() {

			public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
				assertTrue(service instanceof ServiceFactory);
				factory[0] = (ServiceFactory) service;
				return reg;
			}
		};

		exporter.setBundleContext(ctx);

		String beanName = "fooBar";
		exporter.setTargetBeanName(beanName);
		exporter.setInterfaces(new Class[] { service.getClass() });
		beanFactoryControl.expectAndReturn(beanFactory.isSingleton(beanName), true);
		beanFactoryControl.expectAndReturn(beanFactory.containsBean(beanName), true);
		beanFactoryControl.expectAndReturn(beanFactory.getBean(beanName), service);
		beanFactoryControl.replay();
		exporter.afterPropertiesSet();
		exporter.registerService(new Class[] { service.getClass() }, new Properties());

		assertSame(service, factory[0].getService(null, null));
		beanFactoryControl.verify();
	}

	public void testLazyBeanServiceWithServiceFactoryBean() throws Exception {
		final ServiceRegistration reg = new MockServiceRegistration();
		final ServiceFactory[] factory = new ServiceFactory[1];

		final Object actualService = new Object();
		Object service = new ServiceFactory() {

			public Object getService(Bundle arg0, ServiceRegistration arg1) {
				return actualService;
			}

			public void ungetService(Bundle arg0, ServiceRegistration arg1, Object arg2) {
			}

		};

		BundleContext ctx = new MockBundleContext() {

			public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
				assertTrue(service instanceof ServiceFactory);
				factory[0] = (ServiceFactory) service;
				return reg;
			}
		};

		String beanName = "fooBar";

		beanFactoryControl.expectAndReturn(beanFactory.isSingleton(beanName), true);
		beanFactoryControl.expectAndReturn(beanFactory.containsBean(beanName), true);
		beanFactoryControl.expectAndReturn(beanFactory.getBean(beanName), service);
		beanFactoryControl.replay();

		exporter.setBundleContext(ctx);
		exporter.setBeanFactory(beanFactory);
		exporter.setTargetBeanName(beanName);
		exporter.setInterfaces(new Class[] { service.getClass() });

		exporter.afterPropertiesSet();
		exporter.registerService(new Class[] { actualService.getClass() }, new Properties());
		assertSame(actualService, factory[0].getService(null, null));
		beanFactoryControl.verify();
	}

	public void testLazyBeanServiceWithTargetObjectSet() throws Exception {
		final ServiceRegistration reg = new MockServiceRegistration();
		final ServiceFactory[] factory = new ServiceFactory[1];

		Object service = new Object();

		BundleContext ctx = new MockBundleContext() {

			public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
				assertTrue(service instanceof ServiceFactory);
				factory[0] = (ServiceFactory) service;
				return reg;
			}
		};

		exporter.setBundleContext(ctx);
		exporter.setBeanFactory(beanFactory);

		// give an actual target object not a target reference
		exporter.setTarget(service);
		exporter.setInterfaces(new Class[] { service.getClass() });

		beanFactoryControl.replay();
		exporter.afterPropertiesSet();
		exporter.registerService(new Class[] { service.getClass() }, new Properties());

		assertSame(service, factory[0].getService(null, null));
		beanFactoryControl.verify();
	}

	private boolean compareArrays(Object[] a, Object[] b) {
		if (a.length != b.length)
			return false;

		for (int i = 0; i < a.length; i++) {
			boolean found = false;
			for (int j = 0; j < b.length; j++) {
				if (a[i].equals(b[j])) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

	public void testServiceFactory() throws Exception {
		ServiceFactory factory = new ServiceFactory() {

			public Object getService(Bundle bundle, ServiceRegistration registration) {
				return null;
			}

			public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
			}
		};

		ctx = new MockBundleContext();
		exporter.setBundleContext(ctx);
		exporter.setBeanFactory(beanFactory);
		exporter.setInterfaces(new Class[] { Serializable.class, Cloneable.class });
		exporter.setTarget(factory);
		beanFactoryControl.replay();
		exporter.afterPropertiesSet();
	}
}
