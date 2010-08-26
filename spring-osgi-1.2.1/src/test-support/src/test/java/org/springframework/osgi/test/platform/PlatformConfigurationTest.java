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

package org.springframework.osgi.test.platform;

import java.util.Properties;

import org.osgi.framework.BundleContext;

import junit.framework.TestCase;

/**
 * Unit test for the platform configuration properties.
 * 
 * @author Costin Leau
 * 
 */
public class PlatformConfigurationTest extends TestCase {

	private OsgiPlatform platform;


	public void testOverridenConfiguration() throws Exception {
		platform = new AbstractOsgiPlatform() {

			Properties getPlatformProperties() {
				Properties props = new Properties();
				props.put("foo", "bar");
				return props;
			}

			public BundleContext getBundleContext() {
				return null;
			}

			public void start() throws Exception {
			}

			public void stop() throws Exception {
			}

		};

		Properties props = platform.getConfigurationProperties();
		props.setProperty("some.prop", "valueA");
		props.setProperty("other.prop", "valueB");
		// override default property
		props.setProperty("foo", "extra-bar");

		Properties test = platform.getConfigurationProperties();
		assertEquals("valueA", test.getProperty("some.prop"));
		assertEquals("valueB", test.getProperty("other.prop"));
		assertEquals("extra-bar", test.getProperty("foo"));
	}
}
