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

package org.springframework.osgi.iandt.namespace;

import java.net.URL;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.util.ObjectUtils;

/**
 * Integration test for libraries (that contain Spring namespaces) that are
 * embedded inside bundles which use their namespaces. Since the library is not
 * deployed as a bundle, other bundles should not see the namespace but the
 * bundle embedding it, should.
 * 
 * @author Costin Leau
 * 
 */
public class EmbeddedNamespaceLibraryTest extends BaseIntegrationTest {

	protected Manifest getManifest() {
		Manifest mf = super.getManifest();
		// add namespace on the test classpath
		mf.getMainAttributes().putValue(Constants.BUNDLE_CLASSPATH, ".,namespace/ns.jar");
		return mf;
	}

	protected String[] getBundleContentPattern() {
		return (String[]) ObjectUtils.addObjectToArray(super.getBundleContentPattern(), "namespace/**/*");
	}

	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/osgi/iandt/namespace/context.xml" };
	}

	public void testApplicationContextWasProperlyStarted() throws Exception {
		assertNotNull(applicationContext);
		assertNotNull(applicationContext.getBean("bean"));
	}

	public void testNamespaceFilesOnTheClassPath() throws Exception {
		// simple code to trigger an import for this package
		assertNotNull(NamespaceHandlerResolver.class);

		Bundle bundle = bundleContext.getBundle();
		URL handlers = bundle.getResource("META-INF/spring.handlers");
		URL schemas = bundle.getResource("META-INF/spring.schemas");

		assertNotNull(handlers);
		assertNotNull(schemas);

	}

}
