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

package org.springframework.osgi.extender.internal.support;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.osgi.extender.internal.util.BundleUtils;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiServiceUtils;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.Assert;
import org.xml.sax.EntityResolver;

/**
 * Support class that deals with namespace parsers discovered inside Spring bundles.
 * 
 * @author Costin Leau
 * 
 */
public class NamespaceManager implements InitializingBean, DisposableBean {

	private static final Log log = LogFactory.getLog(NamespaceManager.class);

	private static final String NS_HANDLER_RESOLVER_CLASS_NAME = NamespaceHandlerResolver.class.getName();

	/**
	 * The set of all namespace plugins known to the extender
	 */
	private NamespacePlugins namespacePlugins;

	/**
	 * ServiceRegistration object returned by OSGi when registering the NamespacePlugins instance as a service
	 */
	private ServiceRegistration nsResolverRegistration, enResolverRegistration = null;

	/**
	 * OSGi Environment.
	 */
	private final BundleContext context;

	private final String extenderInfo;

	private static final String META_INF = "META-INF/";

	private static final String SPRING_HANDLERS = "spring.handlers";

	private static final String SPRING_SCHEMAS = "spring.schemas";

	/**
	 * Constructor.
	 * 
	 * @param extenderBundleContext
	 */
	public NamespaceManager(BundleContext context) {
		this.context = context;

		extenderInfo = context.getBundle().getSymbolicName() + "|"
				+ OsgiBundleUtils.getBundleVersion(context.getBundle());

		// detect package admin
		this.namespacePlugins = new NamespacePlugins();
	}

	/**
	 * If this bundle defines handler mapping or schema mapping resources, then register it with the namespace plugin
	 * handler.
	 * 
	 * <p/> This method considers only the bundle space and not the class space.
	 * 
	 * @param bundle
	 */
	public void maybeAddNamespaceHandlerFor(Bundle bundle) {
		// Ignore system bundle
		if (OsgiBundleUtils.isSystemBundle(bundle)) {
			return;
		}
		// Ignore non-wired Spring DM bundles
		if ("org.springframework.osgi.core".equals(bundle.getSymbolicName())
				&& !bundle.equals(BundleUtils.getDMCoreBundle(context))) {
			return;
		}

		boolean hasHandlers = bundle.findEntries(META_INF, SPRING_HANDLERS, false) != null;
		boolean hasSchemas = bundle.findEntries(META_INF, SPRING_SCHEMAS, false) != null;

		// if the bundle defines handlers
		if (hasHandlers) {
			// check type compatibility between the bundle's and spring-extender's spring version
			if (hasCompatibleNamespaceType(bundle)) {
				addHandler(bundle);
			} else {
				if (log.isDebugEnabled())
					log.debug("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle)
							+ "] declares namespace handlers but is not compatible with extender [" + extenderInfo
							+ "]; ignoring...");
			}
		} else {
			// bundle declares only schemas, add it though the handlers might not be compatible...
			if (hasSchemas)
				addHandler(bundle);
		}
	}

	private boolean hasCompatibleNamespaceType(Bundle bundle) {
		try {
			Class type = bundle.loadClass(NS_HANDLER_RESOLVER_CLASS_NAME);
			return NamespaceHandlerResolver.class.equals(type);
		} catch (Throwable th) {
			// if the interface is not wired, ignore the bundle
			log.warn("Bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle) + " cannot see class ["
					+ NS_HANDLER_RESOLVER_CLASS_NAME + "]; ignoring its namespace handlers");

			return false;
		}
	}

	/**
	 * Add this bundle to those known to provide handler or schema mappings. This method expects that the validity check
	 * (whatever that is) has been already done.
	 * 
	 * @param bundle
	 */
	protected void addHandler(Bundle bundle) {
		Assert.notNull(bundle);
		if (log.isDebugEnabled()) {
			log.debug("Adding namespace handler resolver for " + OsgiStringUtils.nullSafeNameAndSymName(bundle));
		}

		this.namespacePlugins.addHandler(bundle);
	}

	/**
	 * Remove this bundle from the set of those known to provide handler or schema mappings.
	 * 
	 * @param bundle
	 */
	public void maybeRemoveNameSpaceHandlerFor(Bundle bundle) {
		Assert.notNull(bundle);
		boolean removed = this.namespacePlugins.removeHandler(bundle);
		if (removed && log.isDebugEnabled()) {
			log.debug("Removed namespace handler resolver for " + OsgiStringUtils.nullSafeNameAndSymName(bundle));
		}
	}

	/**
	 * Register the NamespacePlugins instance as an Osgi Resolver service
	 */
	private void registerResolverServices() {
		if (log.isDebugEnabled()) {
			log.debug("Registering Spring NamespaceHandlerResolver and EntityResolver...");
		}
		Bundle bnd = BundleUtils.getDMCoreBundle(context);

		Properties props = null;
		if (bnd != null) {
			props = new Properties();
			props.setProperty(BundleUtils.DM_CORE_ID, "" + bnd.getBundleId());
			props.setProperty(BundleUtils.DM_CORE_TS, "" + bnd.getLastModified());
		}

		nsResolverRegistration = context.registerService(new String[] { NamespaceHandlerResolver.class.getName() },
				this.namespacePlugins, props);

		enResolverRegistration = context.registerService(new String[] { EntityResolver.class.getName() },
				this.namespacePlugins, props);
	}

	/**
	 * Unregister the NamespaceHandler and EntityResolver service
	 */
	private void unregisterResolverService() {

		boolean result = OsgiServiceUtils.unregisterService(nsResolverRegistration);
		result = result || OsgiServiceUtils.unregisterService(enResolverRegistration);

		if (result) {
			if (log.isDebugEnabled())
				log.debug("Unregistering Spring NamespaceHandler and EntityResolver service");
		}

		this.nsResolverRegistration = null;
		this.enResolverRegistration = null;
	}

	/**
	 * @return Returns the namespacePlugins.
	 */
	public NamespacePlugins getNamespacePlugins() {
		return namespacePlugins;
	}

	//
	// Lifecycle methods
	//

	public void afterPropertiesSet() {
		registerResolverServices();
	}

	public void destroy() {
		unregisterResolverService();
		this.namespacePlugins.destroy();
		this.namespacePlugins = null;
	}
}