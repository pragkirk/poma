/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.osgi.iandt.web;

import java.net.SocketPermission;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyPermission;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.core.JdkVersion;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.util.CollectionUtils;

public abstract class BaseWebIntegrationTest extends BaseIntegrationTest {

	private static final long WEB_APP_START_TIMEOUT = 10 * 1000;

	protected static final String WEB_TESTS_GROUP = "org.springframework.osgi.iandt.web";


	protected String[] getTestFrameworkBundlesNames() {
		String[] def = super.getTestFrameworkBundlesNames();
		List col = new ArrayList();
		CollectionUtils.mergeArrayIntoCollection(def, col);

		System.setProperty("DEBUG", "false");
		// set this property (to whatever value) to get logging in Jetty
		//System.setProperty("VERBOSE", "false");

		// Servlet/JSP artifacts
		col.add("javax.servlet, com.springsource.javax.servlet, 2.4.0");
		col.add("org.springframework.osgi, jsp-api.osgi, 2.0-SNAPSHOT");

		// JSP compiler
		col.add("org.springframework.osgi, jasper.osgi, 5.5.23-SNAPSHOT");
		col.add("org.springframework.osgi, commons-el.osgi, 1.0-SNAPSHOT");

		// standard tag library
		col.add("org.springframework.osgi, jstl.osgi, 1.1.2-SNAPSHOT");

		// add MX4J for 1.4
		// if < jdk 1.5, add an JMX implementation
		if (!JdkVersion.isAtLeastJava15())
			col.add("net.sourceforge.mx4j, com.springsource.mx4j, 3.0.2");

		col.add("org.springframework.osgi, catalina.osgi, 5.5.23-SNAPSHOT");
		col.add("org.springframework.osgi, catalina.start.osgi, 1.0.0");

		// jetty starter
		//		col.add("org.springframework.osgi, jetty.start.osgi, 1.0.0");
		//		col.add("org.springframework.osgi, jetty.web.extender.fragment.osgi, 1.0.0");
		//		col.add("org.mortbay.jetty, jetty-util, 6.1.9");
		//		col.add("org.mortbay.jetty, jetty, 6.1.9");

		// Spring DM web extender
		col.add("org.springframework.osgi, spring-osgi-web," + getSpringDMVersion());
		col.add("org.springframework.osgi, spring-osgi-web-extender," + getSpringDMVersion());
		col.add("net.sourceforge.cglib, com.springsource.net.sf.cglib, 2.1.3");

		return (String[]) col.toArray(new String[col.size()]);
	}

	protected String[] getBundleContentPattern() {
		String pkg = getClass().getPackage().getName().replace('.', '/');
		String basePackage = BaseWebIntegrationTest.class.getPackage().getName().replace('.', '/').concat("/");
		String[] patterns = new String[] { BaseIntegrationTest.class.getName().replace('.', '/').concat(".class"),
			basePackage + "/*", pkg + "/**/*" };
		return patterns;
	}

	// wait for the Jetty or Tomcat to start fully
	protected void postProcessBundleContext(BundleContext context) throws Exception {
		super.postProcessBundleContext(context);

		// create a filter for our server implementations
		Filter filter = FrameworkUtil.createFilter("(&(org.springframework.osgi.bean.name=*-server)("
				+ Constants.SERVICE_VENDOR + "=Spring Dynamic Modules))");

		ServiceTracker tracker = new ServiceTracker(context, filter, null);
		tracker.open();
		try {

			// wait for 7 seconds max
			Object server = tracker.waitForService(7 * 1000);
			if (server == null)
				throw new IllegalStateException("no web container server found");
		}
		finally {
			tracker.close();
		}

		// the server has been started, check if the app has been properly deployed
		// before that however, wait 2 secs to war to be unpacked and installed
		Thread.sleep(2 * 1000);
		logger.debug("Checking webapp deployed at " + base());
		long start = System.currentTimeMillis();
		long waited;

		HttpResponse resp;
		// wait until the page is up
		do {
			resp = HttpClient.getLocalResponse(base());
			waited = System.currentTimeMillis() - start;
			// wait 1 second
			if (!resp.isOk())
				Thread.sleep(2 * 1000);
		} while (waited < WEB_APP_START_TIMEOUT && !resp.isOk());
	}

	/**
	 * Returns the web context base. Used for checking if the application has
	 * been properly deployed in a given amount of time.
	 * 
	 * @return
	 */
	protected abstract String base();

	protected boolean createManifestOnlyFromTestClass() {
		return false;
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new PropertyPermission("*", "read"));
		perms.add(new PropertyPermission("*", "write"));
		// accept only local host
		perms.add(new SocketPermission("localhost:8080", "connect"));
		return perms;
	}
}