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

package org.springframework.osgi.mock;

import java.util.Properties;

import org.osgi.framework.Constants;

/**
 * Default properties used by the BundleContext.
 * 
 * @author Costin Leau
 * 
 */
class DefaultBundleContextProperties extends Properties {

	private static final long serialVersionUID = 7814061041669242672L;


	public DefaultBundleContextProperties() {
		this(null);
	}

	public DefaultBundleContextProperties(Properties defaults) {
		super(defaults);
		initProperties();
	}

	private static String getVersion() {
		Package pkg = MockBundleContext.class.getPackage();
		if (pkg != null) {
			String version = pkg.getImplementationVersion();
			if (version != null)
				return version;
		}

		return "unknown";
	}

	protected void initProperties() {
		put(Constants.FRAMEWORK_VERSION, getVersion());
		put(Constants.FRAMEWORK_VENDOR, "SpringSource");
		put(Constants.FRAMEWORK_LANGUAGE, System.getProperty("user.language"));
		put(Constants.FRAMEWORK_OS_NAME, System.getProperty("os.name"));
		put(Constants.FRAMEWORK_OS_VERSION, System.getProperty("os.version"));
		put(Constants.FRAMEWORK_PROCESSOR, System.getProperty("os.arch"));
	}
}
