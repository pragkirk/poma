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
package org.springframework.osgi.iandt.io;

import org.springframework.core.io.Resource;

/**
 * Test to check if loading of files outside of the OSGi world (directly from
 * the filesystem is possible).
 * 
 * @author Costin Leau
 * 
 */
public class IoTests extends BaseIoTest {

	protected String getManifestLocation() {
		return null;
	}

	/**
	 * Add a bundle fragment.
	 */
	protected String[] getTestBundlesNames() {
		return null;
	}

	public void testFileOutsideOSGi() throws Exception {
		String fileLocation = "file:///" + thisClass.getFile().getAbsolutePath();
		// use file system resource defaultLoader
		Resource fileResource = defaultLoader.getResource(fileLocation);
		assertTrue(fileResource.exists());

		// try loading the file using OsgiBundleResourceLoader
		Resource osgiResource = resourceLoader.getResource(fileLocation);
		// check existence of the same file when loading through the
		// OsgiBundleRL
		// NOTE andyp -- we want this to work!!
		assertTrue(osgiResource.exists());

		assertEquals(fileResource.getURL(), osgiResource.getURL());
	}

	public void testNonExistentFileOutsideOSGi() throws Exception {
		String nonExistingLocation = thisClass.getURL().toExternalForm().concat("-bogus-extension");

		Resource nonExistingFile = defaultLoader.getResource(nonExistingLocation);
		assertNotNull(nonExistingFile);
		assertFalse(nonExistingFile.exists());

		Resource nonExistingFileOutsideOsgi = resourceLoader.getResource(nonExistingLocation);
		assertNotNull(nonExistingFileOutsideOsgi);
		assertFalse(nonExistingFileOutsideOsgi.exists());
	}

}
