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
package org.springframework.osgi.mock;

import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * 
 * @author Costin Leau
 * 
 */
public class EntryLookupControllingMockBundleTest extends TestCase {

	private EntryLookupControllingMockBundle bundle;

	protected void setUp() throws Exception {
		bundle = new EntryLookupControllingMockBundle(new Properties());
	}

	protected void tearDown() throws Exception {
		bundle = null;
	}

	public void testGetEntry() throws Exception {
		URL url = new URL("http://bo/ho");
		bundle.setEntryReturnOnNextCallToGetEntry(url);
		assertSame(url, bundle.getEntry("bla"));
	}

	public void testFindEntries() throws Exception {
		String[] source = new String[] {"A"};
		bundle.setResultsToReturnOnNextCallToFindEntries(source);
		Enumeration enm = bundle.findEntries(null, null, false);
		assertNotNull(enm);
		assertTrue(enm.hasMoreElements());
		assertEquals("A", enm.nextElement());
	}
}
