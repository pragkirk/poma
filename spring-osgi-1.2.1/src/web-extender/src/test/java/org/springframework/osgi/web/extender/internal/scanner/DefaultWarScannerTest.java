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

package org.springframework.osgi.web.extender.internal.scanner;

import java.util.Enumeration;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.springframework.osgi.mock.ArrayEnumerator;
import org.springframework.osgi.mock.MockBundle;

/**
 * 
 * @author Costin Leau
 * 
 */
public class DefaultWarScannerTest extends TestCase {

	private Bundle bundle;
	private WarScanner scanner;


	protected void setUp() throws Exception {
		scanner = new DefaultWarScanner();
	}

	protected void tearDown() throws Exception {
		bundle = null;
		scanner = null;
	}

	public void testBundleWithNoWebXmlAndNoSuitableLocation() throws Exception {
		bundle = new MockBundle() {

			public Enumeration findEntries(String path, String filePattern, boolean recurse) {
				return null;
			}

			public String getLocation() {
				return "foo.bar";
			}
		};

		assertFalse(scanner.isWar(bundle));
	}

	public void testBundleWithNoLocation() throws Exception {
		bundle = new MockBundle() {

			public String getLocation() {
				return null;
			}
		};
		assertFalse(scanner.isWar(bundle));
	}

	public void testBundleWithProperExtension() throws Exception {
		bundle = new MockBundle() {

			public String getLocation() {
				return "petclinic.war";
			}
		};
		assertTrue(scanner.isWar(bundle));
	}

	public void testBundleWithLongLocation() throws Exception {
		bundle = new MockBundle() {

			public String getLocation() {
				return "initial@reference:file:petclinic.war";
			}
		};
		assertTrue(scanner.isWar(bundle));
	}

	public void testExpandedBundleLocation() throws Exception {

		bundle = new MockBundle() {

			public String getLocation() {
				return "initial@reference:file:petclinic.war/";
			}
		};
		assertTrue(scanner.isWar(bundle));
	}

	public void testWebInfEntryFromRoot() throws Exception {
		bundle = new MockBundle() {

			public Enumeration findEntries(String path, String filePattern, boolean recurse) {
				assertEquals("/", path);
				assertEquals("WEB-INF", filePattern);
				assertEquals(false, recurse);
				return new ArrayEnumerator(new Object[] { new Object() });
			}
		};

		assertTrue(scanner.isWar(bundle));
	}

	public void testWebInfEntry() throws Exception {
		bundle = new MockBundle() {

			public Enumeration findEntries(String path, String filePattern, boolean recurse) {
				// return null the first time around
				if ("/".equals(path))
					return null;

				assertEquals("WEB-INF", path);
				assertNull(filePattern);
				assertEquals(false, recurse);
				return new ArrayEnumerator(new Object[] { new Object() });
			}
		};

		assertTrue(scanner.isWar(bundle));
	}
}
