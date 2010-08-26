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
package org.springframework.osgi.internal.service.collection.threading;

import edu.umd.cs.mtc.MultithreadedTest;
import edu.umd.cs.mtc.TestFramework;

/**
 * @author Costin Leau
 * 
 */
public abstract class BaseThreadingTest extends MultithreadedTest {

	static int RUN_TIMES = 3;

	public void runTest() throws Throwable {
		TestFramework.runManyTimes(this, RUN_TIMES);
	}

	public void testRun() throws Throwable {
		// do nothing - just to comply with the test suite
	}

}
