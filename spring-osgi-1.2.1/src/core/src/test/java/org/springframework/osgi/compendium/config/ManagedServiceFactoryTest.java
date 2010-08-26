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
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.osgi.TestUtils;
import org.springframework.osgi.compendium.internal.cm.ManagedServiceFactoryFactoryBean;
import org.springframework.osgi.compendium.internal.cm.UpdateStrategy;
import org.springframework.osgi.context.support.BundleContextAwareProcessor;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.service.exporter.support.AutoExport;
import org.springframework.osgi.service.exporter.support.ExportContextClassLoader;

/**
 * Parsing test for ManagedServiceFactory/<managed-service-factory/>
 * 
 * @author Costin Leau
 */
public class ManagedServiceFactoryTest extends TestCase {

	private GenericApplicationContext appContext;


	protected void setUp() throws Exception {

		MockControl mc = MockControl.createNiceControl(Configuration.class);
		final Configuration cfg = (Configuration) mc.getMock();
		mc.expectAndReturn(cfg.getProperties(), new Properties());
		mc.replay();

		BundleContext bundleContext = new MockBundleContext() {

			// always return a ConfigurationAdmin
			public Object getService(ServiceReference reference) {
				return new MockConfigurationAdmin() {

					public Configuration getConfiguration(String pid) throws IOException {
						return cfg;
					}
				};
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		reader.loadBeanDefinitions(new ClassPathResource("managedServiceFactory.xml", getClass()));
		appContext.refresh();
	}

	protected void tearDown() throws Exception {
		appContext.close();
		appContext = null;
	}

	public void testBasicParsing() throws Exception {
		Object factory = appContext.getBean("&simple");
		assertTrue(factory instanceof ManagedServiceFactoryFactoryBean);
	}

	public void testBasicExportAttributes() throws Exception {
		Object factory = appContext.getBean("&simple");
		Object intfs = TestUtils.getFieldValue(factory, "interfaces");
		assertTrue(Arrays.equals((Object[]) intfs, new Class[] { Object.class }));
		Object autoExport = TestUtils.getFieldValue(factory, "autoExport");
		assertEquals(AutoExport.ALL_CLASSES, autoExport);
	}

	public void testNestedInterfaceElement() throws Exception {
		Object factory = appContext.getBean("&ccl");
		Object intfs = TestUtils.getFieldValue(factory, "interfaces");
		assertTrue(Arrays.equals((Object[]) intfs, new Class[] { Map.class, Serializable.class }));
	}

	public void testCCLAttribute() throws Exception {
		Object factory = appContext.getBean("&ccl");
		Object ccl = TestUtils.getFieldValue(factory, "ccl");
		assertEquals(ExportContextClassLoader.SERVICE_PROVIDER, ccl);
	}

	public void testContainerUpdateAttr() throws Exception {
		Object factory = appContext.getBean("&container-update");
		Object strategy = TestUtils.getFieldValue(factory, "updateStrategy");
		assertEquals(UpdateStrategy.CONTAINER_MANAGED, strategy);
	}

	public void testBeanManagedUpdateAttr() throws Exception {
		Object factory = appContext.getBean("&bean-update");
		Object strategy = TestUtils.getFieldValue(factory, "updateStrategy");
		Object method = TestUtils.getFieldValue(factory, "updateMethod");
		assertEquals(UpdateStrategy.BEAN_MANAGED, strategy);
		assertEquals("update", method);
	}
}