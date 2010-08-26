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

package org.springframework.osgi.iandt.io;

import java.io.File;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.osgi.io.OsgiBundleResourcePatternResolver;
import org.springframework.osgi.util.OsgiBundleUtils;

/**
 * @author Costin Leau
 * 
 */
public class EquinoxFileTests extends BaseIoTest {

	private static final String REFERENCE_PROTOCOL = "reference:file:";
	private static final String EXPANDED_BUNDLE_SYM_NAME = "org.sf.osgi.iandt.io.expanded.bundle";


	public void testResolveResourceWithFilePrefix() throws Exception {
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, EXPANDED_BUNDLE_SYM_NAME);
		assertTrue(bundle.getLocation().startsWith(REFERENCE_PROTOCOL));
		ResourcePatternResolver resolver = new OsgiBundleResourcePatternResolver(bundle);
		Resource res = resolver.getResource("resource.res");
		assertTrue(res.getFile().exists());
	}

	public void testResolveResourceWithReferenceFilePrefix() throws Exception {
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, EXPANDED_BUNDLE_SYM_NAME);
		assertTrue(bundle.getLocation().startsWith(REFERENCE_PROTOCOL));
		assertNotNull(new URL(bundle.getLocation()).getFile());
		ResourcePatternResolver resolver = new OsgiBundleResourcePatternResolver(bundle);
		Resource res = resolver.getResource("/META-INF/MANIFEST.MF");
		assertTrue(res.getFile().exists());
	}

	protected void postProcessBundleContext(BundleContext context) throws Exception {
		super.postProcessBundleContext(context);
		File expandedBundle = new File(".", "target/test-classes/expanded-bundle.jar");
		System.out.println("Installing expanded bundle from " + expandedBundle.getCanonicalPath());
		context.installBundle(REFERENCE_PROTOCOL + expandedBundle.getCanonicalPath());
	}

	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return !isEquinox();
	}
}
