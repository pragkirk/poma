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

package org.springframework.osgi.web.extender.deployer.support;

import java.net.URLEncoder;
import java.util.Dictionary;
import java.util.Properties;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.web.deployer.ContextPathStrategy;
import org.springframework.osgi.web.deployer.support.DefaultContextPathStrategy;

/**
 * 
 * @author Costin Leau
 */
public class DefaultContextPathStrategyTest extends TestCase {

	private ContextPathStrategy strategy;

	protected void setUp() throws Exception {
		strategy = new DefaultContextPathStrategy();
	}

	protected void tearDown() throws Exception {
		strategy = null;
	}

	private Bundle createBundleWithLocation(final String location) {
		return new MockBundle() {

			public String getLocation() {
				return location;
			}
		};
	}

	private String encode(String string) throws Exception {
		return URLEncoder.encode(string, "UTF-8");
	}

	public void testRootPath() throws Exception {
		final String expectedContextPath = "/";

		assertEquals(expectedContextPath, strategy.getContextPath(createBundleWithLocation(expectedContextPath)));
	}

	public void testPathAlwaysStartsWithSlash() throws Exception {
		final String expectedContextPath = "file";

		assertTrue(strategy.getContextPath(createBundleWithLocation(expectedContextPath)).startsWith("/"));
	}

	public void testBundleWithNullLocation() throws Exception {
		assertTrue(strategy.getContextPath(createBundleWithLocation(null)).startsWith("/"));
	}

	public void testBundleWithFileLocation() throws Exception {
		final String expectedContextPath = "/file";
		final String location = "/root" + expectedContextPath;

		assertEquals(expectedContextPath, strategy.getContextPath(createBundleWithLocation(location)));
	}

	public void testBundleWithFileNameAndExtension() throws Exception {
		final String expectedContextPath = "/file";
		final String location = "/root" + expectedContextPath + ".extension";

		assertEquals(expectedContextPath, strategy.getContextPath(createBundleWithLocation(location)));
	}

	public void testBundleWithFileNameButNoLeadingSlash() throws Exception {
		final String expectedContextPath = "file";
		final String location = expectedContextPath;

		assertEquals("/" + expectedContextPath, strategy.getContextPath(createBundleWithLocation(location)));
	}

	public void testBundleWithFolderLocation() throws Exception {
		final String expectedContextPath = "/folder";
		final String location = "/root" + expectedContextPath + "/";

		assertEquals(expectedContextPath, strategy.getContextPath(createBundleWithLocation(location)));
	}

	public void testBundleWithFolderLocationAndDots() throws Exception {
		final String expectedContextPath = "/folder";
		final String location = "/root" + expectedContextPath + ".extension/";

		assertEquals("extension should not be removed when dealing with folders", expectedContextPath, strategy
				.getContextPath(createBundleWithLocation(location)));
	}

	public void testBundleWithFolderButNoLeadingSlash() throws Exception {
		final String expectedContextPath = "folder";
		final String location = expectedContextPath + ".extension/";

		assertEquals("/" + expectedContextPath, strategy.getContextPath(createBundleWithLocation(location)));
	}

	public void testBundleWithSpecialCharactersLocation() throws Exception {
		final String expectedContextPath = "some file";
		final String location = "/root/" + expectedContextPath;

		assertEquals("/" + encode(expectedContextPath), strategy.getContextPath(createBundleWithLocation(location)));
	}

	public void testBundleWithSpecialCharactersAndExtensionFolderLocation() throws Exception {
		final String expectedContextPath = "some file";
		final String location = "/root/" + expectedContextPath + ".bla extension/";

		assertEquals("/" + encode(expectedContextPath), strategy.getContextPath(createBundleWithLocation(location)));
	}

	public void testFallBackToBundleName() throws Exception {
		final String expectedContextPath = "someName";

		final Dictionary dict = new Properties();
		dict.put(Constants.BUNDLE_NAME, expectedContextPath);

		Bundle bundle = new MockBundle() {

			public Dictionary getHeaders() {
				return dict;
			}

			public String getLocation() {
				return null;
			}
		};

		assertEquals("/" + expectedContextPath, strategy.getContextPath(bundle));
	}

	public void testFallBackToSymbolicName() throws Exception {
		final String expectedContextPath = "this.is.my.symbolic.name";
		Bundle bundle = new MockBundle(expectedContextPath) {

			public String getLocation() {
				return null;
			}
		};

		assertEquals("/" + expectedContextPath, strategy.getContextPath(bundle));
	}

