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

package org.springframework.osgi.io.internal;

import java.util.Properties;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.util.ObjectUtils;

/**
 * @author Costin Leau
 * 
 */
public class OsgiHeaderUtilsTest extends TestCase {

	private static final String DEFAULT_VERSION = "0.0.0";

	private static String PKG = "com.acme.facade";


	public void testGetNoBundleClassPathDefined() {
		Properties props = new Properties();
		Bundle bundle = new MockBundle(props);
		String[] cp = OsgiHeaderUtils.getBundleClassPath(bundle);
		assertEquals(0, cp.length);
	}

	public void testGetBundleClassPath() {
		Properties props = new Properties();
		String path1 = ".";
		String path2 = "WEB-INF/";
		props.setProperty(Constants.BUNDLE_CLASSPATH, path1 + "," + path2);
		Bundle bundle = new MockBundle(props);
		String[] cp = OsgiHeaderUtils.getBundleClassPath(bundle);
		assertEquals(2, cp.length);
		assertEquals(path1, cp[0]);
		assertEquals(path2, cp[1]);
	}

	public void testGetBundleClassPathWithWhiteSpaces() {
		Properties props = new Properties();
		String path1 = ".";
		String path2 = "WEB-INF/";
		props.setProperty(Constants.BUNDLE_CLASSPATH, " " + path1 + " ,  " + path2 + "   ");
		Bundle bundle = new MockBundle(props);
		String[] cp = OsgiHeaderUtils.getBundleClassPath(bundle);

		// check for spaces
		assertSame(cp[0], cp[0].trim());
		assertSame(cp[1], cp[1].trim());
		// check result
		assertEquals(2, cp.length);
		assertEquals(path1, cp[0]);
		assertEquals(path2, cp[1]);
	}

