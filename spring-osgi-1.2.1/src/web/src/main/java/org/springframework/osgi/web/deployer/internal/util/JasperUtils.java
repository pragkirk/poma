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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.osgi.io.OsgiBundleResourcePatternResolver;
import org.springframework.osgi.io.internal.resolver.DependencyResolver;
import org.springframework.osgi.io.internal.resolver.ImportedBundle;
import org.springframework.osgi.io.internal.resolver.PackageAdminResolver;
import org.springframework.osgi.test.internal.util.jar.JarUtils;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Utility class dedicated to Tomcat Jasper.
 * 
 * @author Costin Leau
 * 
 */
public abstract class JasperUtils {

	private static final String TLD_EXT = ".tld";

	private static final String TLD_PATTERN = "*" + TLD_EXT;

	private static final String META_INF = "META-INF";

	/** Jasper class */
	// org.apache.jasper.JspC
	static final String JASPER_CLASS = "org.apache.jasper.servlet.JspServlet";

	/** logger */
	private static final Log log = LogFactory.getLog(JasperUtils.class);


	/**
	 * Returns an array of Resources pointing to the tag definitions found
	 * inside the bundle classpath.
	 * 
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	private static Resource[] getBundleTagLibs(Bundle bundle) throws IOException {
		ResourcePatternResolver resolver = new OsgiBundleResourcePatternResolver(bundle);
		return resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + META_INF + "/**/" + TLD_PATTERN);
	}

	/**
	 * Returns a list of Resources pointing to taglibs found under META-INF/ in
	 * the imported bundles.
	 * 
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	private static Resource[] getImportedBundlesTagLibs(BundleContext context, Bundle bundle) {
		DependencyResolver resolver = new PackageAdminResolver(context);

		ImportedBundle[] importedBundles = resolver.getImportedBundles(bundle);

		List list = new ArrayList(8);

		for (int i = 0; i < importedBundles.length; i++) {
			Bundle importedBundle = importedBundles[i].getBundle();
			// search the bundle space
			Enumeration enm = importedBundle.findEntries(META_INF, TLD_PATTERN, true);
			while (enm != null && enm.hasMoreElements()) {
				URL entry = (URL) enm.nextElement();
				list.add(new UrlResource(entry));
			}
		}

		return (Resource[]) list.toArray(new Resource[list.size()]);
	}

	/**
	 * Creates a temporary jar using the given resources.
	 * 
	 * @param resource
	 * @return
	 */
	private static URL createTaglibJar(Resource[] resources, Manifest mf) throws IOException {
		File tempJar = File.createTempFile("spring.dm.tld.", ".jar");
		tempJar.deleteOnExit();
		OutputStream fos = new FileOutputStream(tempJar);

		Map entries = new LinkedHashMap();
		for (int i = 0; i < resources.length; i++) {
			Resource resource = resources[i];
			String name = URLDecoder.decode(resource.getURL().getPath(), "UTF8");
			entries.put(name, resource);
		}
		JarUtils.createJar(mf, entries, fos);

		URL jarURL = tempJar.toURL();
		if (log.isTraceEnabled()) {
			StringBuffer buf = new StringBuffer();
			buf.append("\n");
			for (Iterator iterator = entries.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				buf.append(entry.getKey());
				buf.append("\t\t");
				buf.append(entry.getValue());
				buf.append("\n");
			}

			log.trace("Created TLD jar at " + tempJar.toURL() + " containing " + buf);
		}

		return jarURL;
	}

	public static URL[] createTaglibClasspathJars(Bundle bundle) {
		List urls = new ArrayList(2);
		boolean trace = log.isTraceEnabled();

		try {
			// create taglib jar for tlds inside the bundle classpath
			Resource[] res = getBundleTagLibs(bundle);
			if (!ObjectUtils.isEmpty(res)) {
				urls.add(createTaglibJar(res, null));
			}
			if (trace)
				log.trace("Bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle)
						+ " has the following tlds in its classpath " + ObjectUtils.nullSafeToString(res));

			// create taglib jar for tlds from imported bundles
			BundleContext ctx = OsgiBundleUtils.getBundleContext(bundle);
			Resource[] importedTLDs = getImportedBundlesTagLibs(ctx, bundle);
			if (!ObjectUtils.isEmpty(importedTLDs)) {
				urls.add(createTaglibJar(importedTLDs, null));
			}

			if (trace)
				log.trace("Bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle)
						+ " has the following tlds in its imported bundles "
						+ ObjectUtils.nullSafeToString(importedTLDs));

			return (URL[]) urls.toArray(new URL[urls.size()]);
		}
		catch (IOException ex) {
			throw (RuntimeException) new IllegalStateException("Cannot create taglib jars").initCause(ex);
		}
	}

	/**
	 * Creates an URLClassLoader that wraps the given class loader meaning that
	 * all its calls will be delegated to the backing class loader. However, the
	 * bundle context will be used inside {@link URLClassLoader#getURLs()} so
	 * that Tomcat Jasper detects the taglibs available inside the given bundle
	 * and its imports.
	 * 
	 * <p/> To avoid unneeded lookups, the method will check for the presence of
	 * Jasper compiler. If it's not found, then no taglibs will be searched.
	 * 
	 * @param bundle OSGi backing bundle
	 * @param unpackLocation location where the bundle is unpacked
	 * @param parent parent class loader
	 * 
	 * @return Tomcat Jasper 2 suited URLClassLoader wrapper around the given
	 * class loader.
	 */
	public static URLClassLoader createJasperClassLoader(Bundle bundle, ClassLoader parent) {
		// search for Jasper

		boolean jasperPresent = false;
		try
		// first search the bundle 
		{
			bundle.loadClass(JASPER_CLASS);
			jasperPresent = true;
		}
		catch (ClassNotFoundException cnfe) {
			//followed by the parent classloader
			jasperPresent = ClassUtils.isPresent(JASPER_CLASS, parent);
		}

		URL[] tldJars = null;
		if (jasperPresent) {
			if (log.isDebugEnabled())
				log.debug("Jasper present in bundle " + OsgiStringUtils.nullSafeSymbolicName(bundle)
						+ "; looking for taglibs...");
			tldJars = createTaglibClasspathJars(bundle);
		}
		else {
			if (log.isDebugEnabled())
				log.debug("Jasper not present in bundle " + OsgiStringUtils.nullSafeSymbolicName(bundle)
						+ "; ignoring taglibs...");

			tldJars = new URL[0];
		}

		return URLClassLoader.newInstance(tldJars, parent);
	}
}
