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

package org.springframework.osgi.iandt.testingFramework;

import org.springframework.osgi.iandt.BaseIntegrationTest;

/**
 * 
 * @author Costin Leau
 * 
 */
public class SkipTestsTst extends BaseIntegrationTest {

	static final String TEST_SKIPPED_1 = "testFirstSkipped";
	static final String TEST_SKIPPED_2 = "testSecondSkipped";
	static final String TEST_RAN = "testActuallyRan";


	public void testFirstSkipped() throws Exception {
		fail("test should be skipped");
	}

	public void testActuallyRan() throws Exception {
	}

	public void testSecondSkipped() throws Exception {
		fail("test should be skipped");
	}

	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return testMethodName.endsWith("Skipped");
	}

}
