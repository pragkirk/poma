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

package org.springframework.osgi.context;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.osgi.context.support.internal.OsgiBundleScope;

/**
 * Tests for OsgiBundleScope.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiBundleScopeTest extends TestCase {

	ObjectFactory objFactory;

	OsgiBundleScope scope;


	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		scope = new OsgiBundleScope();
		OsgiBundleScope.EXTERNAL_BUNDLE.set(null);
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		objFactory = null;
		scope.destroy();
		scope = null;
	}

	public void testLocalBeans() {
		ObjectFactory factory = new ObjectFactory() {

			public Object getObject() throws BeansException {
				return new Object();
			}

		};
		Object foo = scope.get("foo", factory);
		Object foo2 = scope.get("foo", factory);
		assertNotNull(foo);
		assertSame("instance not cached", foo, foo2);

		Object bar = scope.get("bar", factory);
		Object bar2 = scope.get("bar", factory);
		assertNotNull(bar);
		assertSame("instance not cached", bar, bar2);
	}

	public void testIsExternalBundleCalling() {
		assertFalse(OsgiBundleScope.EXTERNAL_BUNDLE.get() != null);
		OsgiBundleScope.EXTERNAL_BUNDLE.set(new Object());
		assertTrue(OsgiBundleScope.EXTERNAL_BUNDLE.get() != null);
	}

	public void testLocalDestructionCallback() {

		final Object[] callbackCalls = new Object[1];

		scope.registerDestructionCallback("foo", new Runnable() {

			public void run() {
				callbackCalls[0] = Boolean.TRUE;
			}
		});

		scope.destroy();
		assertSame(Boolean.TRUE, callbackCalls[0]);
	}

	public void testDestructionCallbackPassedAround() {
		OsgiBundleScope.EXTERNAL_BUNDLE.set(new Object());

		Runnable callback = new Runnable() {

			public void run() {
			}
		};

		scope.registerDestructionCallback("foo", callback);
		assertSame(callback, OsgiBundleScope.EXTERNAL_BUNDLE.get());
	}
}
