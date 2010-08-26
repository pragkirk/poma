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
 * Test the sync behaviour but this time, by first staring the dependency and the
 * the bundle.
 * 
 * @author Costin Leau
 * 
 */
public class SyncNoWaitWithReverseQueueTest extends BehaviorBaseTest {

	private String tailBundleId = "org.springframework.osgi.iandt, sync-tail-bundle,"
			+ getSpringDMVersion();

	private String bundleId = "org.springframework.osgi.iandt, sync-nowait-bundle,"
			+ getSpringDMVersion();

	public void testBehaviour() throws Exception {

		// locate bundle
		Bundle bundle = installBundle(bundleId);
		Bundle tail = installBundle(tailBundleId);

		// start dependency first
		tail.start();

		Thread.sleep(500);

		// followed by the bundle
		bundle.start();

		assertTrue("bundle " + tail + "hasn't been fully started", OsgiBundleUtils.isBundleActive(tail));
		assertTrue("bundle " + bundle + "hasn't been fully started", OsgiBundleUtils.isBundleActive(bundle));

		// wait for the listener to get the bundles and wait for timeout

		assertContextServiceIs(bundle, true, 2000);

		// check that the dependency service is actually started as the
		// dependency
		// bundle has started
		assertNotNull(bundleContext.getServiceReference(Shape.class.getName()));

	}

}
