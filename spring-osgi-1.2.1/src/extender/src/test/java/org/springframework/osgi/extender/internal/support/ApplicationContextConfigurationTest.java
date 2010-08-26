/*
 * Copyright 2006 the original author or authors.
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
package org.springframework.osgi.extender.internal.support;

import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.osgi.extender.support.ApplicationContextConfiguration;
import org.springframework.osgi.mock.EntryLookupControllingMockBundle;

/**
 * Test that given a bundle, we can correctly determine the spring configuration
 * required for it.
 * 
 * @author Adrian Colyer
 */
public class ApplicationContextConfigurationTest extends TestCase {

	private static final String[] META_INF_SPRING_CONTENT = new String[] { "file://META-INF/spring/context.xml",
			"file://META-INF/spring/context-two.xml" };

	public void testBundleWithNoHeaderAndNoMetaInfSpringResourcesIsNotSpringPowered() {
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(null);
		aBundle.setResultsToReturnOnNextCallToFindEntries(null);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertFalse("bundle is not spring powered", config.isSpringPoweredBundle());
	}

	public void testBundleWithSpringResourcesAndNoHeaderIsSpringPowered() {
		EntryLookupControllingMockBundle aBundle = new RepeatingEntryLookupControllingMockBundle(null);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle is spring powered", config.isSpringPoweredBundle());
	}

