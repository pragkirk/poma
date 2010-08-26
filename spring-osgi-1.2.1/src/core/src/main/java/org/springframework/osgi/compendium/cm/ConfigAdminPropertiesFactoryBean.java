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

package org.springframework.osgi.compendium.cm;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * FactoryBean returning the properties stored under a given persistent id in
 * the ConfigurationAdmin service. Once retrieved, the properties will remain
 * the same, even when the configuration object that it maps, changes.
 * 
 * <b>Note:</b> This implementation performs a lazy initialization of the
 * properties to receive the most up to date configuration.
 * 
 * @author Costin Leau
 * @see Configuration
 * @see ConfigurationAdmin
 * @see org.springframework.core.io.support.PropertiesFactoryBean
 */
public class ConfigAdminPropertiesFactoryBean implements BundleContextAware, InitializingBean, FactoryBean {

	private String persistentId;
	private Properties props;
	private BundleContext bundleContext;
	private boolean localOverride = false;
	private Properties localProperties;


	public void afterPropertiesSet() throws Exception {
		Assert.hasText(persistentId, "persistentId property is required");
		Assert.notNull(bundleContext, "bundleContext property is required");
	}

	public Object getObject() throws Exception {
		if (props == null) {
			props = readProperties();
		}
		return props;
	}

	/**
	 * Reads the current properties in the ConfigurationAdmin.
	 * 
	 * @return
	 */
	private Properties readProperties() {
		Properties properties = new Properties();

		// merge the local properties (upfront)
		if (localProperties != null && !localOverride) {
			CollectionUtils.mergePropertiesIntoMap(localProperties, properties);
		}

		ServiceReference ref = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
		if (ref != null) {
			ConfigurationAdmin cm = (ConfigurationAdmin) bundleContext.getService(ref);
			if (cm != null) {
				try {
					Dictionary dict = cm.getConfiguration(persistentId).getProperties();
					if (dict != null) {
						// copy properties into dictionary
						for (Enumeration enm = dict.keys(); enm.hasMoreElements();) {
							Object key = enm.nextElement();
							Object value = dict.get(key);
							properties.put(key, value);
						}
					}
				}
				catch (IOException ioe) {
					// FIXME: consider adding a custom/different exception
					throw new BeanInitializationException("Cannot retrieve configuration for pid=" + persistentId, ioe);
				}
			}
		}

		// merge local properties (if needed)
		if (localProperties != null && localOverride) {
			CollectionUtils.mergePropertiesIntoMap(localProperties, properties);
		}

		return properties;
	}

	public Class getObjectType() {
		return Properties.class;
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Returns the persistentId.
	 * 
	 * @return Returns the persistentId
	 */
	public String getPersistentId() {
		return persistentId;
	}

	/**
	 * Sets the ConfigurationAdmin persistent Id that the bean should read.
	 * 
	 * @param persistentId The persistentId to set.
	 */
	public void setPersistentId(String persistentId) {
		this.persistentId = persistentId;
	}

	/**
	 * Sets the local properties, e.g. via the nested tag in XML bean
	 * definitions. These can be considered defaults, to be overridden by
	 * properties loaded from the Configuration Admin.
	 */
	public void setProperties(Properties properties) {
		this.localProperties = properties;
	}

	/**
	 * Sets whether local properties override properties from files.
	 * <p>
	 * Default is "false": Properties from the Configuration Admin override
	 * local defaults. Can be switched to "true" to let local properties
	 * override the Configuration Admin properties.
	 */
	public void setLocalOverride(boolean localOverride) {
		this.localOverride = localOverride;
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
}
