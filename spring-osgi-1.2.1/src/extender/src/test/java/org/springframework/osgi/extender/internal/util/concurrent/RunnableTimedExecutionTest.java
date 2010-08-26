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

package org.springframework.osgi.extender.internal.util.concurrent;

import junit.framework.TestCase;

/**
 * 
 * @author Costin Leau
 * 
 */
public class RunnableTimedExecutionTest extends TestCase {

	private long wait = 5 * 1000;


	public void testExecute() {
		RunnableTimedExecution.execute(new Runnable() {

			public void run() {
				assertTrue(true);
			}

		}, wait);
	}

	public void testDestroy() {
		RunnableTimedExecution.execute(new Runnable() {

			public void run() {
				try {
					Thread.sleep(wait * 5);
					fail("should have been interrupted");
				}
				catch (InterruptedException ie) {
					// expected
				}

			}

		}, 10);
	}
}
