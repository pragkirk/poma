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

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.osgi.io.OsgiBundleResourcePatternResolver;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.util.ObjectUtils;

/**
 * Test wildcard matching on bundles with a defined bundle classpath. This is
 * one of the heaviest IO tests as it involves both a bundle classpath and
 * fragments.
 * 
 * @author Costin Leau
 * 
 */
public class BundleClassPathWildcardTest extends BaseIoTest {

	private static boolean noRootCPTestBundleInstalled = false;
	private static final String NO_ROOT_BND_SYM = "org.springframework.bundle.osgi.io.test.no.root.classpath";


	protected Manifest getManifest() {
		Manifest mf = super.getManifest();
		// add bundle classpath
		mf.getMainAttributes().putValue(Constants.BUNDLE_CLASSPATH,
			".,bundleclasspath/folder/,bundleclasspath/simple.jar,foo");
		return mf;
	}

	protected void preProcessBundleContext(BundleContext platformBundleContext) throws Exception {
		super.preProcessBundleContext(platformBundleContext);
		if (!noRootCPTestBundleInstalled) {
			logger.info("Installing no root cp bundle...");
			InputStream stream = getClass().getResourceAsStream("/norootcpbundle.jar");
			Bundle bundle = platformBundleContext.installBundle("norootcpbundle", stream);
			bundle.start();
			noRootCPTestBundleInstalled = true;
		}
	}

	protected String[] getBundleContentPattern() {
		return (String[]) ObjectUtils.addObjectToArray(super.getBundleContentPattern(), "bundleclasspath/**/*");
	}

	public void testClassPathFilesOnBundleClassPath() throws Exception {
		// use org to make sure the bundle class is properly considered (especially for folder based classpath)
		Resource[] res = patternLoader.getResources("classpath:org/**/*.file");
		System.out.println("array count is " + res.length);
		System.out.println(ObjectUtils.nullSafeToString(res));
		printPathWithinContext(res);
		assertTrue("bundle classpath jar not considered", containsString(res, "jar-folder.file"));
	}

	public void testAllClassPathFilesOnBundleClassPath() throws Exception {
		// use org to make sure the bundle class is properly considered (especially for folder based classpath)
		Resource[] res = patternLoader.getResources("classpath*:org/**/*.file");
		System.out.println("array count is " + res.length);
		System.out.println(ObjectUtils.nullSafeToString(res));
		printPathWithinContext(res);
		assertTrue("bundle classpath jar not considered", containsString(res, "jar-folder.file"));
	}

	public void testRootFileOnBundleClassPath() throws Exception {
		// use org to make sure the bundle class is properly considered (especially for folder based classpath)
		Resource[] res = patternLoader.getResources("classpath:*.file");
		System.out.println("array count is " + res.length);
		System.out.println(ObjectUtils.nullSafeToString(res));
		printPathWithinContext(res);
		assertTrue("bundle classpath jar not considered", containsString(res, "jar.file"));
	}

	public void testRootFileOnAllBundleClassPath() throws Exception {
		// use org to make sure the bundle class is properly considered (especially for folder based classpath)
		Resource[] res = patternLoader.getResources("classpath:*.file");
		System.out.println("array count is " + res.length);

		System.out.println(ObjectUtils.nullSafeToString(res));
		printPathWithinContext(res);
		assertTrue("bundle classpath jar not considered", containsString(res, "jar.file"));
	}

	private boolean containsString(Resource[] array, String str) throws IOException {
		for (int i = 0; i < array.length; i++) {
			Resource resource = array[i];
			if (resource.getURL().toExternalForm().indexOf(str) > -1)
				return true;
		}
		return false;
	}

	public void testURLConnectionToJarInsideBundle() throws Exception {
		Resource jar = patternLoader.getResource("bundleclasspath/simple.jar");
		testJarConnectionOn(jar);
	}

	private void testJarConnectionOn(Resource jar) throws Exception {
		String toString = jar.getURL().toExternalForm();
		// force JarURLConnection
		String urlString = "jar:" + toString + "!/";
		URL newURL = new URL(urlString);
		System.out.println(newURL);
		System.out.println(newURL.toExternalForm());
		URLConnection con = newURL.openConnection();
		System.out.println(con);
		System.out.println(con instanceof JarURLConnection);
		JarURLConnection jarCon = (JarURLConnection) con;

		JarFile jarFile = jarCon.getJarFile();
		System.out.println(jarFile.getName());
		Enumeration enm = jarFile.entries();
		while (enm.hasMoreElements())
			System.out.println(enm.nextElement());
	}

