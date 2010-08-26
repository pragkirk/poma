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

package org.springframework.osgi.iandt.web.taglib;

import org.springframework.osgi.iandt.web.BaseWebIntegrationTest;
import org.springframework.osgi.iandt.web.HttpClient;
import org.springframework.osgi.iandt.web.HttpResponse;

/**
 * @author Costin Leau
 * 
 */
public class CoreTaglibTest extends BaseWebIntegrationTest {

	private final String GROUP_ID = "taglib.jsp";


	protected String base() {
		return GROUP_ID + "-" + getSpringDMVersion();
	}

	protected String[] getTestBundlesNames() {
		return new String[] { WEB_TESTS_GROUP + "," + GROUP_ID + "," + getSpringDMVersion() + ",war" };
	}

	public void testIndexPage() throws Exception {
		HttpResponse resp = HttpClient.getLocalResponse(base(), "index.html");
		assertTrue(resp.toString(), resp.isOk());
	}

	public void testCoreTaglibJSP() throws Exception {
		HttpResponse resp = HttpClient.getLocalResponse(base(), "jsp/standard-core-taglib.jsp");
		assertTrue(resp.toString(), resp.isOk());
	}

	public void testXmlTaglibJSP() throws Exception {
		HttpResponse resp = HttpClient.getLocalResponse(base(), "jsp/standard-xml-taglib.jsp");
		assertTrue(resp.toString(), resp.isOk());
	}

	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return getPlatformName().indexOf("Felix") > -1;
	}

	//	public void tstSystemIn() throws Exception {
	//		System.in.read();
	//	}
}
