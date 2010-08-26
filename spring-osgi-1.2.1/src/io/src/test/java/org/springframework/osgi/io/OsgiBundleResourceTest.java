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

package org.springframework.osgi.io;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.Bundle;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.osgi.mock.ArrayEnumerator;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.util.StringUtils;

/**
 * @author Costin Leau
 * 
 */
public class OsgiBundleResourceTest extends TestCase {

	private OsgiBundleResource resource;

	private Bundle bundle;

	private String path;


	protected void setUp() throws Exception {
		path = OsgiBundleResourceTest.class.getName().replace('.', '/').concat(".class");
		bundle = new MockBundle();
		resource = new OsgiBundleResource(bundle, path);
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#hashCode()}.
	 */
	public void testHashCode() {
		assertEquals(path.hashCode(), resource.hashCode());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#OsgiBundleResource(org.osgi.framework.Bundle, java.lang.String)}.
	 */
	public void testOsgiBundleResource() {
		assertSame(bundle, resource.getBundle());
		assertEquals(path, resource.getPath());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#getPath()}.
	 */
	public void testGetPath() {
		assertEquals(path, resource.getPath());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#getBundle()}.
	 */
	public void testGetBundle() {
		assertSame(bundle, resource.getBundle());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#getInputStream()}.
	 */
	public void testGetInputStream() throws Exception {
		InputStream stream = resource.getInputStream();
		assertNotNull(stream);
		stream.close();
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#getURL()}.
	 */
	public void testGetURL() throws Exception {
		assertNotNull(resource.getURL());

		resource = new OsgiBundleResource(bundle, "osgibundle:foo" + path);
		try {
			resource.getURL();
			fail("should have thrown exception");
		}
		catch (Exception ex) {
			// expected
		}
	}

	public void testNonBundleUrlWhichExists() throws Exception {
		File tmp = File.createTempFile("foo", "bar");
		tmp.deleteOnExit();
		resource = new OsgiBundleResource(bundle, "file:" + tmp.toString());
		assertNotNull(resource.getURL());
		assertTrue(resource.exists());
		tmp.delete();
	}

	public void testNonBundleUrlWhichDoesNotExist() throws Exception {
		resource = new OsgiBundleResource(bundle, "file:foo123123");
		resource.getURL();
		assertFalse(resource.exists());
	}

	public void testFileWithSpecialCharsInTheNameBeingResolved() throws Exception {
		String name = "file:./target/test-classes/test-file";
		FileSystemResourceLoader fileLoader = new FileSystemResourceLoader();
		fileLoader.setClassLoader(getClass().getClassLoader());

		Resource fileRes = fileLoader.getResource(name);
		resource = new OsgiBundleResource(bundle, name);

		testFileVsOsgiFileResolution(fileRes, resource);
	}

	public void testFileWithEmptyCharsInTheNameBeingResolved() throws Exception {
		String name = "file:./target/test-classes/test file";
		FileSystemResourceLoader fileLoader = new FileSystemResourceLoader();
		fileLoader.setClassLoader(getClass().getClassLoader());

		Resource fileRes = fileLoader.getResource(name);
		resource = new OsgiBundleResource(bundle, name);

		testFileVsOsgiFileResolution(fileRes, resource);
	}

	public void testFileWithNormalCharsInTheNameBeingResolved() throws Exception {
		String name = "file:.project";
		FileSystemResourceLoader fileLoader = new FileSystemResourceLoader();
		fileLoader.setClassLoader(getClass().getClassLoader());

		Resource fileRes = fileLoader.getResource(name);

		resource = new OsgiBundleResource(bundle, name);
		testFileVsOsgiFileResolution(fileRes, resource);
	}

	private void testFileVsOsgiFileResolution(Resource fileRes, Resource otherRes) throws Exception {
		assertNotNull(fileRes.getURL());
		assertNotNull(fileRes.getFile());
		assertTrue(fileRes.getFile().exists());

		assertNotNull(otherRes.getURL());
		assertNotNull(otherRes.getFile());
		assertTrue(StringUtils.pathEquals(fileRes.getFile().getAbsolutePath(), otherRes.getFile().getAbsolutePath()));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#getResourceFromBundleSpace(java.lang.String)}.
	 */
	public void testGetResourceFromBundle() throws Exception {
		MockControl control = MockControl.createControl(Bundle.class);
		Bundle mock = (Bundle) control.getMock();

		String location = "foo";
		URL result = new URL("file:/" + location);

		control.expectAndReturn(mock.findEntries("/", "foo", false), new ArrayEnumerator(new URL[] { result }));
		control.replay();

		resource = new OsgiBundleResource(mock, location);

		assertEquals(result, resource.getResourceFromBundleSpace(location).getURL());
		control.verify();
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#getResourceFromBundleClasspath(java.lang.String)}.
	 */
	public void testGetResourceFromBundleClasspath() throws Exception {
		MockControl control = MockControl.createControl(Bundle.class);
		Bundle mock = (Bundle) control.getMock();

		String location = "file://foo";
		URL result = new URL(location);

		control.expectAndReturn(mock.getResource(location), result);
		control.replay();

		resource = new OsgiBundleResource(mock, location);

		assertSame(result, resource.getResourceFromBundleClasspath(location));
		control.verify();
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#isRelativePath(java.lang.String)}.
	 */
	public void testIsRelativePath() {
		assertTrue(resource.isRelativePath("foo"));
		assertFalse(resource.isRelativePath("/foo"));
		assertFalse(resource.isRelativePath(":foo"));
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#createRelative(java.lang.String)}.
	 */
	public void testCreateRelativeString() {
		String location = "foo";
		Resource res = resource.createRelative(location);
		assertSame(OsgiBundleResource.class, res.getClass());
		assertEquals("org/springframework/osgi/io/" + location, ((OsgiBundleResource) res).getPath());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#getFilename()}.
	 */
	public void testGetFilename() {
		assertNotNull(resource.getFilename());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#getDescription()}.
	 */
	public void testGetDescription() {
		assertNotNull(resource.getDescription());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResource#equals(java.lang.Object)}.
	 */
	public void testEqualsObject() {
		assertEquals(resource, new OsgiBundleResource(bundle, path));
		assertEquals(resource, resource);
		assertFalse(resource.equals(new OsgiBundleResource(bundle, "")));
		assertFalse(resource.equals(new OsgiBundleResource(new MockBundle(), path)));
	}

	public void testDefaultPathWithinContext() throws Exception {
		assertEquals(path, resource.getPathWithinContext());
	}

	public void testPathWithinBundleSpace() throws Exception {
		String contextPath = "folder/resource";
		resource = new OsgiBundleResource(bundle, "osgibundle:" + contextPath);
		assertEquals(contextPath, resource.getPathWithinContext());
	}

	public void testPathWithinClassSpace() throws Exception {
		String contextPath = "folder/resource";
		resource = new OsgiBundleResource(bundle, "classpath:" + contextPath);
		assertEquals(contextPath, resource.getPathWithinContext());
	}

	public void testPathWithinJarSpace() throws Exception {
		String contextPath = "folder/resource";
		resource = new OsgiBundleResource(bundle, "osgibundlejar:" + contextPath);
		assertEquals(contextPath, resource.getPathWithinContext());
	}

	public void testPathOutsideContext() throws Exception {
		String contextPath = "folder/resource";
		resource = new OsgiBundleResource(bundle, "file:" + contextPath);
		assertNull(resource.getPathWithinContext());
	}

	public void testLastModified() throws Exception {
		assertTrue("last modified should be non zero", resource.lastModified() > 0);
	}
	
	public void testNonExistingFile() throws Exception {
		resource = new OsgiBundleResource(bundle, "file:/some/non.existing.file");
		File file = resource.getFile();
		assertNotNull(file);
		assertFalse(file.exists());
	}
}