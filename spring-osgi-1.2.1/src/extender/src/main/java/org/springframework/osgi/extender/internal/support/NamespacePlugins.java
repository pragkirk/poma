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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.core.CollectionFactory;
import org.springframework.osgi.util.BundleDelegatingClassLoader;
import org.springframework.osgi.util.OsgiStringUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Spring schema handler/resolver for OSGi environments.
 * 
 * Besides delegation this class also does type filtering to avoid wiring the
 * wrong bundle if multiple versions of the same library (which support the same
 * schema) are available.
 * 
 * @author Hal Hildebrand
 * @author Costin Leau
 * 
 */
public class NamespacePlugins implements NamespaceHandlerResolver, EntityResolver, DisposableBean {

	/**
	 * Wrapper class which implements both {@link EntityResolver} and
	 * {@link NamespaceHandlerResolver} interfaces.
	 * 
	 * Simply delegates to the actual implementation discovered in a specific
	 * bundle.
	 */
	private static class Plugin implements NamespaceHandlerResolver, EntityResolver {

		private final NamespaceHandlerResolver namespace;

		private final EntityResolver entity;

		private final Bundle bundle;


		private Plugin(Bundle bundle) {
			this.bundle = bundle;

			ClassLoader loader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle);

			entity = new DelegatingEntityResolver(loader);
			namespace = new DefaultNamespaceHandlerResolver(loader);
		}

		public NamespaceHandler resolve(String namespaceUri) {
			return namespace.resolve(namespaceUri);
		}

		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			return entity.resolveEntity(publicId, systemId);
		}

		public Bundle getBundle() {
			return bundle;
		}
	}


	private static final Log log = LogFactory.getLog(NamespacePlugins.class);

	private final Map plugins = CollectionFactory.createConcurrentMap(5);


	public void addHandler(Bundle bundle) {
		if (log.isDebugEnabled())
			log.debug("Adding as handler " + OsgiStringUtils.nullSafeNameAndSymName(bundle));

		plugins.put(bundle, new Plugin(bundle));
	}

	/**
	 * Return true if a handler mapping was removed for the given bundle.
	 * 
	 * @param bundle bundle to look at
	 * @return true if the bundle was used in the plugin map
	 */
	public boolean removeHandler(Bundle bundle) {
		if (log.isDebugEnabled())
			log.debug("Removing handler " + OsgiStringUtils.nullSafeNameAndSymName(bundle));

		return (plugins.remove(bundle) != null);
	}

	public NamespaceHandler resolve(final String namespaceUri) {
		if (System.getSecurityManager() != null) {
			return (NamespaceHandler) AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					return doResolve(namespaceUri);
				}
			});

		}
		else {
			return doResolve(namespaceUri);
		}
	}

	public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
		if (System.getSecurityManager() != null) {
			try {
				return (InputSource) AccessController.doPrivileged(new PrivilegedExceptionAction() {

					public Object run() throws Exception {
						return doResolveEntity(publicId, systemId);
					}
				});
			}
			catch (PrivilegedActionException pae) {
				Exception cause = pae.getException();
				if (cause instanceof IOException) {
					throw (IOException) cause;
				}
				else
					throw (SAXException) cause;
			}
		}
		else {
			return doResolveEntity(publicId, systemId);
		}
	}

	private NamespaceHandler doResolve(String namespaceUri) {
		boolean debug = log.isDebugEnabled();

		if (debug)
			log.debug("Trying to resolving namespace handler for " + namespaceUri);

		for (Iterator i = plugins.values().iterator(); i.hasNext();) {
			Plugin plugin = (Plugin) i.next();
			try {
				NamespaceHandler handler = plugin.resolve(namespaceUri);
				if (handler != null) {
					if (debug)
						log.debug("Namespace handler for " + namespaceUri + " found inside "
								+ OsgiStringUtils.nullSafeNameAndSymName(plugin.getBundle()));

					return handler;
				}
			}
			catch (IllegalArgumentException ex) {
				if (debug)
					log.debug("Namespace handler for " + namespaceUri + " not found inside "
							+ OsgiStringUtils.nullSafeNameAndSymName(plugin.getBundle()));

			}
		}
		return null;
	}

	private InputSource doResolveEntity(String publicId, String systemId) throws SAXException, IOException {
		boolean debug = log.isDebugEnabled();

		if (debug)
			log.debug("Trying to resolving entity for " + publicId + "|" + systemId);

		if (systemId != null) {
			for (Iterator i = plugins.values().iterator(); i.hasNext();) {
				InputSource inputSource;
				Plugin plugin = (Plugin) i.next();
				try {
					inputSource = plugin.resolveEntity(publicId, systemId);
					if (inputSource != null) {
						if (debug)
							log.debug("XML schema for " + publicId + "|" + systemId + " found inside "
									+ OsgiStringUtils.nullSafeNameAndSymName(plugin.getBundle()));
						return inputSource;
					}

				}
				catch (FileNotFoundException ex) {
					if (debug)
						log.debug("XML schema for " + publicId + "|" + systemId + " not found inside "
								+ OsgiStringUtils.nullSafeNameAndSymName(plugin.getBundle()), ex);
				}
			}
		}

		return null;
	}

	public void destroy() {
		plugins.clear();
	}
}
