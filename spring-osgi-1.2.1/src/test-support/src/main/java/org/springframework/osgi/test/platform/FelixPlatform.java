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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.StringMap;
import org.apache.felix.main.AutoActivator;
import org.apache.felix.main.Main;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.test.internal.util.IOUtils;

/**
 * Apache Felix (1.0.3+/1.4.x+) OSGi platform.
 * 
 * @author Costin Leau
 */
public class FelixPlatform extends AbstractOsgiPlatform {

	private static final Log log = LogFactory.getLog(FelixPlatform.class);

	private static final String FELIX_PROFILE_DIR_PROPERTY = "felix.cache.profiledir";
	/** new property in 1.4.0 replacing cache.profiledir */
	private static final String OSGI_STORAGE_PROPERTY = "org.osgi.framework.storage";

	private BundleContext context;

	private Felix platform;

	private File felixStorageDir;


	public FelixPlatform() {
		toString = "Felix OSGi Platform";
	}

	Properties getPlatformProperties() {
		// load Felix configuration
		Properties props = new Properties();
		createStorageDir(props);
		// disable logging
		props.put("felix.log.level", "0");

		// use embedded mode
		props.put("felix.embedded.execution", "true");
		return props;
	}

	public BundleContext getBundleContext() {
		return context;
	}

	/**
	 * Configuration settings for the OSGi test run.
	 * 
	 * @return
	 */
	private void createStorageDir(Properties configProperties) {
		// create a temporary file if none is set
		if (felixStorageDir == null) {
			felixStorageDir = createTempDir("felix");
			felixStorageDir.deleteOnExit();

			if (log.isTraceEnabled())
				log.trace("Felix storage dir is " + felixStorageDir.getAbsolutePath());
		}

		configProperties.setProperty(FELIX_PROFILE_DIR_PROPERTY, this.felixStorageDir.getAbsolutePath());
		configProperties.setProperty(OSGI_STORAGE_PROPERTY, this.felixStorageDir.getAbsolutePath());
	}

	public void start() throws Exception {
		if (platform == null) {
			// initialize properties and set them as system wide so Felix can pick them up
			System.getProperties().putAll(getConfigurationProperties());

			platform = configureFelix();
			platform.start();
			context = platform.getBundleContext();
		}
	}

	/**
	 * Configures the embedded Felix instance. This facade-like method is needed
	 * since Felix 1.4.x breaks backwards compatibility by defining different
	 * constructors.
	 * 
	 * @return a configured (but not started) Felix instance
	 */
	private Felix configureFelix() throws Exception {
		boolean is14 = false;
		Constructor ctr = null;
		// check for Felix 1.4.x constructor
		try {
			ctr = Felix.class.getConstructor(new Class[] { Map.class });
			is14 = true;
		}
		catch (NoSuchMethodException nsme) {
			ctr = Felix.class.getConstructor(new Class[] { Map.class, List.class });
		}

		Object[] params = commonFelixSetup();
		return (is14 ? configureFelix14X(ctr, params) : configureFelix10X_12X(ctr, params));
	}

	private Felix configureFelix10X_12X(Constructor ctr, Object[] params) throws Exception {
		// Create a case-insensitive property map
		Map configMap = new StringMap((Map) params[0], false);
		return (Felix) ctr.newInstance(new Object[] { configMap, params[1] });
	}

	private Felix configureFelix14X(Constructor ctr, Object[] params) throws Exception {
		Map configProps = (Map) params[0];
		configProps.put("felix.systembundle.activators", params[1]);
		return (Felix) ctr.newInstance(new Object[] { configProps });
	}

	/**
	 * Runs the common 1.0.x/1.2.x/1.4.x Felix setup and returns two Felix
	 * specific objects, the configuration properties and the list of
	 * AutoActivators.
	 * 
	 * @return a 2 object array containing the configuration properties and the
	 *         list of auto-activators
	 */
	private Object[] commonFelixSetup() {
		// Load system properties.
		Main.loadSystemProperties();

		// Read configuration properties.
		Properties configProps = Main.loadConfigProperties();

		if (configProps == null) {
			configProps = new Properties();
		}

		// Copy framework properties from the system properties.
		Main.copySystemProperties(configProps);

		List list = new ArrayList(1);
		list.add(new AutoActivator(configProps));

		return new Object[] { configProps, list };
	}

	public void stop() throws Exception {
		if (platform != null) {
			try {
				platform.stop();
			}
			finally {
				context = null;
				platform = null;
				// remove cache folder
				IOUtils.delete(felixStorageDir);
			}
		}
	}
}