
package org.springframework.osgi.iandt.referenceProxy;

import java.util.List;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.iandt.reference.proxy.ServiceReferer;
import org.springframework.osgi.iandt.simpleservice.MyService;
import org.springframework.osgi.service.importer.ServiceProxyDestroyedException;
import org.springframework.osgi.util.OsgiBundleUtils;

/**
 * @author Glyn Normington
 */
public class ProxyDestructionTest extends BaseIntegrationTest {

	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt, simple.service," + getSpringDMVersion(),
			"org.springframework.osgi.iandt, proxy.destruction," + getSpringDMVersion() };
	}

	/**
	 * Install the bundles, stop the one provinding the service and then the one
	 * consuming the service, *after*, an invocation has been made. The test
	 * checks that shutting down the application context, causes all proxies
	 * waiting to be destroyed properly.
	 * 
	 * @throws Exception
	 */
	public void testProxyDestruction() throws Exception {

		MyService reference = ServiceReferer.serviceReference;

		assertNotNull("reference not initialized", reference);
		assertNotNull("no value specified in the reference", reference.stringValue());

		Bundle simpleServiceBundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext,
			"org.springframework.osgi.iandt.simpleservice");

		assertNotNull("Cannot find the simple service bundle", simpleServiceBundle);
		System.out.println("stopping service bundle");
		simpleServiceBundle.stop();

		while (simpleServiceBundle.getState() == Bundle.STOPPING) {
			System.out.println("waiting for service bundle to stop...");
			Thread.sleep(500);
		}
		System.out.println("service bundle stopped");

		final Bundle proxyDestructionBundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext,
			"org.springframework.osgi.iandt.proxy.destruction");

		Thread t = new Thread() {

			public void run() {
				try {
					// wait a bit so the proxy invocation executes
					Thread.sleep(3000);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					System.out.println("Stopping referring bundle...");
					proxyDestructionBundle.stop();
				}
				catch (BundleException e) {
					e.printStackTrace();
				}
			}

		};
		t.start();

		// Service should be unavailable
		try {
			System.out.println("Invoking method on OSGi service proxy...");
			reference.stringValue();
			fail("ServiceProxyDestroyedException should have been thrown!");
		}
		catch (ServiceProxyDestroyedException e) {
			// Expected
		}
	}

	protected long getDefaultWaitTime() {
		return 60L;
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		return perms;
	}
}
