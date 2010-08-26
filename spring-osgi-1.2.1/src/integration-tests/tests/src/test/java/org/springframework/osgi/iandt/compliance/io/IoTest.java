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

package org.springframework.osgi.iandt.compliance.io;

import java.net.URL;
import java.util.Enumeration;

import org.springframework.osgi.iandt.io.BaseIoTest;
import org.springframework.util.ObjectUtils;

/**
 * Low level access API used for discovering the underlying platform
 * capabilities since there are subtle yet major differences between each
 * implementation.
 * 
 * @author Costin Leau
 * 
 */
public class IoTest extends BaseIoTest {

	protected String[] getBundleContentPattern() {
		return super.getBundleContentPattern();
	}

	// don't use any extra bundles - just the test jar
	protected String[] getTestBundlesNames() {
		return null;
	}

	public void testGetResourceOnMetaInf() throws Exception {
		URL url = bundle.getResource("/META-INF");
		System.out.println(url);
		assertNotNull(url);
	}

	// fails on Felix 1.0.1 (fixed in 1.0.3 and KF 2.0.3)
	public void testGetResourceOnRoot() throws Exception {
		URL url = bundle.getResource("/");
		System.out.println("getResource('/') = " + url);
		assertNotNull("root folder not validated " + url);
	}

	// fails on Felix 1.0.1 (fixed in 1.0.3 and KF 2.0.3)
	public void testGetResourceSOnRoot() throws Exception {
		Enumeration enm = bundle.getResources("/");
		Object[] res = copyEnumeration(enm);
		System.out.println("getResources('/') = " + ObjectUtils.nullSafeToString(res));
		assertEquals("root folder not validated" + ObjectUtils.nullSafeToString(res), 1, res.length);
	}

	public void testFindEntriesOnFolders() throws Exception {
		Enumeration enm = bundle.findEntries("/", null, false);
		// should get 3 entries - META-INF/, org/ and log4j.properties

		Object[] res = copyEnumeration(enm);
		assertEquals("folders ignored; found " + ObjectUtils.nullSafeToString(res), 2, res.length);
	}

	public void testFindEntriesOnSubFolders() throws Exception {
		Enumeration enm = bundle.findEntries("/META-INF", null, false);
		Object[] res = copyEnumeration(enm);
		// should get 1 entry - META-INF/
		assertEquals("folders ignored; found " + ObjectUtils.nullSafeToString(res), 1, res.length);
	}

	// Valid jars do not have entries for root folder / - in fact it doesn't
	// even exist
	public void testGetEntryOnRoot() throws Exception {
		URL url = bundle.getEntry("/");
		assertNotNull(url);
	}

	// get folders
	public void testGetEntriesShouldReturnFoldersOnRoot() throws Exception {
		Enumeration enm = bundle.getEntryPaths("/");
		Object[] res = copyEnumeration(enm);
		assertEquals("folders ignored; found " + ObjectUtils.nullSafeToString(res), 2, res.length);
	}

	public void testGetFolderEntry() throws Exception {
		URL url = bundle.getEntry("META-INF/");
		assertNotNull(url);
	}

	public void testGetFolderEntries() throws Exception {
		Enumeration enm = bundle.getEntryPaths("META-INF/");
		Object[] res = copyEnumeration(enm);
		assertEquals("folders ignored; found " + ObjectUtils.nullSafeToString(res), 1, res.length);
	}

	public void testURLFolderReturnsProperPathForFolders() throws Exception {
		Enumeration enm = bundle.findEntries("/", "META-INF", false);
		assertNotNull(enm);
		assertTrue(enm.hasMoreElements());
		assertTrue(((URL) enm.nextElement()).getPath().endsWith("/"));
	}
}
