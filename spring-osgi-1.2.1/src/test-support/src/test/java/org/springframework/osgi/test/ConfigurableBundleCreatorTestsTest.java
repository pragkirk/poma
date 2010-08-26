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
package org.springframework.osgi.test;

import java.lang.reflect.Field;
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ReflectionUtils;

/**
 * Test Case for AbstractConfigurableBundleCreatorTests.
 * 
 * @author Costin Leau
 * 
 */
public class ConfigurableBundleCreatorTestsTest extends TestCase {

	private AbstractConfigurableBundleCreatorTests bundleCreator;

	protected void setUp() throws Exception {
		bundleCreator = new AbstractConfigurableBundleCreatorTests() {
		};
	}

	protected void tearDown() throws Exception {
		bundleCreator = null;
	}

	public void testGetSettingsLocation() throws Exception {

		assertEquals(bundleCreator.getClass().getPackage().getName().replace('.', '/')
				+ "/ConfigurableBundleCreatorTestsTest$1-bundle.properties", bundleCreator.getSettingsLocation());
	}

	public void testDefaultJarSettings() throws Exception {

		Properties defaultSettings = bundleCreator.getSettings();
		Field field = ReflectionUtils.findField(AbstractConfigurableBundleCreatorTests.class, "jarSettings" , Properties.class);
		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, null, defaultSettings);
		assertNotNull(defaultSettings);
		assertNotNull(bundleCreator.getRootPath());
		assertNotNull(bundleCreator.getBundleContentPattern());
		assertNotNull(bundleCreator.getManifestLocation());
	}

	public void testPropertiesLoading() throws Exception {
		Properties testSettings = bundleCreator.getSettings();

		Properties props = new Properties();
		props.load(new ClassPathResource(
				"org/springframework/osgi/test/ConfigurableBundleCreatorTestsTest$1-bundle.properties").getInputStream());

		assertEquals(props.getProperty(AbstractConfigurableBundleCreatorTests.INCLUDE_PATTERNS),
			testSettings.getProperty(AbstractConfigurableBundleCreatorTests.INCLUDE_PATTERNS));
		assertEquals(props.getProperty(AbstractConfigurableBundleCreatorTests.MANIFEST),
			testSettings.getProperty(AbstractConfigurableBundleCreatorTests.MANIFEST));
	}

}
