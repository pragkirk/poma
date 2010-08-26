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

package org.springframework.osgi.service.importer;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.aop.SpringProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.service.importer.support.ImportContextClassLoader;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;

/**
 * @author Adrian Colyer
 * @author Hal Hildebrand
 * @author Costin Leau
 * 
 */
public class OsgiSingleServiceProxyFactoryBeanTest extends TestCase {

	private OsgiServiceProxyFactoryBean serviceFactoryBean;

	private MockControl mockControl;

	private BundleContext bundleContext;


	protected void setUp() throws Exception {
		super.setUp();
		this.serviceFactoryBean = new OsgiServiceProxyFactoryBean();
		this.serviceFactoryBean.setBeanClassLoader(getClass().getClassLoader());
		// this.serviceFactoryBean.setApplicationContext(new
		// GenericApplicationContext());
		this.mockControl = MockControl.createControl(BundleContext.class);
		this.bundleContext = (BundleContext) this.mockControl.getMock();
	}

	public void testAfterPropertiesSetNoBundle() throws Exception {
		try {
			this.serviceFactoryBean.afterPropertiesSet();
			fail("should have throw IllegalArgumentException since bundle context was not set");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testAfterPropertiesSetNoClassLoader() throws Exception {
		this.serviceFactoryBean.setBundleContext(this.bundleContext);
		try {
			this.serviceFactoryBean.afterPropertiesSet();
			fail("should have throw IllegalArgumentException since classLoader was not set");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testAfterPropertiesSetNoServiceType() throws Exception {
		this.serviceFactoryBean.setBundleContext(this.bundleContext);
		try {
			this.serviceFactoryBean.afterPropertiesSet();
			fail("should have throw IllegalArgumentException since service type was not set");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testAfterPropertiesSetBadFilter() throws Exception {
		this.serviceFactoryBean.setBundleContext(this.bundleContext);
		this.serviceFactoryBean.setInterfaces(new Class[] { ApplicationContext.class });
		this.serviceFactoryBean.setFilter("this is not a valid filter expression");
		try {
			this.serviceFactoryBean.afterPropertiesSet();
			fail("should have throw IllegalArgumentException since filter has invalid syntax");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testGetObjectTypeCompositeInterface() {
		this.serviceFactoryBean.setInterfaces(new Class[] { ApplicationContext.class });
		assertTrue("composite interface not properly created",
			ApplicationContext.class.isAssignableFrom(this.serviceFactoryBean.getObjectType()));
		assertTrue("mixing interface not introduced",
			ImportedOsgiServiceProxy.class.isAssignableFrom(this.serviceFactoryBean.getObjectType()));
	}

	public void testObjectTypeWOCompositeInterface() {
		this.serviceFactoryBean.setInterfaces(new Class[] { AbstractApplicationContext.class });
		assertNull("should not be able to create composite interface when a class is specified",
			this.serviceFactoryBean.getObjectType());
	}

	// OsgiServiceUtils are tested independently in error cases, here we
	// test the
	// correct behaviour of the ProxyFactoryBean when OsgiServiceUtils
	// succesfully
	// finds the service.
	public void testGetObjectWithFilterOnly() throws Exception {
		this.serviceFactoryBean.setBundleContext(new MockBundleContext());
		this.serviceFactoryBean.setInterfaces(new Class[] { Serializable.class });
		String filter = "(beanName=myBean)";
		this.serviceFactoryBean.setFilter(filter);

		MockServiceReference ref = new MockServiceReference();
		Dictionary dict = new Hashtable();
		dict.put(Constants.OBJECTCLASS, new String[] { Serializable.class.getName() });
		ref.setProperties(dict);

		serviceFactoryBean.setBeanClassLoader(getClass().getClassLoader());
		serviceFactoryBean.afterPropertiesSet();

		Object proxy = serviceFactoryBean.getObject();
		assertTrue(proxy instanceof Serializable);
		assertTrue("should be proxied", proxy instanceof SpringProxy);
	}

	public void testClassLoadingOptionsConstant() throws Exception {
		serviceFactoryBean.setContextClassLoader(ImportContextClassLoader.CLIENT);
		serviceFactoryBean.setContextClassLoader(ImportContextClassLoader.SERVICE_PROVIDER);
		serviceFactoryBean.setContextClassLoader(ImportContextClassLoader.UNMANAGED);
	}

}
