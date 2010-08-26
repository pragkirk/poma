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

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.springframework.osgi.mock.MockBundleContext;

/**
 * @author Costin Leau
 * 
 */
public class AbstractOsgiPlatformTest extends TestCase {

	private AbstractOsgiPlatform platform;
	private Properties prop = new Properties();


	protected void setUp() throws Exception {

		final BundleContext ctx = new MockBundleContext();
		prop.setProperty("foo", "bar");

		platform = new AbstractOsgiPlatform() {

			Properties getPlatformProperties() {
				return prop;
			}

			public BundleContext getBundleContext() {
				return ctx;
			}

			public void start() throws Exception {
			}

			public void stop() throws Exception {
			}

		};
	}

	protected void tearDown() throws Exception {
		prop = null;
		platform = null;
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.test.platform.AbstractOsgiPlatform#getConfigurationProperties()}.
	 */
	public void testGetConfigurationProperties() {
		Properties cfg = platform.getConfigurationProperties();
		assertNotNull(cfg);
		Properties sysCfg = System.getProperties();
		for (Iterator iterator = sysCfg.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			assertSame(entry.getValue(), cfg.get(entry.getKey()));
		}
		assertEquals("bar", cfg.getProperty("foo"));

		prop.setProperty("abc", "xyz");
		Properties otherCfg = platform.getConfigurationProperties();
		assertSame(cfg, otherCfg);
		assertFalse(cfg.contains("abc"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.test.platform.AbstractOsgiPlatform#getPlatformProperties()}.
	 */
	public void testGetPlatformProperties() {
		assertSame(prop, platform.getPlatformProperties());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.test.platform.AbstractOsgiPlatform#createTempDir(java.lang.String)}.
	 */
	public void testCreateTempDir() {
		File tmpDir = platform.createTempDir("bla");
		assertNotNull(tmpDir);
		assertTrue(tmpDir.exists());
		tmpDir.delete();
	}

}
