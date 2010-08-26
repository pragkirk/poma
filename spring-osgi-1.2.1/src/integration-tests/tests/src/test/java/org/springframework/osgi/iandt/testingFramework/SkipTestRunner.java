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

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.springframework.test.ConditionalTestCase;

/**
 * @author Costin Leau
 * 
 */
public class SkipTestRunner extends TestCase {

	private TestCase test;

	private TestResult result;


	protected void setUp() throws Exception {
		test = new SkipTestsTst();
		result = new TestResult();
	}

	public void testSkippedTestProperlyRecorded() throws Exception {
		executeTest(SkipTestsTst.TEST_SKIPPED_1);
		executeTest(SkipTestsTst.TEST_RAN);
		executeTest(SkipTestsTst.TEST_SKIPPED_2);

		// tests are being ran as far as JUnit is concerned
		assertEquals("tests are being not being ran by JUnit", 3, result.runCount());
		assertEquals("skipped tests not properly recorded", 2, ConditionalTestCase.getDisabledTestCount());
	}

	private void executeTest(String testMethod) {
		test.setName(testMethod);
		test.run(result);
	}
}
