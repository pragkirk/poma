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

import java.net.URL;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.Bundle;
import org.springframework.core.io.Resource;
import org.springframework.osgi.mock.ArrayEnumerator;

/**
 * @author Costin Leau
 * 
 */
public class OsgiBundleResourceLoaderTest extends TestCase {

	OsgiBundleResourceLoader loader;

	MockControl control;

	Bundle bundle;

	protected void setUp() throws Exception {
		control = MockControl.createStrictControl(Bundle.class);
		bundle = (Bundle) control.getMock();
		loader = new OsgiBundleResourceLoader(bundle);
	}

	protected void tearDown() throws Exception {
		loader = null;
		bundle = null;
		control = null;
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.io.OsgiBundleResourceLoader#getResource(java.lang.String)}.
	 */
	public void testGetClasspathResource() throws Exception {
		String res = "foo.txt";
		URL expected = new URL("file://" + res);
		control.expectAndReturn(bundle.getResource(res), expected);
		control.replay();

		Resource resource = loader.getResource("classpath:" + res);
		assertNotNull(resource);
		assertSame(expected, resource.getURL());
		control.verify();
	}

	public void testGetBundleResource() throws Exception {
		String res = "foo.txt";
		URL url = new URL("file:/" + res);
		control.expectAndReturn(bundle.findEntries("/", res, false), new ArrayEnumerator(new URL[] {url}));
		control.replay();

		Resource resource = loader.getResource("osgibundle:/" + res);
		assertNotNull(resource);
		assertSame(url, resource.getURL());
		control.verify();
	}

	public void testGetRelativeResource() throws Exception {
		String res = "foo.txt";
		URL expected = new URL("file:/" + res);
		control.replay();

		Resource resource = loader.getResource("file:/" + res);
		assertNotNull(resource);
		assertEquals(expected, resource.getURL());
		control.verify();
	}

	public void testGetFallbackResource() throws Exception {
		String res = "foo.txt";
		URL expected = new URL("http:/" + res);
		control.replay();

		Resource resource = loader.getResource("http:/" + res);
		assertNotNull(resource);
		assertEquals(expected, resource.getURL());
		control.verify();
	}

	public void testGetResourceByPath() throws Exception {
		try {
			loader.getResourceByPath(null);
			fail("should have thrown exception");
		}
		catch (Exception ex) {
			// expected
		}
		String path = "foo";
		Resource res = loader.getResourceByPath(path);
		assertNotNull(res);
		assertSame(OsgiBundleResource.class, res.getClass());
		assertEquals(path, ((OsgiBundleResource) res).getPath());
	}
}
