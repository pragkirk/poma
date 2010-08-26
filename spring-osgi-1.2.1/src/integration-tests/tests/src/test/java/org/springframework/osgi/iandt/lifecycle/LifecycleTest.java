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

package org.springframework.osgi.iandt.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.osgi.iandt.BaseIntegrationTest;

/**
 * @author Hal Hildebrand Date: Oct 15, 2006 Time: 5:51:36 PM
 */
public class LifecycleTest extends BaseIntegrationTest {

	protected String getManifestLocation() {
		return null;
	}

	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt,lifecycle," + getSpringDMVersion() };
	}

	public void testLifecycle() throws Exception {
		assertNotSame("Guinea pig has already been shutdown", "true",
			System.getProperty("org.springframework.osgi.iandt.lifecycle.GuineaPig.close"));

		assertEquals("Guinea pig didn't startup", "true",
			System.getProperty("org.springframework.osgi.iandt.lifecycle.GuineaPig.startUp"));
		Bundle[] bundles = bundleContext.getBundles();
		Bundle testBundle = null;
		for (int i = 0; i < bundles.length; i++) {
			if ("org.springframework.osgi.iandt.lifecycle".equals(bundles[i].getSymbolicName())) {
				testBundle = bundles[i];
				break;
			}
		}

		assertNotNull("Could not find the test bundle", testBundle);
		StringBuffer filter = new StringBuffer();
		filter.append("(&");
		filter.append("(").append(Constants.OBJECTCLASS).append("=").append(ApplicationContext.class.getName()).append(
			")");
		filter.append("(").append(ConfigurableOsgiBundleApplicationContext.APPLICATION_CONTEXT_SERVICE_PROPERTY_NAME);
		filter.append("=").append(testBundle.getSymbolicName()).append(")");
		filter.append(")");
		ServiceTracker tracker = new ServiceTracker(bundleContext, bundleContext.createFilter(filter.toString()), null);
		try {

			tracker.open();

			AbstractRefreshableApplicationContext appContext = (AbstractRefreshableApplicationContext) tracker.waitForService(30000);
			assertNotNull("test application context", appContext);
			assertTrue("application context is active", appContext.isActive());

			testBundle.stop();
			while (testBundle.getState() == Bundle.STOPPING) {
				Thread.sleep(10);
			}
			assertEquals("Guinea pig didn't shutdown", "true",
				System.getProperty("org.springframework.osgi.iandt.lifecycle.GuineaPig.close"));

			assertFalse("application context is inactive", appContext.isActive());
		}
		finally {
			tracker.close();
		}
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		return perms;
	}
}
