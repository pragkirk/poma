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

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.osgi.framework.BundleContext;

/**
 * Equinox (3.2.x) OSGi platform.
 * 
 * @author Costin Leau
 * 
 */
public class EquinoxPlatform extends AbstractOsgiPlatform {

	private BundleContext context;


	public EquinoxPlatform() {
		toString = "Equinox OSGi Platform";
	}

	Properties getPlatformProperties() {
		// default properties
		Properties props = new Properties();
		props.setProperty("eclipse.ignoreApp", "true");
		props.setProperty("osgi.clean", "true");
		props.setProperty("osgi.noShutdown", "true");

		// local temporary folder for running tests
		// prevents accidental rewrites
		props.setProperty("osgi.configuration.area", "eclipse_config");
		props.setProperty("osgi.instance.area", "eclipse_config");
		props.setProperty("osgi.user.area", "eclipse_config");

		// props.setProperty("osgi.java.profile.bootdelegation", "ignore");

		// props.setProperty("eclipse.consoleLog", "true");
		// props.setProperty("osgi.debug", "");

		return props;
	}

	public BundleContext getBundleContext() {
		return context;
	}

	public void start() throws Exception {

		if (context == null) {
			// copy configuration properties to sys properties
			System.getProperties().putAll(getConfigurationProperties());

			// Equinox 3.1.x returns void - use of reflection is required
			// use main since in 3.1.x it sets up some system properties
			EclipseStarter.main(new String[0]);

			final Field field = EclipseStarter.class.getDeclaredField("context");

			AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					field.setAccessible(true);
					return null;
				}
			});
			context = (BundleContext) field.get(null);
		}
	}

	public void stop() throws Exception {
		if (context != null) {
			context = null;
			EclipseStarter.shutdown();
		}
	}
}