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

/**
 * Lifecycle contract for the OSGi platform.
 * 
 * @author Costin Leau
 * 
 */
public interface OsgiPlatform {

	/**
	 * Starts the OSGi platform.
	 * 
	 * @throws Exception if starting the platform fails
	 */
	void start() throws Exception;

	/**
	 * Stops the OSGi platform.
	 * 
	 * @throws Exception if stopping the platform fails.
	 */
	void stop() throws Exception;

	/**
	 * Returns the {@link java.util.Properties} object used for configuring the
	 * underlying OSGi implementation before starting it.
	 * 
	 * @return platform implementation specific properties
	 */
	Properties getConfigurationProperties();

	/**
	 * Returns the bundle context of the returned platform. Useful during
	 * startup for installing bundles and interacting with the OSGi instance.
	 * 
	 * @return platform bundle context
	 */
	BundleContext getBundleContext();
}
