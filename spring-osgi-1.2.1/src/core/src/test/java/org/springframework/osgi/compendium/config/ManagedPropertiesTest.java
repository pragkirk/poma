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

package org.springframework.osgi.compendium.config;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ManagedService;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.osgi.TestUtils;
import org.springframework.osgi.compendium.internal.cm.ManagedServiceInstanceTrackerPostProcessor;
import org.springframework.osgi.compendium.internal.cm.UpdateStrategy;
import org.springframework.osgi.context.support.BundleContextAwareProcessor;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceRegistration;

/**
 * @author Costin Leau
 * 
 */
public class ManagedPropertiesTest extends TestCase {

	private GenericApplicationContext appContext;
	private int unregistrationCounter;
	private int registrationCounter;


	protected void setUp() throws Exception {

		MockControl mc = MockControl.createNiceControl(Configuration.class);
		final Configuration cfg = (Configuration) mc.getMock();
		mc.expectAndReturn(cfg.getProperties(), new Properties());
		mc.replay();

		registrationCounter = 0;
		unregistrationCounter = 0;

		BundleContext bundleContext = new MockBundleContext() {

			// always return a ConfigurationAdmin
			public Object getService(ServiceReference reference) {
				return new MockConfigurationAdmin() {

					public Configuration getConfiguration(String pid) throws IOException {
						return cfg;
					}
				};
			}

			public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
				if (service instanceof ManagedService) {
					registrationCounter++;
					return new MockServiceRegistration(clazzes, properties) {

						public void unregister() {
							super.unregister();
							unregistrationCounter++;
						}
					};
				}
				return super.registerService(clazzes, service, properties);
			}

		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		reader.loadBeanDefinitions(new ClassPathResource("managedService.xml", getClass()));
		appContext.refresh();
	}

	protected void tearDown() throws Exception {
		appContext.close();
		appContext = null;
	}

	private ManagedServiceInstanceTrackerPostProcessor getTrackerForBean(String beanName) {
		return (ManagedServiceInstanceTrackerPostProcessor) appContext.getBean(ManagedServiceInstanceTrackerPostProcessor.class.getName()
				+ "#0#" + beanName);
	}

	public void testSimpleBeanTrackingBpp() throws Exception {
		ManagedServiceInstanceTrackerPostProcessor bpp = getTrackerForBean("simple");
		assertEquals("simple", TestUtils.getFieldValue(bpp, "pid"));
		assertNull(TestUtils.getFieldValue(bpp, "updateMethod"));
		assertNull(TestUtils.getFieldValue(bpp, "updateStrategy"));
	}

	public void testSimpleBeanWithNoNameTrackingBpp() throws Exception {
		ManagedServiceInstanceTrackerPostProcessor bpp = getTrackerForBean("org.springframework.osgi.compendium.OneSetter#0");
		assertEquals("non-name", TestUtils.getFieldValue(bpp, "pid"));
		assertNull(TestUtils.getFieldValue(bpp, "updateMethod"));
		assertNull(TestUtils.getFieldValue(bpp, "updateStrategy"));
	}

	public void testSimpleWUpdateBeanTrackingBpp() throws Exception {
		ManagedServiceInstanceTrackerPostProcessor bpp = getTrackerForBean("simpleWUpdate");
		assertEquals("simple", TestUtils.getFieldValue(bpp, "pid"));
		assertNull(TestUtils.getFieldValue(bpp, "updateMethod"));
	}

	public void testMultipleWUpdateBeanTrackingBpp() throws Exception {
		ManagedServiceInstanceTrackerPostProcessor bpp = getTrackerForBean("multipleWUpdate");
		assertEquals("multiple", TestUtils.getFieldValue(bpp, "pid"));
		assertNull(TestUtils.getFieldValue(bpp, "updateMethod"));
		assertEquals(UpdateStrategy.CONTAINER_MANAGED, TestUtils.getFieldValue(bpp, "updateStrategy"));
	}

	public void testBeanManagedTrackingBpp() throws Exception {
		ManagedServiceInstanceTrackerPostProcessor bpp = getTrackerForBean("beanManaged");
		assertEquals("bean-managed", TestUtils.getFieldValue(bpp, "pid"));
		assertEquals("update", TestUtils.getFieldValue(bpp, "updateMethod"));
		assertEquals(UpdateStrategy.BEAN_MANAGED, TestUtils.getFieldValue(bpp, "updateStrategy"));
	}

	public void testTrackingCleanup() throws Exception {
		assertEquals(5, registrationCounter);
		assertEquals(0, unregistrationCounter);
		appContext.close();
		assertEquals(5, unregistrationCounter);
	}
}