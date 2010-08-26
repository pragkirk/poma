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

package org.springframework.osgi.iandt.servicedependency;

import java.util.List;
import java.util.Map;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.iandt.tccl.TCCLService;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.util.Assert;

/**
 * Integration test for the dependency between exporters and importers. Tests
 * multiple exports relying on the same importer.
 * 
 * @author Costin Leau
 * 
 */
public class MultipleExportersDependingOnOneImporterTest extends BaseIntegrationTest {

	private static final String DEP_SYN_NAME = "org.springframework.osgi.iandt.tccl";


	protected String[] getTestBundlesNames() {
		// load the tccl bundle as it exposes a simple service
		return new String[] { "org.springframework.osgi.iandt,tccl," + getSpringDMVersion() };
	}

	protected synchronized String[] getConfigLocations() {
		// trigger loading of TCCLService
		if (TCCLService.class != null) {
			this.notify();
		}

		return new String[] { "org/springframework/osgi/iandt/servicedependency/multi-export-single-import.xml" };

	}

	// test map-exporter
	public void testDirectExporterImporterDependency() throws Exception {
		doServiceTestOn("map", Map.class);
	}

	// test simple-bean
	public void testTransitiveExporterImporterDependency() throws Exception {
		doServiceTestOn("simple-bean", SimpleBean.class);
	}

	protected void doServiceTestOn(String beanName, Class type) throws Exception {
		ServiceReference ref = bundleContext.getServiceReference(type.getName());
		Object service = bundleContext.getService(ref);
		Assert.isInstanceOf(type, service);

		assertSame(applicationContext.getBean(beanName), service);
		Bundle dependency = getDependencyBundle();
		// stop bundle (and thus the exposed service)
		dependency.stop();
		// check if map is still published
		assertNull("exported for " + beanName + " should have been unpublished", ref.getBundle());
		// double check the service space
		assertNull(beanName + " service should be unregistered", bundleContext.getServiceReference(type.getName()));

		dependency.start();
		waitOnContextCreation(DEP_SYN_NAME);

		// the old reference remains invalid
		assertNull("the reference should remain invalid", ref.getBundle());
		// but the service should be back again
		assertSame(applicationContext.getBean(beanName), service);
	}

	public void testTwoExportersWithTheSameImporter() throws Exception {
		// check that both exporters go down one mandatory goes down
		ServiceReference exporterARef = bundleContext.getServiceReference(Map.class.getName());
		ServiceReference exporterBRef = bundleContext.getServiceReference(SimpleBean.class.getName());

		Bundle dependency = getDependencyBundle();
		dependency.stop();

		// check if services are still published
		assertNull("exportedA should have been unpublished", exporterARef.getBundle());
		assertNull("exportedB should have been unpublished", exporterBRef.getBundle());

		// double check the service space
		assertNull("exporterA service should be unregistered", bundleContext.getServiceReference(Map.class.getName()));
		assertNull("exporterB service should be unregistered",
			bundleContext.getServiceReference(SimpleBean.class.getName()));

		dependency.start();
		waitOnContextCreation(DEP_SYN_NAME);

		// the old reference remains invalid
		assertNull("the reference should remain invalid", exporterARef.getBundle());
		assertNull("the reference should remain invalid", exporterBRef.getBundle());

		assertNotNull(bundleContext.getServiceReference(Map.class.getName()));
		assertNotNull(bundleContext.getServiceReference(SimpleBean.class.getName()));
	}

	protected Bundle getDependencyBundle() {
		return OsgiBundleUtils.findBundleBySymbolicName(bundleContext, DEP_SYN_NAME);
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		return perms;
	}
}
