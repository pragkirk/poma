
package org.springframework.osgi.iandt.referenceProxy;

import java.util.List;
import java.util.PropertyPermission;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.iandt.reference.proxy.ServiceReferer;
import org.springframework.osgi.iandt.simpleservice.MyService;
import org.springframework.osgi.service.ServiceUnavailableException;
import org.springframework.osgi.util.OsgiBundleUtils;

/**
 * @author Hal Hildebrand Date: Nov 25, 2006 Time: 12:42:30 PM
 */
public class ReferenceProxyTest extends BaseIntegrationTest {

	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt, simple.service," + getSpringDMVersion(),
			"org.springframework.osgi.iandt, reference.proxy," + getSpringDMVersion() };
	}

	public void testReferenceProxyLifecycle() throws Exception {

		MyService reference = ServiceReferer.serviceReference;

		assertNotNull("reference not initialized", reference);
		assertNotNull("no value specified in the reference", reference.stringValue());

		Bundle simpleServiceBundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext,
			"org.springframework.osgi.iandt.simpleservice");

		assertNotNull("Cannot find the simple service bundle", simpleServiceBundle);
		System.out.println("stopping bundle");
		simpleServiceBundle.stop();

		while (simpleServiceBundle.getState() == Bundle.STOPPING) {
			System.out.println("waiting for bundle to stop");
			Thread.sleep(10);
		}
		System.out.println("bundle stopped");

		// Service should be unavailable
		try {
			reference.stringValue();
			fail("ServiceUnavailableException should have been thrown!");
		}
		catch (ServiceUnavailableException e) {
			// Expected
		}

		System.out.println("starting bundle");
		simpleServiceBundle.start();

		waitOnContextCreation("org.springframework.osgi.iandt.simpleservice");

		System.out.println("bundle started");
		// Service should be running
		assertNotNull(reference.stringValue());
	}

	protected long getDefaultWaitTime() {
		return 15L;
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		return perms;
	}

	protected List getIAndTPermissions() {
		List perms = super.getIAndTPermissions();
		perms.add(new PropertyPermission("*", "read"));
		perms.add(new PropertyPermission("*", "write"));
		return perms;
	}

}