	public void testBundleWithHeaderAndNoMetaInfResourcesIsSpringPowered() throws Exception {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "META-INF/spring/context.xml");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(null);
		aBundle.setEntryReturnOnNextCallToGetEntry(new URL("file://META-INF/spring/context.xml"));
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle is spring powered", config.isSpringPoweredBundle());
	}

	public void testBundleWithNoHeaderShouldWaitFiveMinutes() {
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(null);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertEquals("bundle should timeout in five minutes", new Long(5 * 60 * 1000), new Long(config.getTimeout()));
	}

	public void testBundleWithWaitFiveSecondWaitForTimeout() {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "*;timeout:=5");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle should be Spring powered", config.isSpringPoweredBundle());
		assertEquals("bundle should timeout in 5 s", new Long(5 * 1000), new Long(config.getTimeout()));
	}

	public void testBundleWithWaitForEver() {
		// *;flavour
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "*;timeout:=none");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle should be Spring powered", config.isSpringPoweredBundle());
		assertEquals("bundle should timeout -2 (indicates forever)", new Long(-2), new Long(config.getTimeout()));
	}

	public void tstConfigLocationsInMetaInfNoHeader() {
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(null);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		String[] configFiles = config.getConfigurationLocations();
		assertEquals("0 config files", 0, configFiles.length);
		// assertEquals("bundle-url:file://META-INF/spring/context.xml",
		// configFiles[0]);
		// assertEquals("bundle-url:file://META-INF/spring/context-two.xml",
		// configFiles[1]);
	}

	public void tstConfigLocationsInMetaInfWithHeader() throws Exception {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "META-INF/spring/context.xml");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		aBundle.setEntryReturnOnNextCallToGetEntry(new URL("file://META-INF/spring/context.xml"));
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		String[] configFiles = config.getConfigurationLocations();
		assertTrue("bundle should be Spring powered", config.isSpringPoweredBundle());
		assertEquals("osgibundle:META-INF/spring/context.xml", configFiles[0]);
	}

	public void tstConfigLocationsInMetaInfWithWildcardHeader() throws Exception {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "*;wait-for-dependencies:=false");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		aBundle.setEntryReturnOnNextCallToGetEntry(new URL(META_INF_SPRING_CONTENT[0]));
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		String[] configFiles = config.getConfigurationLocations();
		assertTrue("bundle should be Spring powered", config.isSpringPoweredBundle());
		assertEquals("1 config files", 1, configFiles.length);
		assertEquals(OsgiBundleXmlApplicationContext.DEFAULT_CONFIG_LOCATION, configFiles[0]);
	}

	public void tstEmptyConfigLocationsInMetaInf() throws Exception {
		System.out.println("tsst");
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", ";wait-for-dependencies:=false");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		aBundle.setEntryReturnOnNextCallToGetEntry(new URL(META_INF_SPRING_CONTENT[0]));
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		String[] configFiles = config.getConfigurationLocations();
		assertTrue("bundle should be Spring powered", config.isSpringPoweredBundle());
		assertEquals("1 config files", 1, configFiles.length);
		assertEquals(OsgiBundleXmlApplicationContext.DEFAULT_CONFIG_LOCATION, configFiles[0]);
	}

	public void tstConfigLocationsInMetaInfWithHeaderAndDependencies() throws Exception {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "META-INF/spring/context.xml;wait-for-dependencies:=false");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		aBundle.setEntryReturnOnNextCallToGetEntry(new URL(META_INF_SPRING_CONTENT[0]));
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		String[] configFiles = config.getConfigurationLocations();
		assertTrue("bundle should be Spring powered", config.isSpringPoweredBundle());
		assertEquals("2 config files", 1, configFiles.length);
		assertEquals("osgibundle:META-INF/spring/context.xml", configFiles[0]);
	}

	public void tstBundleWithHeaderWithBadEntriesAndNoMetaInfResourcesIsNotSpringPowered() {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "META-INF/splurge/context.xml");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(null);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertFalse("bundle is not spring powered", config.isSpringPoweredBundle());
	}

	public void tstHeaderWithWildcardEntryAndNoMetaInfResources() {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "*;wait-for-dependencies:=false");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(null);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertFalse("not spring powered", config.isSpringPoweredBundle());
	}

	public void tstHeaderWithBadEntry() throws Exception {
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "META-INF/spring/context-two.xml,META-INF/splurge/context.xml,");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setEntryReturnOnNextCallToGetEntry(new URL("file://META-INF/spring/context-two.xml"));
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertFalse("bundle is not spring powered", config.isSpringPoweredBundle());
		String[] configFiles = config.getConfigurationLocations();
		assertEquals("0 config file", 0, configFiles.length);
	}

	public void testCreateAsynchronouslyDefaultTrue() throws Exception {
		// *;flavour
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "*;timeout:=none");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		aBundle.setEntryReturnOnNextCallToGetEntry(new URL(META_INF_SPRING_CONTENT[0]));
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle should be Spring powered", config.isSpringPoweredBundle());
		assertTrue("bundle should have create-asynchronously = true", config.isCreateAsynchronously());
	}

	public void testSetCreateAsynchronouslyTrue() {
		// *;flavour
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "*;create-asynchronously:=true");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle should be Spring powered", config.isSpringPoweredBundle());
		assertTrue("bundle should have create-asynchronously = true", config.isCreateAsynchronously());
	}

	public void testSetCreateAsynchronouslyFalse() throws Exception {
		// *;flavour
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "META-INF/spring/context.xml;create-asynchronously:=false");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		aBundle.setEntryReturnOnNextCallToGetEntry(new URL(META_INF_SPRING_CONTENT[0]));
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle should be Spring powered", config.isSpringPoweredBundle());
		assertFalse("bundle should have create-asynchronously = false", config.isCreateAsynchronously());
	}

	public void testCreateAsynchronouslyDefaultTrueIfAbsent() {
		// *;flavour
		Dictionary headers = new Hashtable();
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle should have create-asynchronously = true", config.isCreateAsynchronously());
	}

	public void testCreateAsynchronouslyDefaultTrueIfGarbage() {
		// *;flavour
		Dictionary headers = new Hashtable();
		headers.put("Spring-Context", "*;favour:=false");
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setResultsToReturnOnNextCallToFindEntries(META_INF_SPRING_CONTENT);
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(aBundle);
		assertTrue("bundle should have create-asynchronously = true", config.isCreateAsynchronously());
	}

	private static class RepeatingEntryLookupControllingMockBundle extends EntryLookupControllingMockBundle {
		protected String[] findResult;

		public RepeatingEntryLookupControllingMockBundle(Dictionary headers) {
			super(headers);
		}

		public Enumeration findEntries(String path, String filePattern, boolean recurse) {
			if (this.nextFindResult == null) {
				return super.findEntries(path, filePattern, recurse);
			}
			else {
				Enumeration r = this.nextFindResult;
				this.nextFindResult = createEnumerationOver(findResult);
				return r;
			}
		}

		public void setResultsToReturnOnNextCallToFindEntries(String[] r) {
			findResult = r;
			if (findResult == null) {
				findResult = new String[0];
			}
			this.nextFindResult = createEnumerationOver(findResult);
		}
	}
}
