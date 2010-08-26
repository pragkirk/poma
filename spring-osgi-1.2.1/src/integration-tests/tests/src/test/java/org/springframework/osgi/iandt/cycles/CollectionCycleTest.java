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

package org.springframework.osgi.iandt.cycles;

import java.util.Collection;

/**
 * Integration test for checking cyclic injection between an importer and its
 * listeners.
 * 
 * @author Costin Leau
 */
public class CollectionCycleTest extends BaseImporterCycleTest {

	private Collection importer;


	protected String[] getConfigLocations() {
		return new String[] { "/org/springframework/osgi/iandt/cycles/top-level-collection-importer.xml" };
	}

	protected void onSetUp() throws Exception {
		super.onSetUp();
		importer = (Collection) applicationContext.getBean("importer");
	}

	public void testListenerA() throws Exception {
		assertEquals(importer.toString(), listenerA.getTarget().toString());
	}

	public void testListenerB() throws Exception {
		assertEquals(importer.toString(), listenerB.getTarget().toString());
	}

	public void testListenersBetweenThem() throws Exception {
		assertSame(listenerA.getTarget(), listenerB.getTarget());
	}

}
