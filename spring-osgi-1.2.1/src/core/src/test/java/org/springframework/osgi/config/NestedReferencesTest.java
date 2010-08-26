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

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.osgi.context.support.BundleContextAwareProcessor;
import org.springframework.osgi.mock.MockBundleContext;

/**
 * @author Costin Leau
 * 
 */
public class NestedReferencesTest extends TestCase {

	public static class Holder {
		private Object data;

		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}
	}

	private GenericApplicationContext appContext;

	protected void setUp() throws Exception {

		BundleContext bundleContext = new MockBundleContext() {
			// service reference already registered
			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[0];
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		// reader.setEventListener(this.listener);
		reader.loadBeanDefinitions(new ClassPathResource("osgiReferenceNestedBeans.xml", getClass()));
		appContext.refresh();
	}

	protected void tearDown() throws Exception {
		appContext.close();
	}

	public void testNestedBeansMadeTopLevel() throws Exception {
		// 5 top-level plus 4 promoted beans
		assertEquals(5 + 4, appContext.getBeanDefinitionCount());
	}

	public void testNestedReferenceWithName() {
		Object bean = appContext.getBean("satriani#org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean#0");
		Holder holder = (Holder) appContext.getBean("nestedNamedReference", Holder.class);
		assertSame(bean, holder.data);
	}

	public void testNestedReferenceWithoutName() throws Exception {
		Object bean = appContext.getBean("org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean#0");
		Holder holder = (Holder) appContext.getBean("nestedAnonymousReference", Holder.class);
		assertSame(bean, holder.data);
	}

	public void testNestedCollectionWithName() throws Exception {
		Object bean = appContext.getBean("dire-straits#org.springframework.osgi.service.importer.support.OsgiServiceCollectionProxyFactoryBean#0");
		Holder holder = (Holder) appContext.getBean("nestedNamedCollection", Holder.class);
		assertSame(bean, holder.data);
	}

	public void testNesteCollectionWithoutName() throws Exception {
		Object bean = appContext.getBean("org.springframework.osgi.service.importer.support.OsgiServiceCollectionProxyFactoryBean#0");
		Holder holder = (Holder) appContext.getBean("nestedAnonymousCollection", Holder.class);
		assertSame(bean, holder.data);
	}

}
