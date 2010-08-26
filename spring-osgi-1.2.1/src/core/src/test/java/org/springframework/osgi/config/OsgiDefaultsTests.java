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
package org.springframework.osgi.config;

import java.io.Serializable;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.osgi.TestUtils;
import org.springframework.osgi.context.support.BundleContextAwareProcessor;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.service.importer.support.Cardinality;
import org.springframework.osgi.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
import org.springframework.osgi.service.importer.support.internal.support.RetryTemplate;

/**
 * @author Costin Leau
 * 
 */
public class OsgiDefaultsTests extends TestCase {

	private GenericApplicationContext appContext;

	protected void setUp() throws Exception {
		BundleContext bundleContext = new MockBundleContext() {
			// service reference already registered
			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[] { new MockServiceReference(new String[] { Serializable.class.getName() }) };
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		reader.loadBeanDefinitions(new ClassPathResource("osgiDefaults.xml", getClass()));
		appContext.refresh();
	}

	protected void tearDown() throws Exception {
		appContext.close();
		appContext = null;
	}

	public void testLocalDefinitionForTimeout() throws Exception {
		OsgiServiceProxyFactoryBean fb = (OsgiServiceProxyFactoryBean) appContext.getBean("&refWLocalConfig");
		assertEquals(55, getTimeout(fb));
	}

	public void testLocalDefinitionForCardinalityOnMultiImporter() throws Exception {
		OsgiServiceCollectionProxyFactoryBean fb = (OsgiServiceCollectionProxyFactoryBean) appContext.getBean("&colWLocalConfig");
		assertEquals(Cardinality.C_1__N, getCardinality(fb));
	}

	public void testLocalDefinitionForCardinalityOnSingleImporter() throws Exception {
		OsgiServiceProxyFactoryBean fb = (OsgiServiceProxyFactoryBean) appContext.getBean("&refWLocalConfig");
		assertEquals(Cardinality.C_1__1, getCardinality(fb));
	}

	public void testTimeoutDefault() throws Exception {
		OsgiServiceProxyFactoryBean fb = (OsgiServiceProxyFactoryBean) appContext.getBean("&refWDefaults");
		assertEquals("default osgi timeout not applied", 10, getTimeout(fb));
	}

	public void testCardinalityDefaultOnSingleImporter() throws Exception {
		OsgiServiceProxyFactoryBean fb = (OsgiServiceProxyFactoryBean) appContext.getBean("&refWDefaults");
		assertEquals(Cardinality.C_0__1, getCardinality(fb));
	}

	public void testCardinalityDefaultOnMultiImporter() throws Exception {
		OsgiServiceCollectionProxyFactoryBean fb = (OsgiServiceCollectionProxyFactoryBean) appContext.getBean("&colWDefaults");
		assertEquals(Cardinality.C_0__N, getCardinality(fb));
	}

	private Cardinality getCardinality(Object obj) {
		return (Cardinality) TestUtils.getFieldValue(obj, "cardinality");
	}

	private long getTimeout(Object obj) {
		return ((RetryTemplate) TestUtils.getFieldValue(obj, "retryTemplate")).getWaitTime();
	}

}
