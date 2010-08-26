/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.osgi.test.provisioning.internal;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class MavenArtifactFinderTest extends TestCase {

	private static final String GROUP_ID = "foo";
	private static final String PATH = "src/test/resources/org/springframework/osgi/test";


	public void testFindMyArtifact() throws IOException {
		MavenPackagedArtifactFinder finder = new MavenPackagedArtifactFinder(GROUP_ID, "test-artifact", "1.0-SNAPSHOT",
			"jar");
		File found = finder.findPackagedArtifact(new File(PATH));
		assertNotNull(found);
		assertTrue(found.exists());
	}

	public void testFindChildArtifact() throws IOException {
		MavenPackagedArtifactFinder finder = new MavenPackagedArtifactFinder(GROUP_ID, "test-child-artifact",
			"1.0-SNAPSHOT", "jar");
		File found = finder.findPackagedArtifact(new File(PATH));
		assertNotNull(found);
		assertTrue(found.exists());
	}

	public void testFindParentArtifact() throws IOException {
		MavenPackagedArtifactFinder finder = new MavenPackagedArtifactFinder(GROUP_ID, "test-artifact", "1.0-SNAPSHOT",
			"jar");
		File found = finder.findPackagedArtifact(new File(PATH + "/child"));
		assertNotNull(found);
		assertTrue(found.exists());
	}

	public void testSameArtifactIdInTwoDifferentGroupsWithGroup1() throws Exception {
		MavenPackagedArtifactFinder finder = new MavenPackagedArtifactFinder("group1", "artifact", "1.0", "jar");
		File found = finder.findPackagedArtifact(new File(PATH));
		assertNotNull(found);
		assertTrue(found.exists());
		assertTrue(found.getAbsolutePath().indexOf("group1") > -1);
		// make sure group2 is not selected
		assertFalse(found.getAbsolutePath().indexOf("group2") > -1);
	}

	public void testSameArtifactIdInTwoDifferentGroupsWithGroup2() throws Exception {
		MavenPackagedArtifactFinder finder = new MavenPackagedArtifactFinder("group2", "artifact", "1.0", "jar");
		File found = finder.findPackagedArtifact(new File(PATH));
		assertNotNull(found);
		assertTrue(found.exists());
		assertTrue(found.getAbsolutePath().indexOf("group2") > -1);
		// make sure group2 is not selected
		assertFalse(found.getAbsolutePath().indexOf("group1") > -1);
	}

	public void testPomWithoutAGroupId() throws Exception {
		MavenPackagedArtifactFinder finder = new MavenPackagedArtifactFinder("non-existing", "badpom", "1.0", "jar");
		try {
			File found = finder.findPackagedArtifact(new File(PATH));
			fail("expected exception");
		}
		catch (Exception ex) {
			//expected
		}
	}
}