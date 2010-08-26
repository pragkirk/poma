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

package org.springframework.osgi.iandt.web.war;

import org.springframework.osgi.iandt.web.BaseWebIntegrationTest;
import org.springframework.osgi.iandt.web.HttpClient;
import org.springframework.osgi.iandt.web.HttpResponse;

/**
 * Integration test for the simple servlet bundle.
 * 
 * @author Costin Leau
 * 
 */
public class StaticResourceTest extends BaseWebIntegrationTest {

	private final String GROUP_ID = "resources.only";


	protected void onSetUp() throws Exception {

	}

	protected String base() {
		return GROUP_ID + "-" + getSpringDMVersion();
	}

	protected String[] getTestBundlesNames() {
		return new String[] { WEB_TESTS_GROUP + "," + GROUP_ID + "," + getSpringDMVersion() + ",war" };
	}

	public void testWarIndexPage() throws Exception {
		HttpResponse response = HttpClient.getLocalResponse(base(), "index.html");
		assertTrue(response.toString(), response.isOk());
	}

	public void testIndexRedirect() throws Exception {
		HttpResponse response = HttpClient.getLocalResponse(base(), "");
		assertTrue(response.toString(), response.isOk());
	}

	public void testOtherPage() throws Exception {
		HttpResponse response = HttpClient.getLocalResponse(base(), "other.html");
		assertTrue(response.toString(), response.isOk());
	}

	public void testNestedPage() throws Exception {
		HttpResponse response = HttpClient.getLocalResponse(base(), "nested/page.html");
		assertTrue(response.toString(), response.isOk());
	}

	public void testUnexistingNestedPage() throws Exception {
		HttpResponse response = HttpClient.getLocalResponse(base(), "nested/no-such-page.html");
		assertTrue(response.toString(), response.isNotFound());
	}
}
