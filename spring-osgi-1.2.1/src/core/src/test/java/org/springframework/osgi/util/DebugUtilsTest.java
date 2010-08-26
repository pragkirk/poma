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

package org.springframework.osgi.util;

import junit.framework.TestCase;

import org.osgi.framework.Version;
import org.springframework.osgi.TestUtils;

/**
 * Test for the critical logging path in debug utils.
 * 
 * @author Costin Leau
 * 
 */
public class DebugUtilsTest extends TestCase {

	private Version getVersion(String statement, String pkg) {
		return (Version) TestUtils.invokeStaticMethod(DebugUtils.class, "getVersion", new String[] { statement, pkg });
	}

	public void testNoVersion() throws Exception {
		String pkg = "foo";
		assertEquals(Version.emptyVersion, getVersion(pkg, pkg));
	}

	public void testSingleVersion() throws Exception {
		String pkg = "foo";
		String version = "1.2";
		assertEquals(Version.parseVersion(version), getVersion(pkg + ";version=" + version, pkg));
	}

	public void testVersionRange() throws Exception {
		String pkg = "foo";
		String version = "1.2.0.bla";
		assertEquals(Version.parseVersion(version), getVersion(pkg + ";version=\"[" + version + ",3.4\")", pkg));
	}

	public void testVersionRangePlusExtraDirective() throws Exception {
		String pkg = "foo";
		String version = "1.2.0.bla";
		assertEquals(Version.parseVersion(version), getVersion(pkg + ";version=\"[" + version
				+ ",3.4\");resolution:=optional", pkg));
	}

	public void testNoVersionPlusExtraDirective() throws Exception {
		String pkg = "foo";
		assertEquals(Version.emptyVersion, getVersion(pkg + ";resolution:=optional", pkg));
	}

	public void testSingleVersionPlusExtraDirective() throws Exception {
		String pkg = "foo";
		String version = "1.2.0.bla";
		assertEquals(Version.parseVersion(version), getVersion(pkg + ";version=" + version + ";resolution:=optional",
			pkg));
	}

	public void testSingleVersionWithQuotes() throws Exception {
		String pkg = "foo";
		String version = "3.4.5.pausesti";
		assertEquals(Version.parseVersion(version), getVersion(pkg + ";version=\"" + version + "\"", pkg));
	}

}
