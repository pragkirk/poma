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
 *
 */

package org.springframework.osgi.bundle;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link Bundle} installer.
 * 
 * <p/> This {@link FactoryBean} allows customers to use Spring to drive bundle
 * management. Bundles states can be modified using the <code>action</code>
 * (defaults to <em>start</em>) and <code>destroyAction</code> (not set by
 * default) parameters.
 * 
 * <p/> For example, to automatically install and start a bundle from the local
 * maven repository (assuming the bundle has been already retrieved), one can
 * use the following configuration:
 * 
 * <pre class="code">
 * &lt;osgi:bundle id=&quot;aBundle&quot; symbolic-name=&quot;org.company.bundles.a&quot;
 *  location=&quot;file:${localRepository}/org/company/bundles/a/${pom.version}/a-${pom.version}.jar&quot; 
 *  action=&quot;start&quot;/&gt;
 * </pre>
 * 
 * 
 * <p/><strong>Note:</strong> Pay attention when installing bundles
 * dynamically since classes can be loaded aggressively.
 * 
 * @author Andy Piper
 * @author Costin Leau
 * @see BundleAction
 */
public class BundleFactoryBean implements FactoryBean, BundleContextAware, InitializingBean, DisposableBean,
		ResourceLoaderAware {

	private static Log log = LogFactory.getLog(BundleFactoryBean.class);

	// bundle info
	/** Bundle location */
	private String location;

	private Resource resource;

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	/** Bundle symName */
	private String symbolicName;

	/** Actual bundle */
	private Bundle bundle;

	private BundleContext bundleContext;

	private BundleAction action, destroyAction;

	private int startLevel;

	private ClassLoader classLoader;

	/** unused at the moment */
	private boolean pushBundleAsContextClassLoader = false;


	// FactoryBean methods
	public Class getObjectType() {
		return (bundle != null ? bundle.getClass() : Bundle.class);
	}

	public boolean isSingleton() {
		return true;
	}

	public Object getObject() throws Exception {
		return bundle;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(bundleContext, "BundleContext is required");

		// check parameters
		if (bundle == null && !StringUtils.hasText(symbolicName) && !StringUtils.hasText(location))
			throw new IllegalArgumentException("at least one of symbolicName, location, bundle properties is required ");

		// try creating a resource
		if (getLocation() != null) {
			resource = resourceLoader.getResource(getLocation());
		}

		// find the bundle first of all
		if (bundle == null) {
			bundle = findBundle();
		}

		updateStartLevel(getStartLevel());

		if (log.isDebugEnabled())
			log.debug("working with bundle[" + OsgiStringUtils.nullSafeNameAndSymName(bundle));

		if (log.isDebugEnabled())
			log.debug("executing start-up action " + action);
		executeAction(action);
	}

	public void destroy() throws Exception {
		if (log.isDebugEnabled())
			log.debug("executing shutdown action " + action);

		executeAction(destroyAction);

		bundle = null;
		classLoader = null;
	}

	protected void executeAction(BundleAction action) {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		try {
			if (pushBundleAsContextClassLoader) {
				Thread.currentThread().setContextClassLoader(classLoader);
			}

			// switch statement
			// might look ugly but it's the only way to support the
			// install/update variants
			try {

				// Apply these actions only if we have a bundle, do not subsequently install a bundle
				// if none exists
				if (BundleAction.STOP == action) {
					if (bundle != null) {
						bundle.stop();
					}
				}

				else if (BundleAction.UNINSTALL == action) {
					if (bundle != null) {
						bundle.uninstall();
					}
				}
				// Apply these actions and install a bundle if necessary
				else if (BundleAction.INSTALL == action) {
					bundle = installBundle();
				}

				else if (BundleAction.START == action) {
					if (bundle == null) {
						bundle = installBundle();
					}
					bundle.start();
				}

				else if (BundleAction.UPDATE == action) {
					if (bundle == null) {
						bundle = installBundle();
					}
					bundle.update();
				}
				// Default is to do nothing

			}
			catch (BundleException be) {
				throw (RuntimeException) new IllegalStateException("cannot execute action " + action.getLabel()
						+ " on bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle)).initCause(be);
			}
		}
		finally {
			if (pushBundleAsContextClassLoader) {
				Thread.currentThread().setContextClassLoader(ccl);
			}
		}
	}

	/**
	 * Install bundle - the equivalent of install action.
	 * 
	 * @return
	 * @throws BundleException
	 */
	private Bundle installBundle() throws BundleException {
		Assert.hasText(location, "location parameter required when installing a bundle");

		// install bundle (default)
		log.info("Loading bundle from [" + location + "]");

		Bundle bundle = null;
		boolean installBasedOnLocation = (resource == null);

		if (!installBasedOnLocation) {
			InputStream stream = null;
			try {
				stream = resource.getInputStream();
			}
			catch (IOException ex) {
				// catch it since we fallback on normal install
				installBasedOnLocation = true;
			}
			if (!installBasedOnLocation)
				bundle = bundleContext.installBundle(location, stream);
		}

		if (installBasedOnLocation)
			bundle = bundleContext.installBundle(location);

		return bundle;
	}

	/**
	 * Find a bundle based on the configuration (don't apply any actions for
	 * it).
	 * 
	 * @return a Bundle instance based on the configuration.
	 */
	private Bundle findBundle() {
		// first consider symbolicName
		Bundle bundle = null;

		// try to find the bundle
		if (StringUtils.hasText(symbolicName))
			bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, symbolicName);

		return bundle;
	}

	/**
	 * Return the {@link Resource} object (if a {@link ResourceLoader} is
	 * available) from the given location (if any).
	 * 
	 * @return {@link Resource} object for the given location
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Return the given location.
	 * 
	 * @return bundle location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Set the bundle location (optional operation).
	 * 
	 * @param url bundle location (normally an URL or a Spring Resource)
	 * 
	 */
	public void setLocation(String url) {
		location = url;
	}

	/**
	 * Return the given bundle symbolic name.
	 * 
	 * @return bundle symbolic name
	 */
	public String getSymbolicName() {
		return symbolicName;
	}

	/**
	 * Set the bundle symbolic name (optional operation).
	 * 
	 * @param symbolicName bundle symbolic name
	 */
	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public void setBundleContext(BundleContext context) {
		bundleContext = context;
	}

	/**
	 * Returns the action.
	 * 
	 * @return Returns the action
	 */
	public BundleAction getAction() {
		return action;
	}

	/**
	 * Action to execute at startup.
	 * 
	 * @param action action to execute at startup
	 */
	public void setAction(BundleAction action) {
		this.action = action;
	}

	/**
	 * Returns the destroyAction.
	 * 
	 * @return Returns the destroyAction
	 */
	public BundleAction getDestroyAction() {
		return destroyAction;
	}

	/**
	 * Action to execute at shutdown.
	 * 
	 * @param action action to execute at shutdown
	 */
	public void setDestroyAction(BundleAction action) {
		this.destroyAction = action;
	}

	/**
	 * Gets the bundle start level.
	 * 
	 * @return bundle start level
	 */
	public int getStartLevel() {
		return startLevel;
	}

	/**
	 * Sets the bundle start level.
	 * 
	 * @param startLevel bundle start level.
	 */
	public void setStartLevel(int startLevel) {
		this.startLevel = startLevel;
	}

	/**
	 * Determines whether invocations on the remote service should be performed
	 * in the context (thread context class loader) of the target bundle's
	 * ClassLoader. The default is <code>false</code>.
	 * 
	 * @param pushBundleAsContextClassLoader true if the thread context class
	 * loader will be set to the target bundle or false otherwise
	 */
	public void setPushBundleAsContextClassLoader(boolean pushBundleAsContextClassLoader) {
		this.pushBundleAsContextClassLoader = pushBundleAsContextClassLoader;
	}

	public void setClassLoader(ClassLoader classloader) {
		this.classLoader = classloader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	// TODO: improve startlevel handling
	private void updateStartLevel(int level) {
		if (level == 0 || bundle == null)
			return;
		// Set the start level of the bundle if we are able.
		ServiceReference startref = bundleContext.getServiceReference(StartLevel.class.getName());
		if (startref != null) {
			StartLevel start = (StartLevel) bundleContext.getService(startref);
			if (start != null) {
				start.setBundleStartLevel(bundle, level);
			}
			bundleContext.ungetService(startref);
		}
	}

	/**
	 * Returns the bundle with which the class interacts.
	 * 
	 * @return Returns this factory backing bundle
	 */
	public Bundle getBundle() {
		return bundle;
	}

	/**
	 * Set the backing bundle used by this class. Allows programmatic
	 * configuration of already retrieved/created bundle.
	 * 
	 * @param bundle The bundle to set
	 */
	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}

}
