/*
 * Copyright 2006-2009 the original author or authors.
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

import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.Assert;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.core.io.Resource;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiFilterUtils;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;
import org.springframework.osgi.util.OsgiServiceUtils;

/**
 * @author Costin Leau
 */
public class ExporterWithOptionalAndMandatoryImportersTest extends BaseIntegrationTest {

	private static final String DEP_SYN_NAME = "org.springframework.osgi.iandt.dependency.exporter.importer";

	private ServiceRegistration optional, mandatory;

	protected void postProcessBundleContext(BundleContext context) throws Exception {
		super.postProcessBundleContext(context);
		installTestBundle(context);
	}

	protected void onSetUp() throws Exception {
		registerOptional();
		registerMandatory();
	}

	protected void onTearDown() throws Exception {
		Bundle bnd = getDependencyBundle();
		bnd.stop();
		OsgiServiceUtils.unregisterService(mandatory);
		OsgiServiceUtils.unregisterService(optional);
	}

	public void testInjectedDependencies() throws Exception {
		Bundle bnd = getDependencyBundle();
		bnd.start();

		logger.info("Waiting for the test bundle to start up...");
		waitOnContextCreation(DEP_SYN_NAME);
		logger.info("Test bundle context created - starting test...");
		
		assertTrue("exporter not alive on startup", isInjectedExporterAlive());
		optional.unregister();
		assertTrue("exporter affected by the optional dependency", isInjectedExporterAlive());
		mandatory.unregister();
		assertFalse("exporter not affected by the optional dependency", isInjectedExporterAlive());
		registerOptional();
		assertFalse("exporter affected by the optional dependency", isInjectedExporterAlive());
		registerMandatory();
		assertTrue("exporter not affected by the optional dependency", isInjectedExporterAlive());
		optional.unregister();
		assertTrue("exporter affected by the optional dependency", isInjectedExporterAlive());
	}

	public void testDependsOnDependencies() throws Exception {
		Bundle bnd = getDependencyBundle();
		bnd.start();

		logger.info("Waiting for the test bundle to start up...");
		waitOnContextCreation(DEP_SYN_NAME);
		logger.info("Test bundle context created - starting test...");
		
		assertTrue("exporter not alive on startup", isDependsOnExporterAlive());
		optional.unregister();
		assertTrue("exporter affected by the optional dependency", isDependsOnExporterAlive());
		mandatory.unregister();
		assertFalse("exporter not affected by the optional dependency", isDependsOnExporterAlive());
		registerOptional();
		assertFalse("exporter affected by the optional dependency", isDependsOnExporterAlive());
		registerMandatory();
		assertTrue("exporter not affected by the optional dependency", isDependsOnExporterAlive());
		optional.unregister();
		assertTrue("exporter affected by the optional dependency", isInjectedExporterAlive());
	}

	private boolean isExporterAlive(String name) {
		String filter = OsgiFilterUtils.unifyFilter(new Class[] { Serializable.class, Cloneable.class },
				"(org.springframework.osgi.bean.name=" + name + ")");
		ServiceReference reference = OsgiServiceReferenceUtils.getServiceReference(bundleContext, filter);
		if (reference != null) {
			Object service = bundleContext.getService(reference);
			return service != null;
		}
		return false;
	}

	private boolean isInjectedExporterAlive() {
		return isExporterAlive("injected-export");
	}

	private boolean isDependsOnExporterAlive() {
		return isExporterAlive("depends-on-export");
	}

	private void registerOptional() {
		optional = bundleContext.registerService(SortedSet.class.getName(), new TreeSet(), null);
	}

	private void registerMandatory() {
		mandatory = bundleContext.registerService(SortedMap.class.getName(), new TreeMap(), null);
	}

	Bundle installTestBundle(BundleContext context) throws Exception {
		Resource res = getLocator().locateArtifact("org.springframework.osgi.iandt", "export-import-dependency-bundle",
				getSpringDMVersion());
		return context.installBundle("test-bundle", res.getInputStream());
	}

	protected Bundle getDependencyBundle() {
		return OsgiBundleUtils.findBundleBySymbolicName(bundleContext, DEP_SYN_NAME);
	}
}