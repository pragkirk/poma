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

package org.springframework.osgi.extender.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.osgi.extender.OsgiApplicationContextCreator;
import org.springframework.osgi.extender.support.scanning.ConfigurationScanner;
import org.springframework.osgi.extender.support.scanning.DefaultConfigurationScanner;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Default {@link OsgiApplicationContextCreator} implementation.
 * 
 * <p/> Creates an {@link OsgiBundleXmlApplicationContext} instance using the
 * default locations (<tt>Spring-Context</tt> manifest header or
 * <tt>META-INF/spring/*.xml</tt>) if available, null otherwise.
 * 
 * <p/> Additionally, this implementation allows custom locations to be
 * specified through {@link ConfigurationScanner} interface.
 * 
 * @see ConfigurationScanner
 * @author Costin Leau
 * 
 */
public class DefaultOsgiApplicationContextCreator implements OsgiApplicationContextCreator {

	/** logger */
	private static final Log log = LogFactory.getLog(DefaultOsgiApplicationContextCreator.class);

	private ConfigurationScanner configurationScanner = new DefaultConfigurationScanner();


	public DelegatedExecutionOsgiBundleApplicationContext createApplicationContext(BundleContext bundleContext)
			throws Exception {
		Bundle bundle = bundleContext.getBundle();
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(bundle, configurationScanner);
		if (log.isTraceEnabled())
			log.trace("Created configuration " + config + " for bundle "
					+ OsgiStringUtils.nullSafeNameAndSymName(bundle));

		// it's not a spring bundle, ignore it
		if (!config.isSpringPoweredBundle()) {
			return null;
		}

		log.info("Discovered configurations " + ObjectUtils.nullSafeToString(config.getConfigurationLocations())
				+ " in bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "]");

		DelegatedExecutionOsgiBundleApplicationContext sdoac = new OsgiBundleXmlApplicationContext(
			config.getConfigurationLocations());
		sdoac.setBundleContext(bundleContext);
		sdoac.setPublishContextAsService(config.isPublishContextAsService());

		return sdoac;
	}

	/**
	 * Sets the configurationScanner used by this creator.
	 * 
	 * @param configurationScanner The configurationScanner to set.
	 */
	public void setConfigurationScanner(ConfigurationScanner configurationScanner) {
		Assert.notNull(configurationScanner);
		this.configurationScanner = configurationScanner;
	}
}
