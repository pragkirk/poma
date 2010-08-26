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

package org.springframework.osgi.context;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Interface that extends <code>ConfigurableApplicationContext</code> to
 * provides OSGi specific functionality.
 * 
 * <p>
 * <strong>Note:</strong> Just like its ancestors,the setters of this interface
 * should be called before <code>refresh</code>ing the
 * <code>ApplicationContext</code>
 * 
 * @author Costin Leau
 */
public interface ConfigurableOsgiBundleApplicationContext extends ConfigurableApplicationContext {

	/**
	 * Service entry used for specifying the application context name when
	 * published as an OSGi service
	 */
	static final String APPLICATION_CONTEXT_SERVICE_PROPERTY_NAME = "org.springframework.context.service.name";

	/**
	 * Bean name under which the OSGi bundle context is published as a
	 * singleton.
	 */
	static final String BUNDLE_CONTEXT_BEAN_NAME = "bundleContext";


	/**
	 * Sets the config locations for this OSGi bundle application context. If
	 * not set, the implementation is supposed to use a default for the given
	 * bundle.
	 * 
	 * @param configLocations array of configuration locations
	 */
	void setConfigLocations(String[] configLocations);

	/**
	 * Sets the <code>BundleContext</code> used by this OSGi bundle
	 * application context. Normally it's the <code>BundleContext</code> in
	 * which the context runs.
	 * 
	 * <p>
	 * Does not cause an initialization of the context: {@link #refresh()} needs
	 * to be called after the setting of all configuration properties.
	 * 
	 * @param bundleContext the <code>BundleContext</code> used by this
	 * application context.
	 * @see #refresh()
	 */
	void setBundleContext(BundleContext bundleContext);

	/**
	 * Return the <code>BundleContext</code> for this application context.
	 * This method is offered as a helper since as of OSGi 4.1, the bundle
	 * context can be discovered directly from the given bundle.
	 * 
	 * @return the <code>BundleContext</code> in which this application
	 * context runs
	 * 
	 * @see #getBundle()
	 */
	BundleContext getBundleContext();

	/**
	 * Returns the OSGi <code>Bundle</code> for this application context.
	 * 
	 * @return the <code>Bundle</code> for this OSGi bundle application
	 * context.
	 */
	Bundle getBundle();

	/**
	 * Indicates whether this application context should be publish as an OSGi
	 * service if successfully started. By default, this is set to
	 * <code>true</code>.
	 * 
	 * @param publishContextAsService true if the application context should be
	 * published as a service, false otherwise
	 */
	void setPublishContextAsService(boolean publishContextAsService);
}
