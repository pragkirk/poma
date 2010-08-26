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

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.osgi.context.support.BundleContextAwareProcessor;
import org.springframework.osgi.mock.MockBundleContext;

/**
 * @author Costin Leau
 * 
 */
public class CmConfigAndCtxPropertiesConfigurationTest extends TestCase {

	private GenericApplicationContext appContext;

	private BundleContext bundleContext;

	private MockControl adminControl;

	private ConfigurationAdmin admin;

	private Dictionary config;


	protected void setUp() throws Exception {

		adminControl = MockControl.createControl(ConfigurationAdmin.class);
		admin = (ConfigurationAdmin) adminControl.getMock();
		MockControl configMock = MockControl.createControl(Configuration.class);
		Configuration cfg = (Configuration) configMock.getMock();

		config = new Hashtable();

		adminControl.expectAndReturn(admin.getConfiguration("com.xyz.myapp"), cfg, MockControl.ONE_OR_MORE);
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
		reader.loadBeanDefinitions(new ClassPathResource("osgiPropertyPlaceholder.xml", getClass()));
		appContext.refresh();
	}

	protected void tearDown() throws Exception {
		adminControl.verify();
	}

	public void testValidateConfiguration() throws Exception {

	}
}