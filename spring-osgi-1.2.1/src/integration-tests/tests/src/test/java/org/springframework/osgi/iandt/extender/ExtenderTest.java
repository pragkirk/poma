
package org.springframework.osgi.iandt.extender;

import java.io.FilePermission;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.util.CollectionUtils;

/**
 * @author Hal Hildebrand Date: May 21, 2007 Time: 4:43:52 PM
 */
public class ExtenderTest extends BaseIntegrationTest {

	protected String getManifestLocation() {
		return null;
	}

	// Overridden to remove the spring extender bundle!
	protected String[] getTestFrameworkBundlesNames() {
		String[] bundles = super.getTestFrameworkBundlesNames();
		List list = new ArrayList(bundles.length);

		// remove extender
		CollectionUtils.mergeArrayIntoCollection(bundles, list);
		// additionally remove the annotation bundle as well (if included)

		int bundlesFound = 0;
		for (Iterator iter = list.iterator(); (iter.hasNext() && (bundlesFound < 2));) {
			String element = (String) iter.next();
			if (element.indexOf("extender") >= 0 || element.indexOf("osgi-annotation") >= 0) {
				iter.remove();
				bundlesFound++;
			}
		}

		return (String[]) list.toArray(new String[list.size()]);
	}

	// Specifically cannot wait - test scenario has bundles which are spring
	// powered, but will not be started.
	protected boolean shouldWaitForSpringBundlesContextCreation() {
		return false;
	}

	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt, lifecycle," + getSpringDMVersion() };
	}

	public void testLifecycle() throws Exception {
		assertNull("Guinea pig has already been started",
			System.getProperty("org.springframework.osgi.iandt.lifecycle.GuineaPig.close"));

		StringBuffer filter = new StringBuffer();
		filter.append("(&");
		filter.append("(").append(Constants.OBJECTCLASS).append("=").append(ApplicationContext.class.getName()).append(
			")");
		filter.append("(").append("org.springframework.context.service.name");
		filter.append("=").append("org.springframework.osgi.iandt.lifecycle").append(")");
		filter.append(")");
		ServiceTracker tracker = new ServiceTracker(bundleContext, bundleContext.createFilter(filter.toString()), null);
		tracker.open();

		ApplicationContext appContext = (ApplicationContext) tracker.waitForService(1);

		assertNull("lifecycle application context does not exist", appContext);

		Resource extenderResource = getLocator().locateArtifact("org.springframework.osgi", "spring-osgi-extender",
			getSpringDMVersion());
		assertNotNull("Extender bundle resource", extenderResource);
		Bundle extenderBundle = bundleContext.installBundle(extenderResource.getURL().toExternalForm());
		assertNotNull("Extender bundle", extenderBundle);

		extenderBundle.start();

		tracker.open();

		appContext = (ApplicationContext) tracker.waitForService(60000);

		assertNotNull("lifecycle application context exists", appContext);

		assertNotSame("Guinea pig hasn't already been shutdown", "true",
			System.getProperty("org.springframework.osgi.iandt.lifecycle.GuineaPig.close"));

		assertEquals("Guinea pig started up", "true",
			System.getProperty("org.springframework.osgi.iandt.lifecycle.GuineaPig.startUp"));

	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		perms.add(new AdminPermission("*", AdminPermission.RESOLVE));
		perms.add(new FilePermission("<<ALL FILES>>", "read"));
		return perms;
	}
}
