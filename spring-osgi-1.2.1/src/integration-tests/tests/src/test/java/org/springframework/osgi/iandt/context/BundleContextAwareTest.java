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

import java.io.Serializable;

import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.osgi.iandt.BaseIntegrationTest;

/**
 * Test injection of BundleContextAware.
 * 
 * @author Costin Leau
 * 
 */
public class BundleContextAwareTest extends BaseIntegrationTest {

	public static class BundleContextAwareHolder implements BundleContextAware {

		private BundleContext bundleContext;

		public BundleContext getBundleContext() {
			return bundleContext;
		}

		public void setBundleContext(BundleContext bundleContext) {
			this.bundleContext = bundleContext;
		}

		private static class AnotherInnerClass implements Serializable {

		}
	}

	protected String getManifestLocation() {
		return null;
	}

	protected String[] getConfigLocations() {
		return new String[] { "/org/springframework/osgi/iandt/context/bundleContextAwareTest.xml" };
	}

	public void testBundleContextAware() throws Exception {
		BundleContextAwareHolder holder = (BundleContextAwareHolder) applicationContext.getBean("bean");
		assertNotNull(holder.getBundleContext());
		assertSame(bundleContext, holder.getBundleContext());
		assertSame(applicationContext.getBean(ConfigurableOsgiBundleApplicationContext.BUNDLE_CONTEXT_BEAN_NAME),
			holder.getBundleContext());
	}
}
