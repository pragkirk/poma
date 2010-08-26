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

package org.springframework.osgi.iandt.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.test.platform.OsgiPlatform;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.FileCopyUtils;

/**
 * @author Costin Leau
 */
public class OSGI723Test extends BaseIoTest {

	private static boolean bundleInstalled = false;
	private static final String SLASH = "/";


	/**
	 * Add a bundle fragment.
	 */
	protected String[] getTestBundlesNames() {
		return new String[0];
	}

	protected String getManifestLocation() {
		return null;
	}

	protected void preProcessBundleContext(BundleContext platformBundleContext) throws Exception {
		super.preProcessBundleContext(platformBundleContext);
		if (!bundleInstalled) {
			logger.info("Installing OSGi-723 bundle...");
			InputStream stream = getClass().getResourceAsStream("/osgi-723.jar");
			Bundle bundle = platformBundleContext.installBundle("osgi-723", stream);
			bundle.start();
			bundleInstalled = true;
		}
	}

	public static void unpackBundle(Bundle bundle, File targetFolder) {
		// no need to use a recursive method since we get all resources directly
		Enumeration enm = bundle.findEntries(SLASH, null, true);
		while (enm != null && enm.hasMoreElements()) {

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
					File parent = targetFile.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					System.out.println(parent);

					OutputStream targetStream = new FileOutputStream(targetFile);
					System.err.println("Copying " + url + " to " + targetFile);
					FileCopyUtils.copy(url.openStream(), targetStream);
				}
				catch (IOException ex) {
					//
					System.err.println("Cannot copy resource " + entryPath + ex);
					throw (RuntimeException) new IllegalStateException("IO exception while unpacking bundle "
							+ OsgiStringUtils.nullSafeNameAndSymName(bundle)).initCause(ex);
				}
				// no need to close the streams - the utils already handles that
			}
		}
	}

	public void testSpecialBundle() throws Exception {
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, "issue.osgi-723");
		System.out.println(bundle);

		File tmpFile = File.createTempFile("tomcat-bla", ".osgi");
		tmpFile.delete();
		tmpFile.mkdir();

		unpackBundle(bundle, tmpFile);
	}

	protected OsgiPlatform createPlatform() {
		OsgiPlatform platform = super.createPlatform();
		platform.getConfigurationProperties().setProperty("felix.fragment.validation", "warning");
		return platform;
	}
}