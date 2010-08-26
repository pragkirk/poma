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

package org.springframework.osgi.iandt.context;

import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.osgi.iandt.BaseIntegrationTest;

/**
 * Integration test on the functionality offered by OSGi app context.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiAppContextTest extends BaseIntegrationTest {

	private BundleContext bundleContext;


	public void testBundleContextAvailableAsBean() {
		ApplicationContext ctx = applicationContext;
		assertNotNull(ctx);
		assertTrue("bundleContext not available as a bean",
			applicationContext.containsBean(ConfigurableOsgiBundleApplicationContext.BUNDLE_CONTEXT_BEAN_NAME));
	}

	public void testBundleContextInjected() {
		assertNotNull("bundleContext hasn't been injected into the test", bundleContext);
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public void testBundleContextIsTheSame() {
		assertSame(bundleContext,
			applicationContext.getBean(ConfigurableOsgiBundleApplicationContext.BUNDLE_CONTEXT_BEAN_NAME));
	}
}
