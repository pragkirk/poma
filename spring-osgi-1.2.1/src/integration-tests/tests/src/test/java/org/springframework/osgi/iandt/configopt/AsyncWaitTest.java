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
package org.springframework.osgi.iandt.configopt;

import java.awt.Shape;
import java.awt.geom.Area;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiServiceUtils;

/**
 * Integration test for Async Wait (the default behavior). Start the bundle
 * which will wait for the service to come up even though its importer has small
 * timeout.
 * 
 * @author Costin Leau
 * 
 */
public class AsyncWaitTest extends BehaviorBaseTest {

	private ServiceRegistration registration;

	protected void onTearDown() throws Exception {
		OsgiServiceUtils.unregisterService(registration);
	}

	public void testBehaviour() throws Exception {

		String bundleId = "org.springframework.osgi.iandt, async-wait-bundle,"
				+ getSpringDMVersion();

		// start it
		Bundle bundle = installBundle(bundleId);
		bundle.start();

		assertTrue("bundle " + bundle + "hasn't been fully started", OsgiBundleUtils.isBundleActive(bundle));

		// make sure the appCtx is not up
		// check that the appCtx is *not* published (it waits for the service to
		// appear)
		assertContextServiceIs(bundle, false, 500);

		// put service up
		registration = bundleContext.registerService(Shape.class.getName(), new Area(), null);

		// do wait a bit to let the appCtx to fully start
		// check the appCtx again (should be published)
		assertContextServiceIs(bundle, true, 4000);

	}
}
