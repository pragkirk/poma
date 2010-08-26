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

package org.springframework.osgi.iandt.deadlocks;

import java.io.FilePermission;
import java.util.List;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.osgi.iandt.BaseIntegrationTest;

/**
 * @author Hal Hildebrand Date: Jun 5, 2007 Time: 9:10:11 PM
 */
public class DeadlockHandlingTest extends BaseIntegrationTest {

	// Specifically do not wait
	protected boolean shouldWaitForSpringBundlesContextCreation() {
		return false;
	}

	/**
	 * While it may appear that this test is doing nothing, what it is doing is
	 * testing what happens when the OSGi framework is shutdown while the
	 * Spring/OSGi extender is deadlocked. If all goes well, the test will
	 * gracefully end. If not, it will hang for quite a while.
	 */
	public void testErrorHandling() throws Exception {
		Resource errorResource = getLocator().locateArtifact("org.springframework.osgi.iandt", "deadlock",
			getSpringDMVersion());
		assertNotNull("bundle resource exists", errorResource);
		Bundle errorBundle = bundleContext.installBundle(errorResource.getURL().toExternalForm());
		assertNotNull("bundle exists", errorBundle);
		errorBundle.start();
		StringBuffer filter = new StringBuffer();

		filter.append("(&");
		filter.append("(").append(Constants.OBJECTCLASS).append("=").append(
			AbstractRefreshableApplicationContext.class.getName()).append(")");
		filter.append("(").append(ConfigurableOsgiBundleApplicationContext.APPLICATION_CONTEXT_SERVICE_PROPERTY_NAME);
		filter.append("=").append("org.springframework.osgi.iandt.deadlock").append(")");
		filter.append(")");
		ServiceTracker tracker = new ServiceTracker(bundleContext, bundleContext.createFilter(filter.toString()), null);

		try {
			tracker.open();

			AbstractRefreshableApplicationContext appContext = (AbstractRefreshableApplicationContext) tracker.waitForService(3000);
			assertNull("Deadlock context should not be published", appContext);
		}
		finally {
			tracker.close();
		}
	}

	protected List getTestPermissions() {
		List list = super.getTestPermissions();
		list.add(new FilePermission("<<ALL FILES>>", "read"));
		list.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		list.add(new AdminPermission("*", AdminPermission.EXECUTE));
		list.add(new AdminPermission("*", AdminPermission.RESOLVE));
		return list;
	}
}