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
package org.springframework.osgi.test.internal.util;

import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.osgi.framework.Constants;
import org.springframework.core.io.Resource;
import org.springframework.osgi.test.internal.util.jar.JarUtils;
import org.springframework.osgi.test.internal.util.jar.ManifestUtils;
import org.springframework.osgi.test.internal.util.jar.storage.MemoryStorage;
import org.springframework.osgi.test.internal.util.jar.storage.Storage;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 
 * @author Costin Leau
 */
public class ManifestUtilsTest extends TestCase {

	private Storage storage;

	private JarInputStream in;

	protected void setUp() throws Exception {
		storage = new MemoryStorage();
	}

	protected void tearDown() throws Exception {
		storage.dispose();
		IOUtils.closeStream(in);
	}

	public void testEmptyManifest() throws Exception {
		Manifest mf = new Manifest();
		mf.getMainAttributes().putValue("foo", "bar");
		createJar(mf);
		in = new JarInputStream(storage.getInputStream());
		assertEquals(mf, in.getManifest());
	}

	public void testJarUtilsReadResource() throws Exception {
		Manifest mf = new Manifest();
		mf.getMainAttributes().putValue("foo", "bar");
		createJar(mf);
		assertEquals(mf, JarUtils.getManifest(storage.getResource()));
	}

	public void testExportEntries() throws Exception {
		Manifest mf = new Manifest();
		Attributes attrs = mf.getMainAttributes();
		String[] packages = new String[] { "foo.bar; version:=1", "bar.foo", "hop.trop" };
		attrs.putValue(Constants.EXPORT_PACKAGE, StringUtils.arrayToCommaDelimitedString(packages));
		createJar(mf);
		String[] entries = ManifestUtils.determineImportPackages(new Resource[] { storage.getResource(),
				storage.getResource() });
		assertEquals(3, entries.length);
		ObjectUtils.nullSafeEquals(packages, entries);
	}

	private void createJar(Manifest mf) throws Exception {
		mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		JarOutputStream out = new JarOutputStream(storage.getOutputStream(), mf);
		out.flush();
		out.close();
	}
}
