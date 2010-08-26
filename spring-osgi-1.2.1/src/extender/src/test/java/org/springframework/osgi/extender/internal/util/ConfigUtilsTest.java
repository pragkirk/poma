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
import java.util.Hashtable;

import junit.framework.TestCase;

import org.springframework.osgi.extender.support.internal.ConfigUtils;
import org.springframework.osgi.io.OsgiBundleResource;

/**
 * @author Costin Leau
 * 
 */
public class ConfigUtilsTest extends TestCase {

	private Dictionary headers;


	protected void setUp() throws Exception {
		headers = new Hashtable();
	}

	protected void tearDown() throws Exception {
		headers = null;
	}

	public void testGetCompletelyEmptySpringContextHeader() throws Exception {
		String[] locations = ConfigUtils.getHeaderLocations(headers);
		assertEquals(0, locations.length);

	}

	public void testGetEmptyConfigLocations() throws Exception {
		String entry = ";early-init-importers=true";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, entry);
		String[] locations = ConfigUtils.getHeaderLocations(headers);
		assertEquals(0, locations.length);
	}

	public void testGetNotExistingConfigLocations() throws Exception {
		String location = "osgibundle:/META-INF/non-existing.xml";
		String entry = location + "; early-init-importers=true";

		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, entry);
		String[] locations = ConfigUtils.getHeaderLocations(headers);
		assertEquals(1, locations.length);
		assertEquals(location, locations[0]);

	}

	public void testGetWildcardConfigLocs() throws Exception {
		String location = "classpath:/META-INF/spring/*.xml";
		String entry = location + "; early-init-importers=true";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, entry);
		String[] locations = ConfigUtils.getHeaderLocations(headers);
		assertEquals(1, locations.length);
		assertEquals(location, locations[0]);
	}

	public void testMultipleConfigLocs() throws Exception {
		String location1 = "classpath:/META-INF/spring/*.xml";
		String location2 = "osgibundle:/META-INF/non-existing.xml";

		String entry = location1 + "," + location2 + "; early-init-importers=true";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, entry);
		String[] locations = ConfigUtils.getHeaderLocations(headers);
		assertEquals(2, locations.length);
		assertEquals(location1, locations[0]);
		assertEquals(location2, locations[1]);
	}

	public void testLocationWithMultipleDots() throws Exception {
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER,
			"META-INF/file.with.multiple.dots.xml, META-INF/another.file.xml");
		String[] locations = ConfigUtils.getHeaderLocations(headers);
		assertEquals(2, locations.length);
	}
}