	public void testResourceAvailableOnlyInsideJarClasspath() throws Exception {
		Resource[] resources = patternLoader.getResources("classpath*:jar.file");
		assertNotNull(resources);
		System.out.println("Arrays inside the jar");
		printPathWithinContext(resources);
		assertEquals(1, resources.length);
		assertTrue(resources[0].exists());
	}

	public void testResourceAvailableOnlyInsideFolderClasspath() throws Exception {
		Resource[] resources = patternLoader.getResources("classpath*:org/springframework/osgi/iandt/compliance/io/folder-test.file");
		assertNotNull(resources);
		assertEquals(1, resources.length);
		assertTrue(resources[0].exists());
		System.out.println("Arrays inside the classpath folder");
		System.out.println(ObjectUtils.nullSafeToString((resources)));
		printPathWithinContext(resources);
	}

	public void testResourceAvailableWithPatternOnPathsOnlyInsideFolderClasspath() throws Exception {
		Resource[] resources = patternLoader.getResources("classpath*:org/springframework/osgi/iandt/**/folder-test.file");
		assertNotNull(resources);
		assertEquals(1, resources.length);
		assertTrue(resources[0].exists());
		System.out.println(ObjectUtils.nullSafeToString((resources)));
		printPathWithinContext(resources);
	}

	public void testResourceAvailableWithPatternOnlyInsideFolderClasspath() throws Exception {
		Resource[] resources = patternLoader.getResources("classpath:org/springframework/osgi/iandt/**/folder-test.file");
		assertNotNull(resources);
		assertEquals(1, resources.length);
		assertTrue(resources[0].exists());
		System.out.println(ObjectUtils.nullSafeToString((resources)));
		printPathWithinContext(resources);
	}

	private ResourcePatternResolver getNoRootCpBundleResourceResolver() {
		Bundle bnd = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, NO_ROOT_BND_SYM);
		assertNotNull("noRootClassPath bundle was not found", bnd);
		return new OsgiBundleResourcePatternResolver(bnd);
	}

	public void testNoRootCpBundleResourceInRootNotFound() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath:root.file");
		// since there is no pattern matching, the loader will return a non-existing resource
		assertFalse("resource should not be found since / is not in the classpath", res[0].exists());
		System.out.println("classpath:root.file resources");
		printPathWithinContext(res);
	}

	public void testNoRootCpBundleResourceInRootNotFoundOnAllClasspaths() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath*:root.file");
		assertTrue("resource should not be found since / is not in the classpath", ObjectUtils.isEmpty(res));
		System.out.println("root.file resources");
		System.out.println("classpath*:root.file resources");
		printPathWithinContext(res);
	}

	public void testNoRootCpBundleResourceInClassPathFound() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath*:cp.file");
		assertFalse("resource should be found since it's on the classpath", ObjectUtils.isEmpty(res));
		assertTrue("resource should be found since it's on the classpath", res[0].exists());
		System.out.println("classpath*:cp.file resources");
		printPathWithinContext(res);
	}

	public void testNoRootCpBundleResourceNestedInClassPathFound() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath*:/some/nested/nested.file");
		assertFalse("resource should be found since it's on the classpath", ObjectUtils.isEmpty(res));
		assertTrue("resource should be found since it's on the classpath", res[0].exists());
		System.out.println("classpath*:/some/nested/nested.file resources");
		printPathWithinContext(res);
	}

	public void testNoRootCpBundleResourceNestedInPkgInClassPathFound() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath*:/some/nested/pkg/pkg.file");
		assertFalse("resource should be found since it's on the classpath", ObjectUtils.isEmpty(res));
		assertTrue("resource should be found since it's on the classpath", res[0].exists());
	}

	public void testNoRootCpBundleResourcePatternMatching() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath:/**/*.file");
		assertEquals("incorrect number of resources found", 3, res.length);
	}

	public void testNoRootCpBundleResourceMultipleRoots() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath*:/**/*.file");
		assertEquals("incorrect number of resources found", 3, res.length);
	}

	public void testNoRootCpBundleResourcePatternMatchingWithSpecifiedFolder() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath:/some/**/*.file");
		assertEquals("incorrect number of resources found", 2, res.length);
	}

	public void testNoRootCpBundleResourceMultipleRootsSpecifiedFolder() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath*:/some/**/*.file");
		assertEquals("incorrect number of resources found", 2, res.length);
	}

	protected List getTestPermissions() {
		List list = super.getTestPermissions();
		list.add(new AdminPermission("(name=" + NO_ROOT_BND_SYM + ")", AdminPermission.METADATA));
		list.add(new AdminPermission("(name=" + NO_ROOT_BND_SYM + ")", AdminPermission.RESOURCE));
		return list;
	}
}
