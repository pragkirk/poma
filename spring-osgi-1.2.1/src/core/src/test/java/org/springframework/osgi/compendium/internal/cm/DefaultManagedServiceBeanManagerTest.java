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

package org.springframework.osgi.compendium.internal.cm;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.osgi.TestUtils;
import org.springframework.osgi.compendium.MultipleSetters;
import org.springframework.osgi.compendium.OneSetter;
import org.springframework.osgi.mock.MockBundleContext;

/**
 * 
 * @author Costin Leau
 * 
 */
public class DefaultManagedServiceBeanManagerTest extends TestCase {

	private DefaultManagedServiceBeanManager msbm;
	private ConfigurationAdminManager cam;
	private Map configuration;


	protected void setUp() throws Exception {
		cam = new ConfigurationAdminManager("bla", new MockBundleContext()) {

			public Map getConfiguration() {
				return configuration;
			}

		};
	}

	protected void tearDown() throws Exception {
		msbm = null;
		cam = null;
	}

	private Object getUpdateCallback() {
		return TestUtils.getFieldValue(msbm, "updateCallback");
	}

	private Map getBeanMap() {
		return (Map) TestUtils.getFieldValue(msbm, "instanceRegistry");
	}

	public void testNoUpdateStrategy() {
		msbm = new DefaultManagedServiceBeanManager(UpdateStrategy.NONE, null, cam, null);
		assertNull(getUpdateCallback());
	}

	public void testBeanManagedUpdateStrategy() {
		msbm = new DefaultManagedServiceBeanManager(UpdateStrategy.BEAN_MANAGED, "update", cam, null);
		assertTrue(getUpdateCallback().getClass().getName().endsWith("BeanManagedUpdate"));
	}

	public void testContainerManagedUpdateStrategy() {
		msbm = new DefaultManagedServiceBeanManager(UpdateStrategy.CONTAINER_MANAGED, null, cam, null);
		assertTrue(getUpdateCallback().getClass().getName().endsWith("ContainerManagedUpdate"));
	}

	public void testRegister() {
		configuration = new HashMap();
		msbm = new DefaultManagedServiceBeanManager(UpdateStrategy.NONE, null, cam, null);
		Object bean = new Object();
		assertSame(bean, msbm.register(bean));
		assertTrue(getBeanMap().containsValue(bean));
	}

	public void testUnregister() {
		msbm = new DefaultManagedServiceBeanManager(UpdateStrategy.NONE, null, cam, null);
		Object bean = new Object();
		assertSame(bean, msbm.register(bean));
		assertTrue(getBeanMap().containsValue(bean));
		msbm.unregister(bean);
		assertFalse(getBeanMap().containsValue(bean));
	}

	public void testUpdated() {
		msbm = new DefaultManagedServiceBeanManager(UpdateStrategy.NONE, null, cam, null);
	}

	public void testInjectInfoSimple() {
		Map props = new HashMap();
		props.put("prop", "14");
		OneSetter instance = new OneSetter();
		msbm = new DefaultManagedServiceBeanManager(UpdateStrategy.CONTAINER_MANAGED, null, cam, null);
		msbm.applyInitialInjection(instance, props);
		assertEquals(new Long(14), instance.getProp());
	}

	public void testMultipleSetters() {
		Map props = new HashMap();
		props.put("prop", "14");
		props.put("integer", new Double(14));
		props.put("dbl", new Float(14));
		props.put("none", "14");
		props.put("float", "14");
		MultipleSetters instance = new MultipleSetters();
		msbm = new DefaultManagedServiceBeanManager(UpdateStrategy.CONTAINER_MANAGED, null, cam, null);
		msbm.applyInitialInjection(instance, props);
		assertEquals(new Double(14), instance.getDbl());
		assertEquals(14, instance.getInteger());
		assertEquals(new Long(14), instance.getProp());
		assertEquals(new Float(0), new Float(instance.getFloat()));
	}
}