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
package org.springframework.osgi.test.internal.support;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.springframework.osgi.test.AbstractOsgiTests;
import org.springframework.osgi.test.internal.OsgiJUnitTest;
import org.springframework.util.Assert;

/**
 * Reflection-based adapter for {@link OsgiJUnitTest} used for wrapping
 * {@link AbstractOsgiTests} & co. with {@link OsgiJUnitTest} interface without
 * exposing the latter interface (which is internal and might be modified in the
 * future).
 * 
 * @author Costin Leau
 */
public class OsgiJUnitTestAdapter implements OsgiJUnitTest {

	private final TestCase target;

	private final Method injectBundleContext, runTest, setUp, tearDown;

	public OsgiJUnitTestAdapter(TestCase target) {
		Assert.notNull(target, "the adapter can be used only with a non-null test");

		this.target = target;

		try {

			// determine methods
			injectBundleContext = org.springframework.util.ReflectionUtils.findMethod(target.getClass(),
				"injectBundleContext", new Class[] { BundleContext.class });
			org.springframework.util.ReflectionUtils.makeAccessible(injectBundleContext);

			runTest = org.springframework.util.ReflectionUtils.findMethod(target.getClass(), "osgiRunTest");
			org.springframework.util.ReflectionUtils.makeAccessible(runTest);

			setUp = org.springframework.util.ReflectionUtils.findMethod(target.getClass(), "osgiSetUp");
			org.springframework.util.ReflectionUtils.makeAccessible(setUp);

			tearDown = org.springframework.util.ReflectionUtils.findMethod(target.getClass(), "osgiTearDown");
			org.springframework.util.ReflectionUtils.makeAccessible(tearDown);

		}
		catch (Exception ex) {
			throw new RuntimeException(
					"cannot determine JUnit hooks; is this test extending Spring-DM test framework?", ex);
		}

	}

	public void injectBundleContext(BundleContext bundleContext) {
		org.springframework.util.ReflectionUtils.invokeMethod(injectBundleContext, target,
			new Object[] { bundleContext });
	}

	public void osgiRunTest() throws Throwable {
		org.springframework.util.ReflectionUtils.invokeMethod(runTest, target);
	}

	public void osgiSetUp() throws Exception {
		org.springframework.util.ReflectionUtils.invokeMethod(setUp, target);
	}

	public void osgiTearDown() throws Exception {
		org.springframework.util.ReflectionUtils.invokeMethod(tearDown, target);
	}

	public TestCase getTestCase() {
		return target;
	}

}
