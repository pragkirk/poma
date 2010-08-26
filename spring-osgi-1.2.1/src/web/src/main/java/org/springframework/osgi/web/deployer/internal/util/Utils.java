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

package org.springframework.osgi.web.deployer.internal.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.service.importer.support.ImportContextClassLoader;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
import org.springframework.osgi.util.BundleDelegatingClassLoader;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * Utility class for IO operations regarding web integration.
 * 
 * @author Costin Leau
 */
public abstract class Utils {

	/** logger */
	private static final Log log = LogFactory.getLog(Utils.class);

	private static final String SLASH = "/";


	/**
	 * Copies the given bundle content to the given target folder. This means
	 * unpacking the bundle archive. In case of a failure, an exception is
	 * thrown.
	 * 
	 * @param bundle
	 * @param targetFolder
	 */
	public static void unpackBundle(Bundle bundle, File targetFolder) {
		// no need to use a recursive method since we get all resources directly
		Enumeration enm = bundle.findEntries(SLASH, null, true);
		while (enm != null && enm.hasMoreElements()) {
			boolean trace = log.isTraceEnabled();

			// get only the path
			URL url = (URL) enm.nextElement();
			String entryPath = url.getPath();
			if (entryPath.startsWith(SLASH))
				entryPath = entryPath.substring(1);

			File targetFile = new File(targetFolder, entryPath);
			// folder are a special case, we have to create them rather then copy
			if (entryPath.endsWith("/"))
				targetFile.mkdirs();
			else {
				try {
					// handle missing parent folders
					File parent = targetFile.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}

					OutputStream targetStream = new FileOutputStream(targetFile);
					if (trace)
						log.trace("Copying " + url + " to " + targetFile);
					FileCopyUtils.copy(url.openStream(), targetStream);
				}
				catch (IOException ex) {
					//
					log.error("Cannot copy resource " + entryPath, ex);
					throw (RuntimeException) new IllegalStateException("IO exception while unpacking bundle "
							+ OsgiStringUtils.nullSafeNameAndSymName(bundle)).initCause(ex);
				}
				// no need to close the streams - the utils already handles that
			}
		}
	}

	/**
	 * Dedicated utility method used for creating an OSGi reference to
	 * Jetty/Tomcat/XXX server service.
	 * 
	 * @return proxy to the found OSGi service
	 */
	public static Object createServerServiceProxy(BundleContext bundleContext, Class proxyType, String serviceName) {

		OsgiServiceProxyFactoryBean proxyFB = new OsgiServiceProxyFactoryBean();
		proxyFB.setBundleContext(bundleContext);
		proxyFB.setContextClassLoader(ImportContextClassLoader.UNMANAGED);
		proxyFB.setInterfaces(new Class[] { proxyType });
		// use the spring-dm class loader to generate the proxy (since it can see all the needed server classes)
		proxyFB.setBeanClassLoader(proxyType.getClassLoader());
		// wait 5 seconds
		proxyFB.setTimeout(5 * 1000);
		if (StringUtils.hasText(serviceName))
			proxyFB.setServiceBeanName(serviceName);
		proxyFB.afterPropertiesSet();

		return proxyFB.getObject();
	}

	/**
	 * Detects the Jasper/JSP parser (used by the server) and returns a chained
	 * class-loader which incorporates them all. This allows the web application
	 * to use servlets and JSP w/o importing them (just like in traditional
	 * environments).
	 * 
	 * @return chained classloader containing javax. packages and the sever
	 * classes + Jasper/JSP compiler (if present)
	 */
	public static ClassLoader chainedWebClassLoaders(Class serverClass) {
		Assert.notNull(serverClass);
		ClassLoader serverLoader = serverClass.getClassLoader();
		ClassLoader jasperLoader = findClassLoaderFor(JasperUtils.JASPER_CLASS, serverLoader);

		// the server class does not import Jasper, use the web bundle instead
		if (jasperLoader == null) {
			// use the extender classloader
			jasperLoader = findClassLoaderFor(JasperUtils.JASPER_CLASS, Utils.class.getClassLoader());
		}

		// Jasper cannot be found, assume it's not installed
		if (jasperLoader == null)
			return serverLoader;
		// chain Jasper CL after the server class loader
		else {
			return new ChainedClassLoader(new ClassLoader[] { serverLoader, jasperLoader });
		}
	}

	/**
	 * Returns the defining classloader of the given class. This method will
	 * load the class through the given classloader but will return the class
	 * actual class loader (as in OSGi, a classloader can load classes using
	 * other loaders).
	 * 
	 * @param className class to load
	 * @param ClassLoader loader used to load the class (which might contain a
	 * needed import)
	 * @return class actual classloader - null if returned, if the class cannot
	 * be loaded
	 */
	private static ClassLoader findClassLoaderFor(String className, ClassLoader classLoader) {
		try {
			Class clazz = ClassUtils.forName(className, classLoader);
			return clazz.getClassLoader();
		}
		catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Creates an URLClassLoader for the given bundle running inside the server
	 * defined by the given class. This method might create a chained
	 * classloader suitable for loading server classes, javax.servlet as well as
	 * compiling JSPs if Jasper is present.
	 * 
	 * @param bundle bundle backing the webapp
	 * @param serverClass class defining the container running the webapp
	 * @return an URLClassLoader suitable for loading the web app classes.
	 */
	public static URLClassLoader createWebAppClassLoader(Bundle bundle, Class serverClass) {
		// create a chained classloader for server classes (like Jasper)
		ClassLoader serverClassLoader = chainedWebClassLoaders(serverClass);
		// hook that with the bundle
		ClassLoader classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle, serverClassLoader);
		// create classloader suitable for Jasper
		URLClassLoader urlClassLoader = JasperUtils.createJasperClassLoader(bundle, classLoader);
		return urlClassLoader;
	}

}
