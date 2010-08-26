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
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for OsgiPlatform classes. Provides common functionality such as
 * creation a temporary folder on startup and deletion on shutdown. Uses system
 * properties to allow easy configuration from the command line.
 * 
 * @author Costin Leau
 */
abstract class AbstractOsgiPlatform implements OsgiPlatform {

	private static final String TMP_DIR_FALLBACK = "./tmp-test";

	private static final String DEFAULT_SUFFIX = "osgi";

	private static final String TMP_PREFIX = "org.sfw.osgi";

	final Log log = LogFactory.getLog(getClass());

	/**
	 * Subclasses should override this field.
	 */
	String toString = getClass().getName();

	private Properties configurationProperties = null;


	/**
	 * {@inheritDoc}
	 * 
	 * This implementation considers existing system properties as well as
	 * platform specific ones, defined in this class. The system properties are
	 * convenient for changing the configuration directly from the command line
	 * (useful for CI builds) leaving the programmer to ultimately decide the
	 * actual configuration used.
	 */
	public Properties getConfigurationProperties() {
		// check if defaults should apply
		if (configurationProperties == null) {
			configurationProperties = new Properties();
			// system properties
			configurationProperties.putAll(System.getProperties());
			// local properties
			configurationProperties.putAll(getPlatformProperties());
			return configurationProperties;
		}
		return configurationProperties;
	}

	/**
	 * Subclasses can override this to provide special platform properties.
	 * 
	 * @return platform implementation specific properties.
	 */
	abstract Properties getPlatformProperties();

	/**
	 * Returns the underlying OSGi platform name.
	 * 
	 * @return the platform name
	 */
	public String toString() {
		return toString;
	}

	File createTempDir(String suffix) {
		if (suffix == null)
			suffix = DEFAULT_SUFFIX;
		File tempFileName;

		try {
			tempFileName = File.createTempFile(TMP_PREFIX, suffix);
		}
		catch (IOException ex) {
			if (log.isWarnEnabled()) {
				log.warn("Could not create temporary directory, returning a temp folder inside the current folder", ex);
			}
			return new File(TMP_DIR_FALLBACK);
		}

		tempFileName.delete(); // we want it to be a directory...
		File tempFolder = new File(tempFileName.getAbsolutePath());
		tempFolder.mkdir();
		return tempFolder;
	}
}