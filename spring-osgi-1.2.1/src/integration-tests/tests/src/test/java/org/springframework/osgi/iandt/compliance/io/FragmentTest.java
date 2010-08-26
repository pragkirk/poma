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

import org.osgi.framework.Bundle;
import org.springframework.osgi.iandt.io.BaseIoTest;

/**
 * Raw test for discovering the fragments support for each platform. The native
 * {@link Bundle#findEntries(String, String, boolean)} method is being used to
 * see if files from the attached fragments are attached.
 * 
 * 
 * @author Costin Leau
 * 
 */
public class FragmentTest extends BaseIoTest {

	//
	// Folder tests
	//

	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return isFelix();
	}

	protected String[] getBundleContentPattern() {
		return new String[] { "**/*" };
	}

	/**
	 * Check META-INF folders.
	 * 
	 */
	public void testRootFoldersInFragmentsAndOwner() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "META-INF", false));
		assertResourceArray(res, 3);
	}

	public void testRootFolderCommonInFragmentsAlone() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "fragment.folder", false));
		assertResourceArray(res, 2);
	}

	public void testRootFolderOnlyInFragment1() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "fragment1.folder", false));
		assertResourceArray(res, 1);
	}

	public void testNestedFolderOnlyInFragmentsRecursively() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "nested.folder", true));
		assertResourceArray(res, 2);
	}

	public void testNestedFolderOnlyInFragments() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment.folder", "nested.folder", false));
		assertResourceArray(res, 2);
	}

	public void testRootFolderOnlyInFragment2() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "fragment2.folder", false));
		assertResourceArray(res, 1);
	}

	public void testNestedFolderOnlyInFragment1() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment1.folder", "nested.folder.1", false));
		assertResourceArray(res, 1);
	}

	public void testNestedFolderOnlyInFragment2() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment2.folder", "nested.folder.2", false));
		assertResourceArray(res, 1);
	}

	public void testCommonFolderOnlyInFragmentsButNotInHost() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment.folder", "nested.folder", false));
		assertResourceArray(res, 2);
	}

	public void testCommonFolderInFragmentsAndHost() {
		Object[] res = copyEnumeration(bundle.findEntries("/org/springframework", "osgi", false));
		assertResourceArray(res, 3);
	}

	public void testFolderOnlyInHost() {
		Object[] res = copyEnumeration(bundle.findEntries("/org/springframework/osgi/iandt", "bundleScope", false));
		assertResourceArray(res, 1);
	}

	//
	// File tests
	//

	public void testRootFileInBothFragmentsButNotInHost() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "fragment.file", false));
		assertResourceArray(res, 2);
	}

	public void testRootFileOnlyInFragment1() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "fragment1.file", false));
		assertResourceArray(res, 1);
	}

	public void testRootFileOnlyInFragment2() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "fragment2.file", false));
		assertResourceArray(res, 1);
	}

	public void testRootFileOnlyInHostBundle() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "log4j.properties", false));
		assertResourceArray(res, 1);
	}

	public void testNestedFileOnlyInFragments() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment.folder/nested.folder", "nested.file", false));
		assertResourceArray(res, 2);
	}

	public void testNestedFileOnlyInFragment1() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment1.folder/nested.folder.1", "nested.file.1.1", false));
		assertResourceArray(res, 1);
	}

	public void testNestedFileOnlyInFragment2() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment2.folder/nested.folder.2", "nested.file.2.2", false));
		assertResourceArray(res, 1);
	}

	public void testDuplicateFilesInHostAndFragments() {
		Object[] res = copyEnumeration(bundle.findEntries("/org/springframework/osgi/iandt/io", "duplicate.file", false));
		assertResourceArray(res, 3);
	}

	//
	// Classpath tests
	//

	public void testGetResourceOnRootDir() throws Exception {
		URL root = bundle.getResource("/");
		System.out.println(root);
		assertNotNull("root path not considered", root);
	}

	public void testGetResourceSOnRootDir() throws Exception {
		Object[] res = copyEnumeration(bundle.getResources("/"));
		// 3 paths should be found (1 host + 2 fragments)
		assertResourceArray(res, 3);
	}
}
