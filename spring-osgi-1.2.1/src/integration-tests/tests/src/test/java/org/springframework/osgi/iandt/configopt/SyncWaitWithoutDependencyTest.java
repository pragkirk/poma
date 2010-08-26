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

import org.osgi.framework.Bundle;
import org.springframework.osgi.util.OsgiBundleUtils;

/**
 * Integration test for Sync Wait but this time, by checking the waiting by
 * satisfying the dependency through this test.
 * 
 * @author Costin Leau
 * 
 */
public class SyncWaitWithoutDependencyTest extends BehaviorBaseTest {

	public void testBehaviour() throws Exception {

		String bundleId = "org.springframework.osgi.iandt, sync-wait-bundle,"
				+ getSpringDMVersion();

		// locate bundle
		String tailBundleId = "org.springframework.osgi.iandt, sync-tail-bundle,"
				+ getSpringDMVersion();

		// start bundle first (no dependency)
		Bundle bundle = installBundle(bundleId);

		bundle.start();

		assertTrue("bundle " + bundle + "should have started", OsgiBundleUtils.isBundleActive(bundle));
		// start bundle dependency
		Bundle tailBundle = installBundle(tailBundleId);
		tailBundle.start();

		assertTrue("bundle " + tailBundle + "hasn't been fully started", OsgiBundleUtils.isBundleActive(tailBundle));

		// check appCtx hasn't been published
		assertContextServiceIs(bundle, false, 500);
		// check the dependency ctx
		assertContextServiceIs(tailBundle, true, 500);

		// restart the bundle (to catch the tail)
		bundle.stop();
		bundle.start();

		// check appCtx has been published
		assertContextServiceIs(bundle, true, 500);
	}
}
