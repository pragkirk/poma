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
package org.springframework.osgi.extender.internal.util;

import java.util.Dictionary;
import java.util.Properties;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.springframework.osgi.extender.support.internal.ConfigUtils;
import org.springframework.osgi.mock.MockBundle;

public class ConfigUtilsVersioningTest extends TestCase {

	private Bundle bundle;

	private Dictionary props;

	private Version min, max, version;

	protected void setUp() throws Exception {
		props = new Properties();
		bundle = new MockBundle(props);

		min = Version.parseVersion("1.2");
		max = Version.parseVersion("1.3");
		version = Version.parseVersion("1.2.5");
	}

	protected void tearDown() throws Exception {
		props = null;
		bundle = null;
	}

	private void addVersion(String version) {
		props.put(ConfigUtils.EXTENDER_VERSION, version);
	}

	public void testNoVersion() {
		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, Version.emptyVersion));
	}

	public void testLeftOpenRange() {
		String ver = "(1.2, 1.3]";
		addVersion(ver);

		assertFalse(ConfigUtils.matchExtenderVersionRange(bundle, min));
		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, version));
	}

	public void testRightOpenRange() {
		String ver = "[1.2, 1.3)";
		addVersion(ver);

		assertFalse(ConfigUtils.matchExtenderVersionRange(bundle, max));
		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, version));
	}

	public void testLeftCloseRange() {
		String ver = "[1.2, 1.3]";
		addVersion(ver);

		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, min));
		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, version));
	}

	public void testRightCloseRange() {
		String ver = "[1.2, 1.3]";
		addVersion(ver);

		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, max));
		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, version));
	}

	public void testTooManyCommas() {
		String ver = "[1.2, ,1.3]";
		addVersion(ver);

		try {
			ConfigUtils.matchExtenderVersionRange(bundle, Version.emptyVersion);
			fail("should have thrown exception; invalid range");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testTooManyCommasAgain() {
		String ver = "[1,2 , 1.3)";
		addVersion(ver);

		try {
			ConfigUtils.matchExtenderVersionRange(bundle, Version.emptyVersion);
			fail("should have thrown exception; invalid range");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testNoBracketsIntervalOnRight() {
		String ver = "[1.2, 1.3";
		addVersion(ver);

		try {
			ConfigUtils.matchExtenderVersionRange(bundle, Version.emptyVersion);
			fail("should have thrown exception; invalid range");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}

	}

	public void testNoBracketsIntervalOnLeft() {
		String ver = "1.2, 1.3)";
		addVersion(ver);

		try {
			ConfigUtils.matchExtenderVersionRange(bundle, Version.emptyVersion);
			fail("should have thrown exception; invalid range");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}

	}

	public void testNoCommaInterval() {
		String ver = "[1.2 1.3]";
		addVersion(ver);

		try {
			ConfigUtils.matchExtenderVersionRange(bundle, Version.emptyVersion);
			fail("should have thrown exception; invalid range");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}
}
