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
package org.springframework.osgi;

import java.lang.ref.Reference;

import junit.framework.Assert;

/**
 * Utility class providing methods related to 'Garbage Collector' and
 * WeakReferences.
 * Normally used inside JUnit test cases.
 * 
 * @author Costin Leau
 * 
 */
public abstract class GCTests {

	/**
	 * Number of iterators while calling the GC.
	 */
	public static int GC_ITERATIONS = 30;

	public static void assertGCed(Reference reference) {
		assertGCed("given object was not reclaimed", reference);
	}

	/**
	 * Assert that the given object reference has been reclaimed. This assertion
	 * is useful for determing if there are hard references to the given object.
	 * 
	 * @param message
	 * @param reference
	 */
	public static void assertGCed(String message, Reference reference) {
		int garbageSize = 300;

		for (int i = 0; i < GC_ITERATIONS; i++) {
			if (reference.get() == null) {
				return;
			}
			else {
				// add garbage
				byte[] garbage = new byte[garbageSize];
				garbageSize = garbageSize << 1;

				// trigger the GC
				System.gc();
				System.runFinalization();
			}
		}

		Assert.fail(message);
	}
}
