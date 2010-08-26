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

package org.springframework.osgi.compendium.internal.cm;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.springframework.osgi.util.internal.MapBasedDictionary;

import junit.framework.TestCase;

/**
 * @author Costin Leau
 * 
 */
public class UpdateMethodAdapterTest extends TestCase {

	public class NoMethod {

	}

	public static class OneMapMethod {

		public static int INVOCATIONS = 0;


		public void update(Map properties) {
			INVOCATIONS++;
		}
	}

	public static class OneDictionaryMethod {

		public static int INVOCATIONS = 0;


		public void dictMethod(Dictionary properties) {
			INVOCATIONS++;
		}
	}

	public static class BothMethods {

		public static int INVOCATIONS = 0;


		public void update(Dictionary prop) {
			INVOCATIONS++;
		}

		public void update(Map properties) {
			INVOCATIONS++;
		}
	}

	public class NonPublicMethod {

		void update(Map props) {
		}
	}


	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testDetermineUpdateMethodWNoMethod() {
		assertTrue(UpdateMethodAdapter.determineUpdateMethod(NoMethod.class, "update").isEmpty());
	}

	public void testDetermineUpdateMethodWMapMethod() {
		assertEquals(1, UpdateMethodAdapter.determineUpdateMethod(OneMapMethod.class, "update").size());
	}

	public void testDetermineUpdateMethodWDictMethod() {
		assertEquals(1, UpdateMethodAdapter.determineUpdateMethod(OneDictionaryMethod.class, "dictMethod").size());
	}

	public void testDetermineUpdateMethodWBothMethod() {
		assertEquals(2, UpdateMethodAdapter.determineUpdateMethod(BothMethods.class, "update").size());
	}

	public void testDetermineUpdateMethodWNonPublicMethod() {
		assertTrue(UpdateMethodAdapter.determineUpdateMethod(NonPublicMethod.class, "update").isEmpty());
	}

	public void testInvokeCustomMethodsOnMapMethod() {
		OneMapMethod.INVOCATIONS = 0;
		Map methods = UpdateMethodAdapter.determineUpdateMethod(OneMapMethod.class, "update");
		UpdateMethodAdapter.invokeCustomMethods(new OneMapMethod(), methods, new HashMap());
		assertEquals(1, OneMapMethod.INVOCATIONS);
	}

	public void testInvokeCustomMethodsOnDictMethod() {
		OneDictionaryMethod.INVOCATIONS = 0;
		Map methods = UpdateMethodAdapter.determineUpdateMethod(OneDictionaryMethod.class, "dictMethod");
		UpdateMethodAdapter.invokeCustomMethods(new OneDictionaryMethod(), methods, new MapBasedDictionary());
		assertEquals(1, OneDictionaryMethod.INVOCATIONS);
	}

	public void testInvokeCustomMethodsOnBothMethod() {
		BothMethods.INVOCATIONS = 0;
		Map methods = UpdateMethodAdapter.determineUpdateMethod(BothMethods.class, "update");
		UpdateMethodAdapter.invokeCustomMethods(new BothMethods(), methods, new MapBasedDictionary());
		assertEquals(2, BothMethods.INVOCATIONS);
	}
}
