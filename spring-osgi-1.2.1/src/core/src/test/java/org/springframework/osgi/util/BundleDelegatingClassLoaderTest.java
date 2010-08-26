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
package org.springframework.osgi.util;

import java.net.URL;
import java.util.Enumeration;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.Bundle;
import org.springframework.aop.framework.ProxyFactory;

/**
 * @author Costin Leau
 * 
 */
public class BundleDelegatingClassLoaderTest extends TestCase {

	private BundleDelegatingClassLoader classLoader;

	private MockControl bundleCtrl;

	private Bundle bundle;

	protected void setUp() throws Exception {
		bundleCtrl = MockControl.createStrictControl(Bundle.class);
		bundle = (Bundle) bundleCtrl.getMock();
		classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle,
			ProxyFactory.class.getClassLoader());
		bundleCtrl.reset();
	}

	protected void tearDown() throws Exception {
		bundleCtrl.verify();
		classLoader = null;
		bundleCtrl = null;
		bundle = null;
	}

	public void tstEquals() {
		bundleCtrl.replay();

		assertFalse(classLoader.equals(new Object()));
		assertEquals(classLoader, classLoader);
		assertTrue(classLoader.equals(BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle,
			ProxyFactory.class.getClassLoader())));

		// assertEquals(bundle.hashCode(), clientClassLoader.hashCode());
	}

	public void testFindClass() throws Exception {
		String className = "foo.bar";
		String anotherClassName = "bar.foo";
		bundleCtrl.expectAndReturn(bundle.loadClass(className), Object.class);
		bundleCtrl.expectAndThrow(bundle.loadClass(anotherClassName), new ClassNotFoundException());
		bundleCtrl.expectAndReturn(bundle.getSymbolicName(), "Test Bundle Symbolic Name");
		bundleCtrl.replay();

		assertSame(Object.class, classLoader.findClass(className));

		try {
			classLoader.findClass(anotherClassName);
		}
		catch (ClassNotFoundException ex) {
			// expected
		}
	}

	public void testFindResource() throws Exception {
		String resource = "file://bla-bla";
		URL url = new URL(resource);

		bundleCtrl.expectAndReturn(bundle.getResource(resource), url);
		bundleCtrl.replay();

		assertSame(url, classLoader.findResource(resource));
	}

	public void testFindResources() throws Exception {
		String resource = "bla-bla";
		MockControl enumCtrl = MockControl.createStrictControl(Enumeration.class);
		Enumeration enumeration = (Enumeration) enumCtrl.getMock();

		bundleCtrl.expectAndReturn(bundle.getResources(resource), enumeration);
		bundleCtrl.replay();

		assertSame(enumeration, classLoader.findResources(resource));
	}

}
