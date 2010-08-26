
package org.springframework.osgi.iandt.dependencyWithDelay;

import java.io.FilePermission;
import java.util.List;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.util.OsgiStringUtils;

/**
 * @author Hal Hildebrand Date: Aug 27, 2007 Time: 9:36:23 AM
 */

public class FactoryDependencyTest extends BaseIntegrationTest {

	private static final String DEPENDENT_CLASS_NAME = "org.springframework.osgi.iandt.dependencies.Dependent";

	private static final String DELAY_PROP = "org.springframework.osgi.iandt.dependencies.factory.delay";


	protected String getManifestLocation() {
		return null;
	}

	// dependency bundle - depends on service2, service3 and, through a nested
	// reference, to service1
	// simple.service2 - publishes service2
	// simple.service3 - publishes service3
	// simple - publishes service1
	public void testDependencies() throws Exception {
		System.setProperty(DELAY_PROP, "10000");

		Bundle dependencyTestBundle = bundleContext.installBundle(getLocator().locateArtifact(
			"org.springframework.osgi.iandt", "dependencies", getSpringDMVersion()).getURL().toExternalForm());

		Bundle simpleService2Bundle = bundleContext.installBundle(getLocator().locateArtifact(
			"org.springframework.osgi.iandt", "simple.service2", getSpringDMVersion()).getURL().toExternalForm());
		Bundle simpleService3Bundle = bundleContext.installBundle(getLocator().locateArtifact(
			"org.springframework.osgi.iandt", "simple.service3", getSpringDMVersion()).getURL().toExternalForm());
		Bundle factoryServiceBundle = bundleContext.installBundle(getLocator().locateArtifact(
			"org.springframework.osgi.iandt", "dependendencies.factory", getSpringDMVersion()).getURL().toExternalForm());

		assertNotNull("Cannot find the factory service bundle", factoryServiceBundle);
		assertNotNull("Cannot find the simple service 2 bundle", simpleService2Bundle);
		assertNotNull("Cannot find the simple service 3 bundle", simpleService3Bundle);
		assertNotNull("dependencyTest can't be resolved", dependencyTestBundle);

		assertNotSame("factory service bundle is in the activated state!", new Integer(Bundle.ACTIVE), new Integer(
			factoryServiceBundle.getState()));

		assertNotSame("simple service 2 bundle is in the activated state!", new Integer(Bundle.ACTIVE), new Integer(
			simpleService2Bundle.getState()));

		assertNotSame("simple service 3 bundle is in the activated state!", new Integer(Bundle.ACTIVE), new Integer(
			simpleService3Bundle.getState()));

		startDependencyAsynch(dependencyTestBundle);
		Thread.sleep(2000); // Yield to give bundle time to get into waiting
		// state.
		ServiceReference dependentRef = bundleContext.getServiceReference(DEPENDENT_CLASS_NAME);

		assertNull("Service with unsatisfied dependencies has been started!", dependentRef);

		startDependency(simpleService3Bundle);

		dependentRef = bundleContext.getServiceReference(DEPENDENT_CLASS_NAME);

		assertNull("Service with unsatisfied dependencies has been started!", dependentRef);

		startDependency(simpleService2Bundle);

		assertNull("Service with unsatisfied dependencies has been started!", dependentRef);

		dependentRef = bundleContext.getServiceReference(DEPENDENT_CLASS_NAME);

		startDependency(factoryServiceBundle);

		assertNull("Service with unsatisfied dependencies has been started!", dependentRef);

		waitOnContextCreation("org.springframework.osgi.iandt.dependencies");

		dependentRef = bundleContext.getServiceReference(DEPENDENT_CLASS_NAME);

		assertNotNull("Service has not been started!", dependentRef);

		Object dependent = bundleContext.getService(dependentRef);

		assertNotNull("Service is not available!", dependent);

	}

	private void startDependency(Bundle bundle) throws BundleException, InterruptedException {
		bundle.start();
		waitOnContextCreation(bundle.getSymbolicName());
		System.out.println("started bundle [" + OsgiStringUtils.nullSafeSymbolicName(bundle) + "]");
	}

	private void startDependencyAsynch(final Bundle bundle) {
		System.out.println("starting dependency test bundle");
		Runnable runnable = new Runnable() {

			public void run() {
				try {
					bundle.start();
					System.out.println("started dependency test bundle");
				}
				catch (BundleException ex) {
					System.err.println("can't start bundle " + ex);
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.setDaemon(false);
		thread.setName("dependency test bundle");
		thread.start();
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
		list.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		list.add(new AdminPermission("*", AdminPermission.EXECUTE));
		list.add(new AdminPermission("*", AdminPermission.RESOLVE));
		list.add(new AdminPermission("*", AdminPermission.METADATA));
		return list;
	}
}