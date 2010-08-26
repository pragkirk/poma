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
 * Integration test for AsyncNoWait
 * 
 * @author Costin Leau
 * 
 */
public class AsyncNoWaitTest extends BehaviorBaseTest {

	private ServiceRegistration registration;

	protected void onTearDown() throws Exception {
		OsgiServiceUtils.unregisterService(registration);
	}

	public void testBehaviour() throws Exception {
		String bundleId = "org.springframework.osgi.iandt, async-nowait-bundle,"
				+ getSpringDMVersion();

		// start it
		Bundle bundle = installBundle(bundleId);
		bundle.start();

		// wait for the bundle to start and fail
		Thread.sleep(3000);

		// put service up
		registration = bundleContext.registerService(Shape.class.getName(), new Area(), null);

		assertTrue("bundle " + bundle + "hasn't been fully started", OsgiBundleUtils.isBundleActive(bundle));

		// check that the appCtx is *not* published 
		// TODO: this fails sometimes on the build server - find out why
		// assertContextServiceIs(bundle, false, 1000);
	}
}
