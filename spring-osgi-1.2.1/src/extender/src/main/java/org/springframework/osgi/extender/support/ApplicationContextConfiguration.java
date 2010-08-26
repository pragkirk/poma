/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.springframework.osgi.extender.support;

import java.util.Dictionary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.springframework.osgi.extender.support.internal.ConfigUtils;
import org.springframework.osgi.extender.support.scanning.ConfigurationScanner;
import org.springframework.osgi.extender.support.scanning.DefaultConfigurationScanner;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Configuration class for Spring-DM application contexts.
 * 
 * Determines the configuration information available in a bundle for
 * constructing an application context. Reads all the Spring-DM options present
 * in the bundle header.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
public class ApplicationContextConfiguration {

	/** logger */
	private static final Log log = LogFactory.getLog(ApplicationContextConfiguration.class);

	private final Bundle bundle;

	private final ConfigurationScanner configurationScanner;

	private final boolean asyncCreation;

	private final String[] configurationLocations;

	private final boolean isSpringPoweredBundle;

	private final boolean publishContextAsService;

	private final boolean waitForDeps;

	private final String toString;

	private final long timeout;


	/**
	 * Constructs a new <code>ApplicationContextConfiguration</code> instance
	 * from the given bundle. Uses the {@link DefaultConfigurationScanner}
	 * internally for discovering Spring-powered bundles.
	 * 
	 * @param bundle bundle for which the application context configuration is
	 * created
	 */
	public ApplicationContextConfiguration(Bundle bundle) {
		this(bundle, new DefaultConfigurationScanner());
	}

	public ApplicationContextConfiguration(Bundle bundle, ConfigurationScanner configurationScanner) {
		Assert.notNull(bundle);
		Assert.notNull(configurationScanner);
		this.bundle = bundle;
		this.configurationScanner = configurationScanner;

		Dictionary headers = this.bundle.getHeaders();

		String[] configs = this.configurationScanner.getConfigurations(bundle);

		this.isSpringPoweredBundle = !ObjectUtils.isEmpty(configs);
		this.configurationLocations = configs;
		long option = ConfigUtils.getTimeOut(headers);
		// translate into ms
		this.timeout = (option >= 0 ? option * 1000 : option);
		this.publishContextAsService = ConfigUtils.getPublishContext(headers);
		this.asyncCreation = ConfigUtils.getCreateAsync(headers);
		this.waitForDeps = ConfigUtils.getWaitForDependencies(headers);

		// create toString
		StringBuffer buf = new StringBuffer();
		buf.append("AppCtxCfg [Bundle=");
		buf.append(OsgiStringUtils.nullSafeSymbolicName(bundle));
		buf.append("]isSpringBundle=");
		buf.append(isSpringPoweredBundle);
		buf.append("|async=");
		buf.append(asyncCreation);
		buf.append("|wait-for-deps=");
		buf.append(waitForDeps);
		buf.append("|publishCtx=");
		buf.append(publishContextAsService);
		buf.append("|timeout=");
		buf.append(timeout / 1000);
		buf.append("s");
		toString = buf.toString();
		if (log.isTraceEnabled()) {
			log.trace("Configuration: " + toString);
		}
	}

	/**
	 * Indicates if the given bundle is "Spring-Powered" or not.
	 * 
	 * True if this bundle has at least one defined application context
	 * configuration file.
	 * 
	 * <p/> A bundle is "Spring-Powered" if it has at least one configuration
	 * resource.
	 */
	public boolean isSpringPoweredBundle() {
		return this.isSpringPoweredBundle;
	}

	/**
	 * Returns the timeout (in milliseconds) an application context needs to
	 * wait for mandatory dependent services.
	 */
	public long getTimeout() {
		return this.timeout;
	}

	/**
	 * Indicates if an application context needs to be created asynchronously or
	 * not.
	 * 
	 * Should the application context wait for all non-optional service
	 * references to be satisfied before starting?
	 */
	public boolean isCreateAsynchronously() {
		return this.asyncCreation;
	}

	/**
	 * Indicates if the application context needs to be published as a service
	 * or not.
	 * 
	 * @return Returns the publishContextAsService.
	 */
	public boolean isPublishContextAsService() {
		return publishContextAsService;
	}

	/**
	 * Indicates if the configuration must wait for dependencies.
	 * 
	 * @return true if the configuration indicates that dependencies should be
	 * waited for.
	 */
	public boolean isWaitForDependencies() {
		return waitForDeps;
	}

	/**
	 * Returns the locations of the configuration resources used to build the
	 * application context (as Spring resource paths).
	 * 
	 * @return configuration paths
	 */
	public String[] getConfigurationLocations() {
		return this.configurationLocations;
	}

	public String toString() {
		return toString;
	}
}
