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

package org.springframework.osgi.test.internal;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.springframework.osgi.test.internal.util.IOUtils;
import org.springframework.osgi.test.internal.util.jar.JarCreator;
import org.springframework.osgi.test.internal.util.jar.storage.MemoryStorage;
import org.springframework.osgi.test.internal.util.jar.storage.Storage;

/**
 * @author Costin Leau
 * 
 */
public class JarCreatorTests extends TestCase {

	private JarCreator creator;

	private Storage storage;


	protected void setUp() throws Exception {
		creator = new JarCreator();
		storage = new MemoryStorage();
		creator.setStorage(storage);
	}

	protected void tearDown() throws Exception {
		storage.dispose();
	}

	public void testJarCreation() throws Exception {

		final Manifest mf = new Manifest();

		Map entries = mf.getEntries();
		Attributes attrs = new Attributes();

		attrs.putValue("rocco-ventrella", "winelight");
		entries.put("test", attrs);

		String location = JarCreatorTests.class.getName().replace('.', '/') + ".class";
		// get absolute file location
		// file:/...s/org/springframework/osgi/test/JarCreatorTests.class
		final URL clazzURL = getClass().getClassLoader().getResource(location);

		// go two folders above
		// ...s/org/springframework/
		String rootPath = new URL(clazzURL, "../../").toExternalForm();

		String firstLevel = new URL(clazzURL, "../").toExternalForm().substring(rootPath.length());
		// get file folder
		String secondLevel = new URL(clazzURL, ".").toExternalForm().substring(rootPath.length());

		// now determine the file relative to the root
		String file = clazzURL.toExternalForm().substring(rootPath.length());

		// create a simple jar from a given class and a manifest
		creator.setContentPattern(new String[] { file });
		creator.setRootPath(rootPath);
		creator.setAddFolders(true);

		System.out.println("creating jar with just one file " + file + " from root " + rootPath);

		// create the jar
		creator.createJar(mf);

		// start reading the jar
		JarInputStream jarStream = null;

		try {
			jarStream = new JarInputStream(storage.getInputStream());
			// get manifest
			assertEquals("original manifest not found", mf, jarStream.getManifest());

			// move the jar stream to the first entry (which should be META-INF/ folder)
			String entryName = jarStream.getNextEntry().getName();

			assertEquals("META-INF/ not found", "META-INF/", entryName);

			entryName = jarStream.getNextEntry().getName();
			assertEquals("folders above the file not included", firstLevel, entryName);

			entryName = jarStream.getNextEntry().getName();
			assertEquals("file folder not included", secondLevel, entryName);

			// now get the file
			jarStream.getNextEntry();
			// open the original file
			InputStream originalFile = clazzURL.openStream();

			int b;
			while ((b = originalFile.read()) != -1)
				assertEquals("incorrect jar content", b, jarStream.read());
		}
		finally {
			IOUtils.closeStream(jarStream);

		}
	}
}
