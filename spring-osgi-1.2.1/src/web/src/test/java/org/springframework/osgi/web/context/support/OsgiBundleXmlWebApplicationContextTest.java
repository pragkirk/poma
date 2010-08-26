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

package org.springframework.osgi.web.context.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextAwareProcessor;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * 
 * @author Costin Leau
 * 
 */
public class OsgiBundleXmlWebApplicationContextTest extends TestCase {

	private OsgiBundleXmlWebApplicationContext context;
	private ServletContext servletContext;
	private static Collection ignored, resolved, bpp;


	protected void setUp() throws Exception {
		context = new OsgiBundleXmlWebApplicationContext();
		ignored = new ArrayList();
		resolved = new ArrayList();
		bpp = new ArrayList();
	}

	protected void tearDown() throws Exception {
		context = null;
		servletContext = null;
	}

	public void testPostProcessBeanFactoryConfigurableListableBeanFactory() {

		ConfigurableListableBeanFactory mock = new DefaultListableBeanFactory() {

			public void ignoreDependencyInterface(Class ifc) {
				ignored.add(ifc);
			}

			public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
				super.addBeanPostProcessor(beanPostProcessor);
				bpp.add(beanPostProcessor.getClass());
			}

			public void registerResolvableDependency(Class dependencyType, Object autowiredValue) {
				super.registerResolvableDependency(dependencyType, autowiredValue);
				resolved.add(dependencyType);
			}
		};

		context.postProcessBeanFactory(mock);

		assertTrue(ignored.contains(ServletConfigAware.class));
		assertTrue(ignored.contains(ServletContextAware.class));
		assertTrue(resolved.contains(ServletContext.class));
		assertTrue(resolved.contains(ServletConfig.class));
		assertTrue(bpp.contains(ServletContextAwareProcessor.class));
	}

	public void testCustomizeApplicationContextServiceProperties() {
		String ns = "foo";
		context.setNamespace(ns);
		Map props = new HashMap();
		context.setBundleContext(new MockBundleContext());
		context.customizeApplicationContextServiceProperties(props);
		assertEquals(ns, props.get("org.springframework.web.context.namespace"));
	}

	public void testSetServletContextWOBundleContext() {
		servletContext = new MockServletContext();
		BundleContext bc = new MockBundleContext();
		servletContext.setAttribute(OsgiBundleXmlWebApplicationContext.BUNDLE_CONTEXT_ATTRIBUTE, bc);
		context.setServletContext(servletContext);
		assertSame(servletContext, context.getServletContext());
		assertSame(bc, context.getBundleContext());
	}

	public void testSetServletContextWOBundleContextWithParent() throws Exception {
		servletContext = new MockServletContext();
		BundleContext bc = new MockBundleContext();
		ConfigurableOsgiBundleApplicationContext parent = new OsgiBundleXmlApplicationContext();
		parent.setBundleContext(bc);
		context.setParent(parent);
		context.setServletContext(servletContext);
		assertSame(servletContext, context.getServletContext());
		assertSame(bc, parent.getBundleContext());
		assertSame(bc, context.getBundleContext());
		assertSame(parent, context.getParent());
	}

	public void testSetServletContextWBundleContext() {
		servletContext = new MockServletContext();
		context.setServletContext(servletContext);
		assertSame(servletContext, context.getServletContext());
	}

	public void testSetServletConfig() {
		ServletContext servletContext = new MockServletContext();
		BundleContext bc = new MockBundleContext();
		servletContext.setAttribute(OsgiBundleXmlWebApplicationContext.BUNDLE_CONTEXT_ATTRIBUTE, bc);
		ServletConfig servletConfig = new MockServletConfig(servletContext);
		context.setServletConfig(servletConfig);
		assertSame(bc, context.getBundleContext());
	}

	public void testNamespace() {
		String ns = "foo";
		context.setNamespace(ns);
		assertSame(ns, context.getNamespace());
	}

	public void testNamespaceFallingBackToServletConfig() throws Exception {
		String ns = "foo";
		ServletConfig sc = new MockServletConfig(ns);
		context.setServletConfig(sc);
		assertEquals(ns + "-servlet", context.getNamespace());
	}

	public void testDefaultConfigLocationsWONamespace() {
		assertTrue(Arrays.equals(new String[] { XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION },
			context.getDefaultConfigLocations()));
	}

	public void testDefaultConfigLocationsWNamespace() throws Exception {
		String ns = "foo";
		context.setNamespace(ns);
		assertTrue(Arrays.equals(new String[] { XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION_PREFIX + ns
				+ XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION_SUFFIX }, context.getDefaultConfigLocations()));
	}

	public void testConfigLocation() {
		String loc1 = "a";
		String loc2 = "b";
		String locs = loc1 + "," + loc2;

		context.setConfigLocation(locs);
		assertTrue(Arrays.equals(new String[] { loc1, loc2 }, context.getConfigLocations()));
	}
}
