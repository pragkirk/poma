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

package org.springframework.osgi.samples.simplewebapp;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.JdkVersion;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;

/**
 * Web integration test that bootstraps the web containers and its dependencies
 * and tests the Http connection to the local server at
 * <code>http://localhost:8080/simple-web-app/</code>.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiHttpIntegrationTest extends AbstractConfigurableBundleCreatorTests {

	private static final String SPRING_OSGI_GROUP = "org.springframework.osgi";


	/**
	 * {@inheritDoc}
	 * 
	 * <p/>Installs the required web bundles (such as Apache Tomcat) before
	 * running the integration test.
	 */
	protected String[] getTestBundlesNames() {
		List col = new ArrayList();

		// Servlet/JSP artifacts
		col.add("javax.servlet, com.springsource.javax.servlet, 2.4.0");
		col.add(SPRING_OSGI_GROUP + ", jsp-api.osgi, 2.0-SNAPSHOT");

		// JSP compiler
		col.add(SPRING_OSGI_GROUP + ", jasper.osgi, 5.5.23-SNAPSHOT");
		col.add(SPRING_OSGI_GROUP + ", commons-el.osgi, 1.0-SNAPSHOT");

		// standard tag library
		col.add("org.springframework.osgi, jstl.osgi, 1.1.2-SNAPSHOT");

		// add MX4J for 1.4
		// if < jdk 1.5, add an JMX implementation
		if (!JdkVersion.isAtLeastJava15())
			col.add("net.sourceforge.mx4j, com.springsource.mx4j, 3.0.2");

		col.add(SPRING_OSGI_GROUP + ", catalina.osgi, 5.5.23-SNAPSHOT");
		col.add(SPRING_OSGI_GROUP + ", catalina.start.osgi, 1.0.0");

		// Spring DM web extender
		col.add(SPRING_OSGI_GROUP + ", spring-osgi-web," + getSpringDMVersion());
		col.add(SPRING_OSGI_GROUP + ", spring-osgi-web-extender," + getSpringDMVersion());
		col.add("net.sourceforge.cglib, com.springsource.net.sf.cglib, 2.1.3");

		// the war
		col.add(SPRING_OSGI_GROUP + ".samples.simple-web-app, war, " + getSpringDMVersion() + ",war");
		return (String[]) col.toArray(new String[col.size()]);
	}

	private String url() {
		return "http://localhost:8080/simple-web-app";
	}

	private void testConnection(String address) throws Exception {
		URL url = new URL(address);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setUseCaches(false);
		try {
			con.connect();
			assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
		}
		finally {
			con.disconnect();
		}
	}

	public void testHtmlPage() throws Exception {
		// wait 10 seconds to make sure things are properly deployed
		Thread.sleep(10 * 1000);
		testConnection(url() + "/index.html");
	}

	public void testHelloServlet() throws Exception {
		testConnection(url() + "/helloWorldServlet");
	}

	public void testResourceServlet() throws Exception {
		testConnection(url() + "/resourceServlet?resource=/META-INF/MANIFEST.MF");
	}

	public void testJSP() throws Exception {
		testConnection(url() + "/hello-osgi-world.jsp");
	}

	//  Uncomment this method to stop the test from ending and manually connect to the browser
	//		public void testWarDeployed() throws Exception {
	//		System.in.read();
	//	}

	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return getPlatformName().indexOf("Felix") > -1;
	}
}