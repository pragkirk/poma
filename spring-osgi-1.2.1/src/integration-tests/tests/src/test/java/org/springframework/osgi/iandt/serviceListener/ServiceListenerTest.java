
package org.springframework.osgi.iandt.serviceListener;

import java.util.ArrayList;
import java.util.List;
import java.util.PropertyPermission;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.iandt.service.listener.MyListener;
import org.springframework.osgi.util.OsgiBundleUtils;

/**
 * @author Hal Hildebrand Date: Nov 14, 2006 Time: 8:18:15 AM
 */
public class ServiceListenerTest extends BaseIntegrationTest {

	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt,simple.service," + getSpringDMVersion(),
			"org.springframework.osgi.iandt, service.listener," + getSpringDMVersion() };
	}

	public void testServiceListener() throws Exception {
		assertEquals("Expected initial binding of service", 1, MyListener.BOUND_COUNT);
		assertEquals("Unexpected initial unbinding of service", 0, MyListener.UNBOUND_COUNT);

		Bundle simpleServiceBundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext,
			"org.springframework.osgi.iandt.simpleservice");

		assertNotNull("Cannot find the simple service bundle", simpleServiceBundle);

		simpleServiceBundle.stop();
		while (simpleServiceBundle.getState() == Bundle.STOPPING) {
			Thread.sleep(100);
		}

		assertEquals("Expected one binding of service", 1, MyListener.BOUND_COUNT);
		assertTrue("Expected only one unbinding of service", MyListener.UNBOUND_COUNT < 2);
		assertEquals("Expected unbinding of service not seen", 1, MyListener.UNBOUND_COUNT);

		logger.debug("about to restart simple service");
		simpleServiceBundle.start();
		waitOnContextCreation("org.springframework.osgi.iandt.simpleservice");
		// wait some more to let the listener binding propagate
		Thread.sleep(1000);

		logger.debug("simple service succesfully restarted");
		assertTrue("Expected only two bindings of service", MyListener.BOUND_COUNT < 3);
		assertEquals("Expected binding of service not seen", 2, MyListener.BOUND_COUNT);
		assertEquals("Unexpected unbinding of service", 1, MyListener.UNBOUND_COUNT);
	}

	protected long getDefaultWaitTime() {
		return 10L;
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		return perms;
	}
}
