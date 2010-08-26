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

import java.io.FilePermission;
import java.util.List;
import java.util.Map;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.iandt.simpleservice.MyService;
import org.springframework.osgi.iandt.tccl.TCCLService;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.util.Assert;

/**
 * @author Costin Leau
 * 
 */
public class CollectionImporterTest extends BaseIntegrationTest {

	private static final String TCCL_SYM_NAME = "org.springframework.osgi.iandt.tccl";

	private static final String SERVICE_SYM_NAME = "org.springframework.osgi.iandt.simpleservice";


	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/osgi/iandt/servicedependency/multi-export-multi-collection-import.xml" };
	}

	protected String[] getTestBundlesNames() {
		// load the tccl bundle, plus simple.service
		return new String[] { "org.springframework.osgi.iandt, tccl," + getSpringDMVersion(),
			"org.springframework.osgi.iandt, simple.service," + getSpringDMVersion() };
	}

	public void testExporterAWhenImporterAGoesDownAndUp() throws Exception {
		assertTrue("exporterA should be running", isExporterAStarted());
		logger.info("Taking down serviceA...");
		takeDownServiceA();
		assertFalse("serviceA should take exporterA down", isExporterAStarted());
		logger.info("Putting up serviceA...");
		putUpServiceA();
		// check exporter
		assertTrue("serviceA is up again, so should exporterA", isExporterAStarted());
	}

	public void testExporterBWhenImporterAGoesDownAndUp() throws Exception {
		assertTrue("exporterB should be running", isExporterBStarted());
		logger.info("Taking down serviceA...");
		takeDownServiceA();
		assertFalse("serviceA should take exporterB down", isExporterBStarted());
		logger.info("Putting up serviceA...");
		putUpServiceA();
		// check exporter
		assertTrue("service A is up again, so should exporterB", isExporterBStarted());
	}

	public void testExporterBWhenImporterAGoesDownThenImporterBThenBothUpAgain() throws Exception {
		assertTrue("exporterB should be running", isExporterBStarted());

		takeDownServiceA();
		assertFalse("serviceA should take exporterB down", isExporterBStarted());

		// take down B
		takeDownServiceC();
		// check exporter
		assertFalse("serviceC down should keep exporterB down", isExporterBStarted());

		putUpServiceA();
		// check exporter
		assertFalse("service C is still down and so should be exporterB", isExporterBStarted());
		putUpServiceC();
		// check exporter
		assertTrue("service A,B up -> exporterB up", isExporterBStarted());
	}

	private void checkAndTakeDownService(String beanName, Class type, String bundleSymName) throws Exception {
		ServiceReference ref = bundleContext.getServiceReference(type.getName());
		Object service = bundleContext.getService(ref);
		Assert.isInstanceOf(type, service);

		Bundle dependency = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, bundleSymName);
		// stop dependency bundle -> no importer -> exporter goes down
		dependency.stop();
	}

	private void putUpService(String bundleSymName) throws Exception {
		Bundle dependency = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, bundleSymName);
		dependency.start();
		waitOnContextCreation(bundleSymName);
	}

	private void takeDownServiceA() throws Exception {
		checkAndTakeDownService("serviceA", MyService.class, SERVICE_SYM_NAME);
	}

	private void putUpServiceA() throws Exception {
		putUpService(SERVICE_SYM_NAME);
	}

	private void takeDownServiceC() throws Exception {
		checkAndTakeDownService("serviceC", TCCLService.class, TCCL_SYM_NAME);
	}

	private void putUpServiceC() throws Exception {
		putUpService(TCCL_SYM_NAME);
	}

	private boolean isExporterBStarted() throws Exception {
		return (bundleContext.getServiceReference(SimpleBean.class.getName()) != null);
	}

	private boolean isExporterAStarted() throws Exception {
		return (bundleContext.getServiceReference(Map.class.getName()) != null);
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		return perms;
	}
}
