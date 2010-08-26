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

package org.springframework.osgi.iandt.cm.managedservicefactory;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.osgi.iandt.cm.BaseConfigurationAdminTest;
import org.springframework.osgi.util.OsgiStringUtils;

/**
 * Integration test for managed-service-factory element.
 * 
 * @author Costin Leau
 */
public class ManagedServiceFactoryTest extends BaseConfigurationAdminTest {

	private Properties props;
	private final String FPID = ManagedServiceFactoryTest.class.getName();
	private final String FILTER = "(service.factoryPid=" + FPID + ")";


	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/osgi/iandt/cm/managedservicefactory/context.xml" };
	}

	private void initProperties() {
		props = new Properties();
		props.setProperty("class", System.class.getName());
		props.setProperty("integer", "54321");
	}

	protected void onSetUp() throws Exception {
		super.onSetUp();
		Listener.instances.clear();
		initProperties();
	}

	protected void prepareConfiguration(ConfigurationAdmin configAdmin) throws Exception {
		initProperties();
		Properties localCopy = (Properties) props.clone();
		localCopy.setProperty("simple", "simple");

		// prepare one instance
		updateAndWaitForConfig(configAdmin, localCopy);
	}

	public void testConfigurationCreation() throws Exception {
		Configuration[] cfgs = cm.listConfigurations(FILTER);
		int size = (cfgs != null ? cfgs.length : 0);

		Dictionary newProps = new Properties();
		newProps.put("foo", "bar");
		updateAndWaitForConfig(newProps);

		cfgs = cm.listConfigurations(FILTER);
		assertNotNull(cfgs);
		assertEquals("new factory configuration not registered", cfgs.length, size + 1);
	}

	public void testCreateInstance() throws Exception {
		Dictionary newProps = new Properties();
		newProps.put("new", "instance");

		int sizeA = Listener.instances.size();

		updateAndWaitForConfig(newProps);

		int sizeB = Listener.instances.size();
		assertTrue(sizeB > sizeA);
	}

	public void testDestroyInstance() throws Exception {
		prepareConfiguration(cm);
		Configuration[] cfgs = cm.listConfigurations(FILTER);
		int sizeA = cfgs.length;
		cfgs[0].delete();
		synchronized (Listener.unregBarrier) {
			Listener.unregBarrier.wait(10 * 1000);
		}

		int sizeB = Listener.instances.size();
		assertEquals(sizeA - 1, sizeB);
	}

	public void testPublishedServices() throws Exception {
		prepareConfiguration(cm);

		ServiceReference refs[] = bundleContext.getServiceReferences(Map.class.getName(), null);

		assertNotNull(refs);
		assertTrue(refs.length > 0);
		for (int i = 0; i < refs.length; i++) {
			ServiceReference serviceReference = refs[i];
			System.out.println(OsgiStringUtils.nullSafeToString(refs[i]));
		}
	}

	public void testReturnedObjects() throws Exception {
		Dictionary newProps = new Properties();
		newProps.put("new", "instance");

		updateAndWaitForConfig(newProps);

		Object bean = applicationContext.getBean("msf");
		assertTrue(bean instanceof Collection);
		assertFalse(bean instanceof List);
		Collection col = (Collection) bean;

		// check the read-only nature of the collection
		try {
			assertTrue(col.add(new Object()));
			fail("the collection should be read-only - write operations should throw an exception");
		}
		catch (Exception ex) {
			// expected
		}
		for (Iterator iterator = col.iterator(); iterator.hasNext();) {
			Object item = (Object) iterator.next();
			System.out.println(item);
			assertTrue(item instanceof ServiceRegistration);
		}
	}

	protected boolean createManifestOnlyFromTestClass() {
		return false;
	}

	private void updateAndWaitForConfig(ConfigurationAdmin cm, final Dictionary properties) throws Exception {
		Configuration cfg = cm.createFactoryConfiguration(FPID, null);
		cfg.update(properties);
		synchronized (Listener.regBarrier) {
			Listener.regBarrier.wait(10 * 1000);
		}
	}

	private void updateAndWaitForConfig(final Dictionary properties) throws Exception {
		updateAndWaitForConfig(cm, properties);
	}
}