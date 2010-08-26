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

import java.util.Properties;

import junit.framework.TestCase;

/**
 * @author Costin Leau
 * 
 */
public class PropertiesUtilTest extends TestCase {

	private static final String VALUE = "bar";

	private Properties props;

	protected void setUp() throws Exception {
		props = new Properties();
		props.load(getClass().getResourceAsStream("test.properties"));
		props = PropertiesUtil.expandProperties(props);
	}

	protected void tearDown() throws Exception {
		props = null;
	}

	public void testSimpleProperties() {
		assertEquals(VALUE, props.get("foo"));
	}

	public void testSimpleKeyExpansion() {
		String key = "expanded." + VALUE;
		assertEquals(key, props.get(key));
	}

	public void testDoubleKeyExpansion() {
		String key = VALUE + VALUE;
		assertEquals(key, props.get(key));
	}

	public void testSimpleValueExpansion() {
		String key = "expanded.foo";
		assertEquals(key, props.get(key));
	}

	public void testDoubleValueExpansion() {
		String key = "foofoo";
		assertEquals(key, props.get(key));
	}

	public void testKeyWithIncludeValue() {
		Properties properties = new Properties();
		String sign = "+";
		properties.put("include", sign);
		assertEquals(properties, PropertiesUtil.filterValuesStartingWith(props, sign));
	}
}
