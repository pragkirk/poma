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

package org.springframework.osgi.iandt.ns;

import java.awt.Shape;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;

/**
 * Integration test that provides a namespace that is also used internally.
 * 
 * @author Costin Leau
 */
public class NamespaceProviderAndConsumerTest extends BaseIntegrationTest {

	private Shape nsBean;

	private static final String BND_SYM_NAME = "org.springframework.osgi.iandt.ns.own.provider";


	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt, ns.own.consumer," + getSpringDMVersion() };
	}

	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/osgi/iandt/ns/context.xml" };
	}

	public void testApplicationContextWasProperlyStarted() throws Exception {
		assertNotNull(applicationContext);
		assertNotNull(applicationContext.getBean("nsDate"));
		assertNotNull(applicationContext.getBean("nsBean"));
	}

	public void testTestAutowiring() throws Exception {
		assertNotNull(nsBean);
	}

	public void tstNamespaceFilesOnTheClassPath() throws Exception {
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, BND_SYM_NAME);
		assertNotNull("cannot find handler bundle", bundle);
		URL handlers = bundle.getResource("META-INF/spring.handlers");
		URL schemas = bundle.getResource("META-INF/spring.schemas");

		assertNotNull("cannot find a handler inside the custom bundle", handlers);
		assertNotNull("cannot find a schema inside the custom bundle", schemas);
	}

	public void testNSBundlePublishedOkay() throws Exception {
		ServiceReference ref = OsgiServiceReferenceUtils.getServiceReference(bundleContext,
			ApplicationContext.class.getName(), "(" + Constants.BUNDLE_SYMBOLICNAME + "=" + BND_SYM_NAME + ")");
		assertNotNull(ref);
		ApplicationContext ctx = (ApplicationContext) bundleContext.getService(ref);
		assertNotNull(ctx);
		assertNotNull(ctx.getBean("nsBean"));
		assertNotNull(ctx.getBean("nsDate"));

	}

	/**
	 * @param nsBean The nsBean to set.
	 */
	public void setNsBean(Shape nsBean) {
		this.nsBean = nsBean;
	}
}