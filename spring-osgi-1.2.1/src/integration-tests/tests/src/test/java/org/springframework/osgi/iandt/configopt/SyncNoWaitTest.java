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

import org.osgi.framework.Bundle;
import org.springframework.osgi.util.OsgiBundleUtils;

/**
 * Integration test for Sync NoWait.
 * 
 * Start two bundles, one which requires a dependency and one which provides it.
 * However, since they are started synchronously, the first one will fail.
 * 
 * @author Costin Leau
 * 
 */
public class SyncNoWaitTest extends BehaviorBaseTest {

	private String tailBundleId = "org.springframework.osgi.iandt, sync-tail-bundle,"
			+ getSpringDMVersion();

	private String bundleId = "org.springframework.osgi.iandt, sync-nowait-bundle,"
			+ getSpringDMVersion();

	public void testBehaviour() throws Exception {

		// locate bundle
		Bundle bundle = installBundle(bundleId);
		Bundle tail = installBundle(tailBundleId);

		// start bundle first
		bundle.start();

		assertTrue("bundle " + bundle + "hasn't been fully started", OsgiBundleUtils.isBundleActive(bundle));

		// followed by its tail
		tail.start();
		assertTrue("bundle " + tail + "hasn't been fully started", OsgiBundleUtils.isBundleActive(tail));

		// wait for the listener to get the bundles and wait for timeout

		// make sure the appCtx is not up
		// check that the appCtx is *not* published (it waits for the service to
		// appear)
		assertContextServiceIs(bundle, false, 3000);

		// wait for appCtx to timeout
		//Thread.sleep(3000);

		// check that the dependency service is actually started as the
		// dependency
		// bundle has started
		assertNotNull(bundleContext.getServiceReference(Shape.class.getName()));
	}

}
