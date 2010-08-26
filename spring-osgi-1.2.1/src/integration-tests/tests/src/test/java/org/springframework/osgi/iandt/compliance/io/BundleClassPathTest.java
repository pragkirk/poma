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

package org.springframework.osgi.iandt.compliance.io;

import java.io.InputStream;
import java.lang.reflect.ReflectPermission;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.PropertyPermission;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundlePermission;
import org.osgi.framework.Constants;
import org.osgi.framework.PackagePermission;
import org.osgi.framework.ServicePermission;
import org.springframework.osgi.iandt.BaseIntegrationTest;

/**
 * IO compliance test for bundles containing Bundle ClassPath entries.
 * 
 * @author Costin Leau
 * 
 */
public class BundleClassPathTest extends BaseIntegrationTest {

	protected Manifest getManifest() {
		Manifest mf = super.getManifest();
		// add bundle classpath
		mf.getMainAttributes().putValue(Constants.BUNDLE_CLASSPATH,
			".,bundleclasspath/folder,bundleclasspath/simple.jar");
		return mf;
	}

	protected String[] getBundleContentPattern() {
		String pkg = getClass().getPackage().getName().replace('.', '/').concat("/");
		String[] patterns = new String[] { BaseIntegrationTest.class.getName().replace('.', '/').concat(".class"),
			pkg + "**/*", "bundleclasspath/**/*" };
		return patterns;
	}


	private Bundle bundle;
	private String classLocation;


	protected void onSetUp() throws Exception {
		bundle = bundleContext.getBundle();
		classLocation = BundleClassPathTest.class.getName().replace('.', '/') + ".class";
	}

	public void testGetResourceOnClassInsideBundle() throws Exception {
		assertNotNull(bundle.getResource(classLocation));
	}

	public void testGetResourceOnFileJustInsideBundle() throws Exception {
		assertNotNull(bundle.getResource("org/springframework/osgi/iandt/compliance/io/package.file"));
		assertNotNull(bundle.getResource("org/springframework/osgi/iandt/compliance/io/test.file"));
	}

	public void testGetResourceOnFileOnBundleClassPathAndBundleJar() throws Exception {
		assertNotNull(bundle.getResource("org/springframework/osgi/iandt/compliance/io/test.file"));
	}

	public void testGetResourceOnFileJustInsideFolderOnClassPath() throws Exception {
		assertNotNull(bundle.getResource("org/springframework/osgi/iandt/compliance/io/folder-test.file"));
	}

	public void testGetResourceOnFileJustInsideJarOnClassPath() throws Exception {
		assertNotNull(bundle.getResource("jar.file"));
	}

	public void testGetResourcesOnFilePresentMultipleTimesOnTheClassPathAndInsideTheBundle() throws Exception {
		System.out.println("running test" + this.getName());
		Enumeration enm = bundle.getResources("org/springframework/osgi/iandt/compliance/io/test.file");
		int count = 0;
		while (enm.hasMoreElements()) {
			Object object = (Object) enm.nextElement();
			System.out.println("found res " + object);
			count++;
		}
		assertEquals("not all resources are found", 3, count);
	}

	public void testFindEntriesOnFileJustInsideFolderOnClassPath() throws Exception {
		System.out.println("running test" + this.getName());
		Enumeration enm = bundle.findEntries("org/springframework/osgi/iandt/compliance/io/", "folder-test.file", false);
		assertNull("findEntries doesn't work on bundle classpath entries", enm);
	}

	public void testFindEntriesOnFileJustInsideJarOnClassPath() throws Exception {
		System.out.println("running test" + this.getName());
		Enumeration enm = bundle.findEntries("/", "jar.file", false);
		assertNull("findEntries doesn't work on bundle classpath entries", enm);
	}

	// disabled as it fails on the server for some reason (linux + equinox)
	// TODO: investigate
	public void tstFindEntriesOnFilePresentMultipleTimesOnTheClassPathAndInsideTheBundle() throws Exception {
		System.out.println("running test" + this.getName());
		Enumeration enm = bundle.findEntries("org/springframework/osgi/iandt/compliance/io/", "test.file", false);
		int count = 0;
		while (enm.hasMoreElements()) {
			count++;
		}
		assertEquals("bundle only resources are found", 1, count);
	}

	public void testGetEntryOnFileJustInsideFolderOnClassPath() throws Exception {
		System.out.println("running test" + this.getName());
		URL url = bundle.getEntry("org/springframework/osgi/iandt/compliance/io/folder-test.file");
		assertNull("findEntries doesn't work on bundle classpath entries", url);
	}

	public void testGetEntryOnFileJustInsideJarOnClassPath() throws Exception {
		System.out.println("running test" + this.getName());
		URL url = bundle.getEntry("jar.file");
		assertNull("findEntries doesn't work on bundle classpath entries", url);
	}

	// fails on Felix + KF
	public void tstFindEntriesOnMetaInfEntryOnSystemBundle() throws Exception {
		Bundle sysBundle = bundleContext.getBundle(0);
		Enumeration enm = sysBundle.findEntries("/", "META-INF", false);
		assertNotNull("system bundle doesn't return META-INF", enm);
	}

	// fails on Felix + KF
	public void tstGetEntryOnMetaInfEntryOnSystemBundle() throws Exception {
		Bundle sysBundle = bundleContext.getBundle(0);
		URL url = sysBundle.getEntry("/META-INF");
		assertNotNull("system bundle doesn't consider META-INF on classpath", url);
	}

	// simple debugging test (no need to keep it running)
	public void tstConnectionToJarOnClassPath() throws Exception {
		URL url = bundle.getEntry("bundleclasspath/simple.jar");
		System.out.println("jar url is " + url);
		URLConnection con = url.openConnection();
		System.out.println(con);
		System.out.println(con.getContentType());
		InputStream stream = con.getInputStream();
		JarInputStream jis = new JarInputStream(stream);
		System.out.println(jis);
		System.out.println(jis.available());
		System.out.println(jis.getNextJarEntry());
		System.out.println(jis.getNextJarEntry());
		System.out.println(jis.getNextJarEntry());
		System.out.println(jis.available());
		System.out.println(jis.getNextJarEntry());
		System.out.println(jis.available());
		jis.close();
	}
}
