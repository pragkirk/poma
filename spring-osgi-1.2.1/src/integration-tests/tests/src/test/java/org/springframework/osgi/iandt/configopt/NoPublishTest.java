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

import java.awt.Point;

import org.osgi.framework.Bundle;
import org.springframework.osgi.util.OsgiBundleUtils;

/**
 * Integration test for publish-context directive.
 * 
 * @author Costin Leau
 * 
 */
public class NoPublishTest extends BehaviorBaseTest {

	public void testBehaviour() throws Exception {
		String bundleId = "org.springframework.osgi.iandt, nopublish-bundle,"
				+ getSpringDMVersion();

		// start it
		Bundle bundle = installBundle(bundleId);
		bundle.start();
		// wait for the listener to catch up
		Thread.sleep(1000);
		assertTrue("bundle " + bundle + "hasn't been fully started", OsgiBundleUtils.isBundleActive(bundle));

		// check that the appCtx is not publish
		assertContextServiceIs(bundle, false, 1000);

		// but the point service is
		assertNotNull("point service should have been published"
				+ bundleContext.getServiceReference(Point.class.getName()));
	}
}
