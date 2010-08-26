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

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.iandt.simpleservice.MyService;
import org.springframework.osgi.iandt.simpleservice2.MyService2;
import org.springframework.osgi.iandt.tccl.TCCLService;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.util.Assert;

/**
 * Integration test with multiple imports that are hooked inside the same
 * exporter.
 * 
 * @author Costin Leau
 * 
 */
public class SingleExportDependingOnMultipleImportTest extends BaseIntegrationTest {

	private static final String TCCL_SYM_NAME = "org.springframework.osgi.iandt.tccl";

	private static final String SERVICE_SYM_NAME = "org.springframework.osgi.iandt.simpleservice";

	private static final String SERVICE_2_SYM_NAME = "org.springframework.osgi.iandt.simpleservice2";


	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/osgi/iandt/servicedependency/single-export-multi-import.xml" };
	}

	protected String[] getTestBundlesNames() {
		// load the tccl bundle, plus simple.service + simple.service.2
		return new String[] { "org.springframework.osgi.iandt, tccl," + getSpringDMVersion(),
			"org.springframework.osgi.iandt, simple.service," + getSpringDMVersion(),
			"org.springframework.osgi.iandt, simple.service2," + getSpringDMVersion() };
	}

	public void testOneImporterCGoesDownAndThenUpAgain() throws Exception {
		// importer C = TCCL bundle
		// check exporter
		assertTrue("exporter should be running", isExporterStarted());

		takeDownServiceC();
		assertFalse("serviceC should take exporter down", isExporterStarted());
		putUpServiceC();
		// check exporter
		assertTrue("service C is up again, exporter should be up as well", isExporterStarted());
	}

	public void testImporterAGoesDownThenImporterBThenImporterAComesUpAgainAndThenB() throws Exception {
		// check exporter
		assertTrue("exporter should be running", isExporterStarted());

		// start with A

		// take down A
		takeDownServiceA();
		// check exporter
		assertFalse("serviceA should take exporter down", isExporterStarted());

		// then B

		// take down B
		takeDownServiceB();
		// check exporter
		assertFalse("serviceB should keep exporter down", isExporterStarted());

		// put up A
		putUpServiceA();
		// check exporter
		assertFalse("serviceB is still down so the exporter should be down as well", isExporterStarted());
		// put up B
		putUpServiceB();
		// check exporter
		assertTrue("service A + B up, exporter should be up as well", isExporterStarted());
	}

	public void testImportersGoDownOneAfterTheOtherAndTheExporterDoesNotStartUntilAllImportersAreUp() throws Exception {
		// check exporter
		assertTrue("exporter should be running", isExporterStarted());

		// take down A
		takeDownServiceA();
		assertFalse("serviceA should take exporter down", isExporterStarted());

		takeDownServiceB();
		// check exporter
		assertFalse("serviceB should keep exporter down", isExporterStarted());

		takeDownServiceC();
		// check exporter
		assertFalse("serviceB should keep exporter down", isExporterStarted());

		putUpServiceA();
		// check exporter
		assertFalse("serviceB,C are still down so the exporter should be down as well", isExporterStarted());

		putUpServiceC();
		assertFalse("serviceB is still down so the exporter should be down as well", isExporterStarted());

		// put up B
		putUpServiceB();
		// check exporter
		assertTrue("service A + B + C up, exporter should be up as well", isExporterStarted());
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

	private void takeDownServiceB() throws Exception {
		checkAndTakeDownService("serviceB", MyService2.class, SERVICE_2_SYM_NAME);
	}

	private void putUpServiceB() throws Exception {
		putUpService(SERVICE_2_SYM_NAME);
	}

	private void takeDownServiceC() throws Exception {
		checkAndTakeDownService("serviceC", TCCLService.class, TCCL_SYM_NAME);
	}

	private void putUpServiceC() throws Exception {
		putUpService(TCCL_SYM_NAME);
	}

	private boolean isExporterStarted() throws Exception {
		return (bundleContext.getServiceReference(SimpleBean.class.getName()) != null);
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		return perms;
	}
}