	public void testFallBackToIdentity() throws Exception {
		Bundle bundle = new MockBundle() {

			public Dictionary getHeaders() {
				return null;
			}

			public String getLocation() {
				return null;
			}

			public String getSymbolicName() {
				return null;
			}
		};
		String path = strategy.getContextPath(bundle);
		assertTrue(path.startsWith("/"));
	}

	public void testBundleHeader() throws Exception {
		String value = "bla";
		Dictionary headers = new Properties();
		headers.put("Web-ContextPath", value);
		Bundle bundle = new MockBundle(headers);

		String path = strategy.getContextPath(bundle);
		assertTrue(path.startsWith("/"));
		assertEquals("/" + value, path);
	}

	public void testBundleHeaderWithoutText() throws Exception {
		String value = "   ";
		Dictionary headers = new Properties();
		headers.put("Web-ContextPath", value);
		Bundle bundle = new MockBundle(headers);

		assertEquals("/", strategy.getContextPath(bundle));
	}

	public void testBundleHeaderMispelled() throws Exception {
		final String expectedContextPath = "someLocation";

		final Dictionary dict = new Properties();
		dict.put("Web-ContextPth", "somethingElse");

		Bundle bundle = new MockBundle() {

			public Dictionary getHeaders() {
				return dict;
			}

			public String getLocation() {
				return expectedContextPath;
			}
		};

		assertEquals("/" + expectedContextPath, strategy.getContextPath(bundle));
	}

	public void testBundleHeaderEncoding() throws Exception {
		String value = "some file";
		Dictionary headers = new Properties();
		headers.put("Web-ContextPath", value);
		Bundle bundle = new MockBundle(headers);

		String path = strategy.getContextPath(bundle);
		assertTrue(path.startsWith("/"));
		assertEquals("/" + encode(value), path);
	}

	public void testPrefixRemoval() throws Exception {
		final String expectedContextPath = "/folder";
		final String location = "file:/somePath" + expectedContextPath + "/";

		assertEquals(expectedContextPath, strategy.getContextPath(createBundleWithLocation(location)));
	}

	public void testMultiPrefixRemoval() throws Exception {
		final String expectedContextPath = "/bundle";
		final String location = "jar:http@file:url:/somePath" + expectedContextPath + ".jar";

		assertEquals(expectedContextPath, strategy.getContextPath(createBundleWithLocation(location)));
	}

	public void testAnotherMultiPrefixRemoval() throws Exception {
		final String expectedContextPath = "com.foo.some.bundle.web_1.0.0";
		final String location = "initial@reference:file:" + expectedContextPath + ".SNAPSHOT";

		assertEquals("/" + expectedContextPath, strategy.getContextPath(createBundleWithLocation(location)));
	}

	public void testEmptyWebContextPath() throws Exception {
		String value = "";
		Dictionary headers = new Properties();
		headers.put("Web-ContextPath", value);
		Bundle bundle = new MockBundle(headers);
		String path = strategy.getContextPath(bundle);
		assertEquals("/", path);
	}

	public void testRootWebContextPath() throws Exception {
		String value = "/";
		Dictionary headers = new Properties();
		headers.put("Web-ContextPath", value);
		Bundle bundle = new MockBundle(headers);
		String path = strategy.getContextPath(bundle);
		assertEquals(value, path);
	}

	public void testWebContextPathStartsWithSlash() throws Exception {
		String value = "/web";
		Dictionary headers = new Properties();
		headers.put("Web-ContextPath", value);
		Bundle bundle = new MockBundle(headers);
		String path = strategy.getContextPath(bundle);
		assertEquals(value, path);
	}

	public void testWebContextPathContainsExtraWhiteSpaces() throws Exception {
		String value = " /web  ";
		Dictionary headers = new Properties();
		headers.put("Web-ContextPath", value);
		Bundle bundle = new MockBundle(headers);
		String path = strategy.getContextPath(bundle);
		assertEquals(value.trim(), path);
	}

	public void testWebContextWithNestedPath() throws Exception {
		String value = "/web/my/app";
		Dictionary headers = new Properties();
		headers.put("Web-ContextPath", value);
		Bundle bundle = new MockBundle(headers);
		String path = strategy.getContextPath(bundle);
		assertEquals(value, path);
	}

	public void testWebContextWithNestedPathAndMultipleSlashes() throws Exception {
		String value = "/web/my/super/uber-nice/app/";
		Dictionary headers = new Properties();
		headers.put("Web-ContextPath", value);
		Bundle bundle = new MockBundle(headers);
		String path = strategy.getContextPath(bundle);
		assertEquals(value, path);
	}
}