/*
 * Copyright 2006 the original author or authors.
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
package org.springframework.osgi.samples.simpleservice.test;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.samples.simpleservice.MyService;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
import org.springframework.osgi.test.platform.OsgiPlatform;

/**
 * Starts up an OSGi environment (equinox, knopflerfish, or 
 * felix according to the profile selected) and installs the
 * simple service bundle and the bundles it depends on. The test
 * classes in this project will be turned into a virtual bundle 
 * which is also installed and the tests are then run inside the
 * OSGi runtime.
 * 
 * The tests have access to a BundleContext, which we use to 
 * test that the simpleService bean was indeed published as 
 * an OSGi service.
 * 
 * @author Adrian Colyer
 */
public class SimpleServiceBundleTest extends AbstractConfigurableBundleCreatorTests {

	/**
	 * The manifest to use for the "virtual bundle" created
	 * out of the test classes and resources in this project
	 * 
	 * This is actually the boilerplate manifest with one additional
	 * import-package added. We should provide a simpler customization
	 * point for such use cases that doesn't require duplication
	 * of the entire manifest...
	 */
	protected String getManifestLocation() { 
		return "classpath:org/springframework/osgi/samples/simpleservice/test/MANIFEST.MF";
	}
	
	/**
	 * The location of the packaged OSGi bundles to be installed
	 * for this test. Values are Spring resource paths. The bundles
	 * we want to use are part of the same multi-project maven
	 * build as this project is. Hence we use the localMavenArtifact
	 * helper method to find the bundles produced by the package
	 * phase of the maven build (these tests will run after the
	 * packaging phase, in the integration-test phase). 
	 * 
	 * JUnit, commons-logging, spring-core and the spring OSGi
	 * test bundle are automatically included so do not need
	 * to be specified here.
	 */
	protected String[] getTestBundlesNames() {
		return new String[] {
			"org.springframework.osgi.samples, simple-service-bundle," +getSpringDMVersion()
		};
	}
	
	/**
	 * The superclass provides us access to the root bundle
	 * context via the 'getBundleContext' operation
	 */
	public void testOSGiStartedOk() {
		assertNotNull(bundleContext);
	}
	
	/**
	 * The simple service should have been exported as an
	 * OSGi service, which we can verify using the OSGi
	 * service APIs.
	 *
	 * In a Spring bundle, using osgi:reference is a much
	 * easier way to get a reference to a published service.
	 * 
	 */
	public void testSimpleServiceExported() {
		waitOnContextCreation("org.springframework.osgi.samples.simpleservice");
        ServiceReference ref = bundleContext.getServiceReference(MyService.class.getName());
        assertNotNull("Service Reference is null", ref);
        try {
            MyService simpleService = (MyService) bundleContext.getService(ref);
            assertNotNull("Cannot find the service", simpleService);
            assertEquals("simple service at your service", simpleService.stringValue());
        } finally {
            bundleContext.ungetService(ref);
        }
	}
	
    /* work-around Felix 1.4.x which doesn't fully support fragments and throws an exception by default */
	protected OsgiPlatform createPlatform() {
		OsgiPlatform platform = super.createPlatform();
		platform.getConfigurationProperties().setProperty("felix.fragment.validation", "warning");
		return platform;
	}	
}