	public void testGetRequireBundleUndeclared() throws Exception {
		Properties props = new Properties();
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);
		assertEquals(0, rb.length);
	}

	public void testGetRequireBundleWithMultipleBundlesAttributesAndWhitespaces() throws Exception {
		Properties props = new Properties();
		String pkg2 = "foo.bar";
		props.setProperty(Constants.REQUIRE_BUNDLE, "  " + PKG + ";visibility:=reexport;bundle-version=\"1.0\" ,\t  "
				+ pkg2 + "\n  ");
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);

		assertSame(rb[0], rb[0].trim());
		assertSame(rb[1], rb[1].trim());
	}

	public void testGetRequireBundleWMultipleUnversionedEntries() throws Exception {
		Properties props = new Properties();
		String b1 = "foo";
		String b2 = "bar";
		props.setProperty(Constants.REQUIRE_BUNDLE, b1 + "," + b2);
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);
		assertEquals(2, rb.length);
		assertEquals(b1, rb[0]);
		assertEquals(b2, rb[1]);
	}

	public void testRequireBundleWithSimpleVersions() throws Exception {
		Properties props = new Properties();
		String b1 = "foo;bundle-version=1.1.0";
		String b2 = "bar;bundle-version=2";
		props.setProperty(Constants.REQUIRE_BUNDLE, b1 + "," + b2);
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);
		assertEquals(2, rb.length);
		assertEquals(b1, rb[0]);
		assertEquals(b2, rb[1]);
	}

	public void testRequireBundleWithRangeVersions() throws Exception {
		Properties props = new Properties();
		String b1 = "foo;bundle-version=\"[1.0,2.0)\"";
		String b2 = "bar;bundle-version=1.0.0";
		props.setProperty(Constants.REQUIRE_BUNDLE, b1 + "," + b2);
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);
		assertEquals(2, rb.length);
		assertEquals(b1, rb[0]);
		assertEquals(b2, rb[1]);
	}

	public void testRequireBundleWithQuotes() throws Exception {
		Properties props = new Properties();
		String b1 = "foo;bundle-version=\"[1.0,2.0)\"";
		String b2 = "bar;bundle-version=\"1.0.0\"";
		props.setProperty(Constants.REQUIRE_BUNDLE, b1 + "," + b2);
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);
		assertEquals(2, rb.length);
		assertEquals(b1, rb[0]);
		assertEquals(b2, rb[1]);
	}

	public void testRequireBundleWithVersionAndExtraAttributes() throws Exception {
		Properties props = new Properties();
		String b1 = "foo;bundle-version=\"[1.0,2.0)\";visibility:=reexport";
		String b2 = "bar;resolution:=optional;bundle-version=\"1.0.0\"";
		props.setProperty(Constants.REQUIRE_BUNDLE, b1 + "," + b2);
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);
		assertEquals(2, rb.length);
		assertEquals(b1, rb[0]);
		assertEquals(b2, rb[1]);
	}

	public void testParseRequireBundleEntryWithNoVersion() throws Exception {
		String entry = PKG;
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(DEFAULT_VERSION, result[1]);
	}

	public void testParseRequireBundleEntryWithSimpleUnquotedVersion() throws Exception {
		String version = "1.0.0.a";
		String entry = PKG + ";" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=" + version;
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(version, result[1]);
	}

	public void testParseRequireBundleEntryWithSimpleQuotedVersion() throws Exception {
		String version = "1.2.3";
		String entry = PKG + ";" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"" + version + "\"";
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(version, result[1]);
	}

	public void testParseRequireBundleEntryWithVersionRange() throws Exception {
		String version = "[1.0.0,2.0.0a)";
		String entry = PKG + ";" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"" + version + "\"";
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(version, result[1]);
	}

	public void testParseRequireBundleEntryWithSimpleUnquotedVersionAndExtraAttributes() throws Exception {
		String version = "1.0.0.a";
		String entry = PKG + ";visibility:=reexport;" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=" + version
				+ ";resolution:=optional";
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(version, result[1]);
	}

	public void testParseRequireBundleEntryWithSimpleQuotedVersionAndExtraAttributes() throws Exception {
		String version = "1.0.0.a";
		String entry = PKG + ";visibility:=reexport;" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"" + version
				+ "\";resolution:=optional";
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(version, result[1]);
	}

	public void testParseRequireBundleEntryWithVersionRangeAndExtraAttributes() throws Exception {
		String version = "[1.0.0,2.0.0a)";
		String entry = PKG + ";visibility:=reexport;" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"" + version
				+ "\";resolution:=optional";
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(version, result[1]);
	}

	public void testParseRequireBundleEntryWithNoVersionAndExtraAttributes() throws Exception {
		String entry = PKG + ";visibility:=reexport;resolution:=optional";
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(DEFAULT_VERSION, result[1]);
	}

	//
	// old battery of tests
	//

	public void testParseEntryWithAttribute() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG + ";visibility:=reexport");
		assertEquals(PKG, values[0]);
		assertEquals(DEFAULT_VERSION, values[1]);
	}

	public void testParseSimpleEntry() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG);
		assertEquals(PKG, values[0]);
		assertEquals(DEFAULT_VERSION, values[1]);
	}

	public void testParseEntryWithSingleVersion() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG + ";bundle-version=\"1.0\"");
		assertEquals(PKG, values[0]);
		assertEquals("1.0", values[1]);
	}

	public void testParseEntryWithRangeVersion() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG + ";bundle-version=\"[1.0,2.0)\"");
		assertEquals(PKG, values[0]);
		assertEquals("[1.0,2.0)", values[1]);
	}

	public void testParseEntryWithRangeVersionAndExtraHeader() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG
				+ ";bundle-version=\"[1.0,2.0)\";visibility:=reexport");
		assertEquals(PKG, values[0]);
		assertEquals("[1.0,2.0)", values[1]);
	}

	public void testParseEntryWithExtraHeaderAndRangeVersion() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG
				+ ";visibility:=reexport;bundle-version=\"[1.0,2.0)\"");
		assertEquals(PKG, values[0]);
		assertEquals("[1.0,2.0)", values[1]);
	}

	public void testParseEntryWithExtraHeaderAndSimpleVersion() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG
				+ ";visibility:=reexport;bundle-version=\"1.0\"");
		assertEquals(PKG, values[0]);
		assertEquals("1.0", values[1]);
	}
}
