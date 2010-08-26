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

package org.springframework.osgi.iandt.proxycreator;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.util.OsgiBundleUtils;

/**
 * Integration test that checks that a new classloader is created when the
 * bundle is refreshed. The test updates a bundle that internally creates JDK
 * and CGLIB proxies which, will fail in case the old CL is preserved.
 * 
 * @author Costin Leau
 * 
 */
public class ProxyCreatorTest extends BaseIntegrationTest {

	private static final String PROXY_CREATOR_SYM_NAME = "org.springframework.osgi.iandt.proxy.creator";


	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt,proxy.creator," + getSpringDMVersion(),
			"net.sourceforge.cglib, com.springsource.net.sf.cglib, 2.1.3" };
	}

	public void testNewProxiesCreatedOnBundleRefresh() throws Exception {
		// get a hold of the bundle proxy creator bundle and update it
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, PROXY_CREATOR_SYM_NAME);

		assertNotNull("proxy creator bundle not found", bundle);
		// update bundle (and thus create a new version of the classes)
		bundle.update();

		// make sure it starts-up
		try {
			waitOnContextCreation(PROXY_CREATOR_SYM_NAME, 60);
		}
		catch (Exception ex) {
			fail("updating the bundle failed");
		}
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		perms.add(new AdminPermission("*", AdminPermission.RESOLVE));
		return perms;
	}
}
