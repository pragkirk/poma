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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.osgi.context.support.BundleContextAwareProcessor;
import org.springframework.osgi.mock.MockBundleContext;

/**
 * @author Costin Leau
 */
public class ConfigPropertiesHandlerTest extends TestCase {

	private GenericApplicationContext appContext;

	private BundleContext bundleContext;

	private MockControl adminControl;

	private ConfigurationAdmin admin;

	private Dictionary config;

	private String persistentId = "foo.bar";

	private Configuration cfg;


	protected void setUp() throws Exception {

		adminControl = MockControl.createControl(ConfigurationAdmin.class);
		admin = (ConfigurationAdmin) adminControl.getMock();
		MockControl configMock = MockControl.createControl(Configuration.class);
		cfg = (Configuration) configMock.getMock();

		config = new Hashtable();

		adminControl.expectAndReturn(admin.getConfiguration(persistentId), cfg, MockControl.ONE_OR_MORE);
		configMock.expectAndReturn(cfg.getProperties(), config, MockControl.ONE_OR_MORE);

		adminControl.replay();
		configMock.replay();

		bundleContext = new MockBundleContext() {

			// add Configuration admin support
			public Object getService(ServiceReference reference) {
				return admin;
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		// reader.setEventListener(this.listener);
		reader.loadBeanDefinitions(new ClassPathResource("configProperties.xml", getClass()));
		appContext.refresh();
	}

	protected void tearDown() throws Exception {
		adminControl.verify();
	}

	public void testPropertiesLazyInit() throws Exception {
		adminControl.reset();
		adminControl.replay();
	}

	public void testBlankConfigProperties() throws Exception {
		config.put("Spring", "Source");
		Object bean = appContext.getBean("named");
		assertTrue(bean instanceof Properties);
		assertEquals(config, bean);
	}

	public void testPropertiesWithDefaultsAndNoOverride() throws Exception {
		persistentId = "noLocalOverride";

		adminControl.reset();
		adminControl.expectAndReturn(admin.getConfiguration(persistentId), cfg, MockControl.ONE_OR_MORE);
		adminControl.replay();

		config.put("foo", "foo");
		config.put("Spring", "Source");
		Object bean = appContext.getBean(persistentId);
		assertTrue(bean instanceof Properties);
		Properties props = (Properties) bean;
		assertFalse(config.equals(bean));
		// the local property has been replaced
		assertEquals("foo", props.getProperty("foo"));
		// but the one not present on the CM are still present
		assertTrue(props.containsKey("kry"));
		assertTrue(props.containsKey("Spring"));
		assertEquals(3, props.entrySet().size());
	}

	public void testPropertiesWithDefaultsAndOverride() throws Exception {
		persistentId = "localOverride";

		adminControl.reset();
		adminControl.expectAndReturn(admin.getConfiguration(persistentId), cfg, MockControl.ONE_OR_MORE);
		adminControl.replay();

		config.put("foo", "foo");
		config.put("Spring", "Source");
		Object bean = appContext.getBean(persistentId);
		assertTrue(bean instanceof Properties);
		Properties props = (Properties) bean;
		assertFalse(config.equals(bean));
		// the local property is still present
		assertEquals("bar", props.getProperty("foo"));
		// the CM props are still there
		assertTrue(props.containsKey("kry"));
		// and so are the local props
		assertTrue(props.containsKey("Spring"));
		assertEquals(3, props.entrySet().size());
	}

	// disabled until custom attributes are enabled again
	public void tstPropertiesWithPropRef() throws Exception {
		persistentId = "custom-attributes";

		adminControl.reset();
		adminControl.expectAndReturn(admin.getConfiguration(persistentId), cfg, MockControl.ONE_OR_MORE);
		adminControl.replay();

		config.put("foo", "foo");
		config.put("Spring", "Source");
		Object bean = appContext.getBean(persistentId);
		BeanDefinition bd = appContext.getBeanDefinition(persistentId);
		System.out.println(bd.getScope());
		assertTrue(bean instanceof Properties);
		Properties props = (Properties) bean;
		assertFalse(config.equals(bean));
		// the local property is still present
		assertEquals("bar", props.getProperty("foo"));
		// the CM props are still there
		assertTrue(props.containsKey("kry"));
		// and so are the local props
		assertTrue(props.containsKey("Spring"));
		assertEquals(3, props.entrySet().size());
	}
}
