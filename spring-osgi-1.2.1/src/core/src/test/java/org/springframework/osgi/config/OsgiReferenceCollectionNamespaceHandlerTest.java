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

import java.io.Externalizable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

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
import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;
import org.springframework.osgi.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.springframework.osgi.service.importer.support.internal.collection.OsgiServiceList;
import org.springframework.osgi.service.importer.support.internal.collection.OsgiServiceSet;
import org.springframework.osgi.service.importer.support.internal.collection.OsgiServiceSortedList;
import org.springframework.osgi.service.importer.support.internal.collection.OsgiServiceSortedSet;
import org.springframework.osgi.service.importer.support.internal.collection.comparator.ServiceReferenceComparator;

/**
 * @author Costin Leau
 * 
 */
public class OsgiReferenceCollectionNamespaceHandlerTest extends TestCase {

	private GenericApplicationContext appContext;


	protected void setUp() throws Exception {
		// reset counter just to be sure
		DummyListener.BIND_CALLS = 0;
		DummyListener.UNBIND_CALLS = 0;

		DummyListenerServiceSignature.BIND_CALLS = 0;
		DummyListenerServiceSignature.UNBIND_CALLS = 0;

		DummyListenerServiceSignature2.BIND_CALLS = 0;
		DummyListenerServiceSignature2.UNBIND_CALLS = 0;

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
		reader.loadBeanDefinitions(new ClassPathResource("osgiReferenceCollectionNamespaceHandlerTests.xml", getClass()));
		appContext.refresh();
	}

	protected void tearDown() throws Exception {
		appContext.close();
	}

	public void testSimpleList() {
		Object factoryBean = appContext.getBean("&simpleList");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);
		// get the factory product
		Object bean = appContext.getBean("simpleList");
		assertFalse(bean instanceof OsgiServiceList);
		assertTrue(bean instanceof List);
	}

	public void testSimpleSet() {
		Object factoryBean = appContext.getBean("&simpleSet");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);
		// get the factory product
		Object bean = appContext.getBean("simpleSet");
		assertFalse(bean instanceof OsgiServiceSet);
		assertTrue(bean instanceof Set);
	}

	public void testSimpleListWithGreedyProxyingOn() throws Exception {
		Object factoryBean = appContext.getBean("&simpleListWithGreedyProxying");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);
		assertEquals(Boolean.TRUE, (Boolean) TestUtils.getFieldValue(factoryBean, "greedyProxying"));
		// get the factory product
		Object bean = appContext.getBean("simpleListWithGreedyProxying");
		assertFalse(bean instanceof OsgiServiceList);
		assertTrue(bean instanceof List);
	}

	public void testSimpleListWithDefaultProxying() throws Exception {
		Object factoryBean = appContext.getBean("&simpleSet");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);
		assertEquals(Boolean.FALSE, (Boolean) TestUtils.getFieldValue(factoryBean, "greedyProxying"));
	}

	public void testImplicitSortedList() {
		Object factoryBean = appContext.getBean("&implicitSortedList");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);
		// get the factory product
		Object bean = appContext.getBean("implicitSortedList");
		assertFalse(bean instanceof OsgiServiceSortedList);
		assertTrue(bean instanceof List);

		OsgiServiceSortedList exposedProxy = (OsgiServiceSortedList) TestUtils.getFieldValue(factoryBean,
			"exposedProxy");
		assertSame(appContext.getBean("defaultComparator"), exposedProxy.comparator());
	}

	public void testImplicitSortedSet() {
		Object factoryBean = appContext.getBean("&implicitSortedSet");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);

		Object bean = appContext.getBean("implicitSortedSet");
		assertFalse(bean instanceof OsgiServiceSortedSet);
		assertTrue(bean instanceof SortedSet);

		assertSame(appContext.getBean("defaultComparator"), ((SortedSet) bean).comparator());
	}

	public void testSimpleSortedList() {
		Object factoryBean = appContext.getBean("&implicitSortedList");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);

		Object bean = appContext.getBean("implicitSortedList");
		assertFalse(bean instanceof OsgiServiceSortedList);
		assertTrue(bean instanceof List);

		Class[] intfs = getInterfaces(factoryBean);
		assertTrue(Arrays.equals(new Class[] { Serializable.class }, intfs));
	}

	public void testSimpleSortedSet() {
		Object factoryBean = appContext.getBean("&implicitSortedSet");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);

		Object bean = appContext.getBean("implicitSortedSet");
		assertFalse(bean instanceof OsgiServiceSortedSet);
		assertTrue(bean instanceof SortedSet);

		Class[] intfs = getInterfaces(factoryBean);
		assertTrue(Arrays.equals(new Class[] { Externalizable.class }, intfs));
	}

	public void testSortedSetWithNaturalOrderingOnRefs() throws Exception {
		Object factoryBean = appContext.getBean("&sortedSetWithNaturalOrderingOnRefs");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);

		Comparator comp = getComparator(factoryBean);

		assertNotNull(comp);
		assertSame(ServiceReferenceComparator.class, comp.getClass());

		Class[] intfs = getInterfaces(factoryBean);
		assertTrue(Arrays.equals(new Class[] { Externalizable.class }, intfs));

		OsgiServiceLifecycleListener[] listeners = getListeners(factoryBean);
		assertEquals(2, listeners.length);

		Object bean = appContext.getBean("sortedSetWithNaturalOrderingOnRefs");
		assertFalse(bean instanceof OsgiServiceSortedSet);
		assertTrue(bean instanceof SortedSet);

	}

	public void testSortedListWithNaturalOrderingOnServs() throws Exception {
		Object factoryBean = appContext.getBean("&sortedListWithNaturalOrderingOnServs");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);

		assertNull(getComparator(factoryBean));

		Object bean = appContext.getBean("sortedListWithNaturalOrderingOnServs");
		assertFalse(bean instanceof OsgiServiceSortedList);
		assertTrue(bean instanceof List);

		Class[] intfs = getInterfaces(factoryBean);
		assertTrue(Arrays.equals(new Class[] { Externalizable.class }, intfs));
	}

	private Class[] getInterfaces(Object proxy) {
		return (Class[]) TestUtils.getFieldValue(proxy, "interfaces");
	}

	private Comparator getComparator(Object proxy) {
		return (Comparator) TestUtils.getFieldValue(proxy, "comparator");
	}

	private OsgiServiceLifecycleListener[] getListeners(Object proxy) {
		return (OsgiServiceLifecycleListener[]) TestUtils.getFieldValue(proxy, "listeners");
	}
}
