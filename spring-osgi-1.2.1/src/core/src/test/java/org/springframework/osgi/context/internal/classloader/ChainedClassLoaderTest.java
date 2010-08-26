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

package org.springframework.osgi.context.internal.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.springframework.osgi.TestUtils;

/**
 * @author Costin Leau
 */
public class ChainedClassLoaderTest extends TestCase {

	private ChainedClassLoader chainedLoader;
	private ClassLoader emptyCL;


	protected void setUp() throws Exception {
		emptyCL = new URLClassLoader(new URL[0], null) {

			public Class loadClass(String name) throws ClassNotFoundException {
				throw new ClassNotFoundException(name);
			}

			public URL getResource(String name) {
				return null;
			}
		};

		chainedLoader = new ChainedClassLoader(new ClassLoader[] { emptyCL }, emptyCL);
	}

	protected void tearDown() throws Exception {
		chainedLoader = null;
		emptyCL = null;
	}

	public void testChainedClassLoaderClassLoaderArray() throws Exception {
		String className = "java.lang.Object";
		try {
			emptyCL.loadClass(className);
			fail("should not be able to load classes");
		}
		catch (ClassNotFoundException cnfe) {
			// expected
		}

		chainedLoader = new ChainedClassLoader(new ClassLoader[] { emptyCL });
		chainedLoader.loadClass(className);
	}

	public void testParentClassLoader() throws Exception {
		chainedLoader = new ChainedClassLoader(new ClassLoader[] { emptyCL });
		ClassLoader parent = chainedLoader.getParent();
		assertNotNull(parent);
		// fragile check (might fail on non SUN VMs)
		assertTrue("does the test run on a SUN VM or is it embedded?", parent.getClass().getName().indexOf("App") >= 0);
	}

	public void testChainedClassLoaderClassLoaderArrayClassLoader() throws Exception {
		String className = "java.lang.Object";

		try {
			emptyCL.loadClass(className);
			fail("should not be able to load classes");
		}
		catch (ClassNotFoundException cnfe) {
			// expected
		}

		try {
			chainedLoader.loadClass(className);
			fail("should not be able to load classes");
		}
		catch (ClassNotFoundException cnfe) {
			// expected
		}
	}

	public void testGetResourceString() throws Exception {
		System.out.println(chainedLoader.getResource("java/lang/Object.class"));
		assertNull(chainedLoader.getResource("java/lang/Object.class"));
		chainedLoader.addClassLoader(Object.class);
		assertNotNull(chainedLoader.getResource("java/lang/Object.class"));
	}

	public void testAddClassLoaderClass() throws Exception {
		chainedLoader.addClassLoader(Object.class);
		chainedLoader.loadClass("java.lang.Object");
	}

	public void testAddClassLoaderClassLoader() throws Exception {
		chainedLoader.addClassLoader(Bundle.class.getClassLoader());
		chainedLoader.loadClass("org.osgi.framework.Bundle");
	}

	public void testNonOSGiClassLoaderInsertOrder() throws Exception {
		ClassLoader appLoader = ClassLoader.getSystemClassLoader();
		ClassLoader extLoader = appLoader.getParent();

		chainedLoader.addClassLoader(extLoader);
		chainedLoader.addClassLoader(appLoader);

		// read the internal array
		List list = (List) TestUtils.getFieldValue(chainedLoader, "nonOsgiLoaders");

		// the loaders should be inserted based on their inheritance
		assertSame(appLoader, list.get(0));
		assertSame(extLoader, list.get(1));
	}
}