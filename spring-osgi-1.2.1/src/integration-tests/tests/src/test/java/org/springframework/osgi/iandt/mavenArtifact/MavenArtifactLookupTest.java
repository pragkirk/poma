
package org.springframework.osgi.iandt.mavenArtifact;

import java.io.FilePermission;
import java.util.List;
import java.util.PropertyPermission;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.iandt.cardinality0to1.test.MyListener;
import org.springframework.osgi.iandt.cardinality0to1.test.ReferenceContainer;
import org.springframework.osgi.service.ServiceUnavailableException;

/**
 * @author Hal Hildebrand
 *         Date: Mar 5, 2007
 *         Time: 6:00:39 PM
 */

/**
 * This test ensures that Maven artifact lookup is maintained. Note that all
 * Maven artifact lookups are explicity using the type of the artifact - a
 * property of the artifact resolution that we need to preserve.
 */
public class MavenArtifactLookupTest extends BaseIntegrationTest {

	protected String getManifestLocation() {
		// return
		// "classpath:org/springframework/osgi/test/mavenArtifact/MavenArtifactLookupTest.MF";
		return null;
	}

	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt, simple.service," + getSpringDMVersion(),
			"org.springframework.osgi.iandt, cardinality0to1," + getSpringDMVersion() };
	}

	public void test0to1Cardinality() throws Exception {

		Bundle simpleService2Bundle = bundleContext.installBundle(getLocator().locateArtifact(
			"org.springframework.osgi.iandt", "simple.service2", getSpringDMVersion(), "jar").getURL().toExternalForm());

		assertNotNull("Cannot find the simple service 2 bundle", simpleService2Bundle);

		assertNotSame("simple service 2 bundle is in the activated state!", new Integer(Bundle.ACTIVE), new Integer(
			simpleService2Bundle.getState()));

		assertEquals("Unxpected initial binding of service", 0, MyListener.BOUND_COUNT);
		assertEquals("Unexpected initial unbinding of service", 0, MyListener.UNBOUND_COUNT);
		assertNotNull("Service reference should be not null", ReferenceContainer.service);

		try {
			ReferenceContainer.service.stringValue();
			fail("Service should be unavailable");
		}
		catch (ServiceUnavailableException e) {
			// expected
		}

		startDependency(simpleService2Bundle);

		assertEquals("Expected initial binding of service", 1, MyListener.BOUND_COUNT);
		assertEquals("Unexpected initial unbinding of service", 0, MyListener.UNBOUND_COUNT);
		assertNotNull("Service reference should be not null", ReferenceContainer.service);

		assertNotNull(ReferenceContainer.service.stringValue());

	}

	private void startDependency(Bundle simpleService2Bundle) throws BundleException, InterruptedException {
		System.out.println("Starting dependency");
		simpleService2Bundle.start();

		waitOnContextCreation("org.springframework.osgi.iandt.simpleservice2");

		System.out.println("Dependency started");
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new FilePermission("<<ALL FILES>>", "read"));
		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		perms.add(new AdminPermission("*", AdminPermission.RESOLVE));
		return perms;
	}

	protected List getIAndTPermissions() {
		List perms = super.getIAndTPermissions();
		perms.add(new PropertyPermission("*", "read"));
		perms.add(new PropertyPermission("*", "write"));
		return perms;
	}
}
