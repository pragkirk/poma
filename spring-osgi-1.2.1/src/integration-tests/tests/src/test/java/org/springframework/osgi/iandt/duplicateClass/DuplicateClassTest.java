
package org.springframework.osgi.iandt.duplicateClass;

import java.io.FilePermission;
import java.util.List;
import java.util.PropertyPermission;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.iandt.simpleservice.MyService;
import org.springframework.osgi.util.OsgiStringUtils;

/**
 * Test which installs twice a bundle with the same symbolic name but with
 * different classes to check that proper reloading occurs.
 * 
 * @author Andy Piper
 */
public class DuplicateClassTest extends BaseIntegrationTest {

	private static final String DEPENDENT_CLASS_NAME = "org.springframework.osgi.iandt.simpleservice.MyService";


	protected String getManifestLocation() {
		return "classpath:org/springframework/osgi/iandt/duplicateClass/DuplicateClassTest.MF";
	}

	public void testDependencies() throws Exception {

		// Simple Service bundle (provides the base package + 1 service)
		Bundle simpleServiceBundle = bundleContext.installBundle(getLocator().locateArtifact(
			"org.springframework.osgi.iandt", "simple.service", getSpringDMVersion()).getURL().toExternalForm());
		assertNotNull("Cannot find the simple service bundle", simpleServiceBundle);

		assertNotSame("simple service bundle is in the activated state!", new Integer(Bundle.ACTIVE), new Integer(
			simpleServiceBundle.getState()));
		startDependency(simpleServiceBundle);

		// Identical Simple Service bundle (+1 service)
		Bundle simpleServiceDuplicateBundle = bundleContext.installBundle(getLocator().locateArtifact(
			"org.springframework.osgi.iandt", "simple.service.identical", getSpringDMVersion()).getURL().toExternalForm());
		assertNotNull("Cannot find the simple service duplicate bundle", simpleServiceDuplicateBundle);
		assertNotSame("simple service 2 bundle is in the activated state!", new Integer(Bundle.ACTIVE), new Integer(
			simpleServiceDuplicateBundle.getState()));
		startDependency(simpleServiceDuplicateBundle);

		ServiceReference[] refs = bundleContext.getServiceReferences(DEPENDENT_CLASS_NAME, null);

		assertEquals(2, refs.length);

		MyService service1 = (MyService) bundleContext.getService(refs[0]);
		MyService service2 = (MyService) bundleContext.getService(refs[1]);

		assertNotNull(service1);
		assertNotNull(service2);

		String msg1 = service1.stringValue();
		String msg2 = service2.stringValue();

		String jmsg = "Bond.  James Bond.";
		String cmsg = "Connery.  Sean Connery #1";
		System.out.println(msg1);
		System.out.println(msg2);
		assertNotSame(msg1, msg2);
		assertTrue(msg1.equals(jmsg) || msg1.equals(cmsg));
		assertTrue(msg2.equals(jmsg) || msg2.equals(cmsg));

		bundleContext.ungetService(refs[0]);
		bundleContext.ungetService(refs[1]);

		// Uninstall duplicate 1
		simpleServiceDuplicateBundle.uninstall();
		// stop base bundle so that the package is still around but its service
		// is not
		simpleServiceBundle.stop();

		// Install something subtley different
		simpleServiceDuplicateBundle = bundleContext.installBundle(getLocator().locateArtifact(
			"org.springframework.osgi.iandt", "simple.service.2.identical", getSpringDMVersion()).getURL().toExternalForm());
		assertNotNull("Cannot find the simple service duplicate 2 bundle", simpleServiceDuplicateBundle);
		startDependency(simpleServiceDuplicateBundle);

		refs = bundleContext.getServiceReferences(DEPENDENT_CLASS_NAME, null);

		assertEquals(1, refs.length);

		service1 = (MyService) bundleContext.getService(refs[0]);

		assertNotNull(service1);

		msg1 = service1.stringValue();

		System.out.println(msg1);
		assertTrue(msg1.equals("Dalton.  Timothy Dalton #1"));
	}

	private void startDependency(Bundle bundle) throws BundleException, InterruptedException {
		bundle.start();
		waitOnContextCreation(bundle.getSymbolicName());
		System.out.println("started bundle [" + OsgiStringUtils.nullSafeSymbolicName(bundle) + "]");
	}

	protected boolean shouldWaitForSpringBundlesContextCreation() {
		return true;
	}

	protected long getDefaultWaitTime() {
		return 60L;
	}

	protected List getTestPermissions() {
		List list = super.getTestPermissions();
		list.add(new FilePermission("<<ALL FILES>>", "read"));
		list.add(new AdminPermission("*", AdminPermission.EXECUTE));
		list.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		list.add(new AdminPermission("*", AdminPermission.RESOLVE));
		list.add(new AdminPermission("*", AdminPermission.METADATA));
		return list;
	}

	protected List getIAndTPermissions() {
		List perms = super.getIAndTPermissions();
		perms.add(new PropertyPermission("*", "read"));
		perms.add(new PropertyPermission("*", "write"));
		return perms;
	}
}
