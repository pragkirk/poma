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

package org.springframework.osgi.compendium.internal.cm;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiServiceUtils;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.osgi.util.internal.MapBasedDictionary;

/**
 * Class responsible for interacting with the Configuration Admin service. It
 * handles the retrieval and updates for a given persistent id.
 * 
 * @author Costin Leau
 * @see org.osgi.service.cm.ConfigurationAdmin
 * @see ManagedService
 */
class ConfigurationAdminManager implements DisposableBean {

	/**
	 * Configuration Admin whiteboard 'listener'.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private class ConfigurationWatcher implements ManagedService {

		public void updated(Dictionary props) throws ConfigurationException {
			if (log.isTraceEnabled())
				log.trace("Configuration [" + pid + "] has been updated with properties " + props);

			synchronized (monitor) {
				// update properties
				properties = new MapBasedDictionary(props);
				// invoke callback
				if (beanManager != null)
					beanManager.updated(properties);
			}
		}
	}


	/** logger */
	private static final Log log = LogFactory.getLog(ConfigurationAdminManager.class);

	private final BundleContext bundleContext;
	private final String pid;
	// up to date configuration
	private Map properties = null;
	private boolean initialized = false;
	private ManagedServiceBeanManager beanManager;
	private final Object monitor = new Object();

	private ServiceRegistration registration;


	/**
	 * Constructs a new <code>ConfigurationAdminManager</code> instance.
	 * 
	 */
	public ConfigurationAdminManager(String pid, BundleContext bundleContext) {
		this.pid = pid;
		this.bundleContext = bundleContext;
	}

	public void setBeanManager(ManagedServiceBeanManager beanManager) {
		synchronized (monitor) {
			this.beanManager = beanManager;
		}
	}

	/**
	 * Returns the configuration 'monitored' by this managed.
	 * 
	 * @return monitored configuration
	 */
	public Map getConfiguration() {
		initialize();
		synchronized (monitor) {
			return properties;
		}
	}

	/**
	 * Initializes the conversation with the configuration admin. This method
	 * allows for lazy service registration to avoid notification being sent w/o
	 * any beans requesting it.
	 */
	private void initialize() {
		synchronized (monitor) {
			if (initialized)
				return;
			initialized = true;

			// initialize the properties
			initProperties();
		}

		if (log.isTraceEnabled())
			log.trace("Initial properties for pid [" + pid + "] are " + properties);

		Properties props = new Properties();
		props.put(Constants.SERVICE_PID, pid);
		Bundle bundle = bundleContext.getBundle();
		props.put(Constants.BUNDLE_SYMBOLICNAME, OsgiStringUtils.nullSafeSymbolicName(bundle));
		props.put(Constants.BUNDLE_VERSION, OsgiBundleUtils.getBundleVersion(bundle));

		ServiceRegistration reg = bundleContext.registerService(ManagedService.class.getName(),
			new ConfigurationWatcher(), props);

		synchronized (monitor) {
			this.registration = reg;
		}
	}

	private void initProperties() {
		ServiceReference ref = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
		if (ref != null) {
			ConfigurationAdmin cm = (ConfigurationAdmin) bundleContext.getService(ref);
			if (cm != null) {
				try {
					properties = new MapBasedDictionary(cm.getConfiguration(pid).getProperties());
				}
				catch (IOException ioe) {
					// FIXME: consider adding a custom/different exception
					throw new BeanInitializationException("Cannot retrieve configuration for pid=" + pid, ioe);
				}
			}
		}
	}

	public void destroy() {
		ServiceRegistration reg = null;
		synchronized (monitor) {
			reg = this.registration;
			this.registration = null;
		}

		if (OsgiServiceUtils.unregisterService(reg)) {
			log.trace("Shutting down CM tracker for pid [" + pid + "]");
		}
	}
}