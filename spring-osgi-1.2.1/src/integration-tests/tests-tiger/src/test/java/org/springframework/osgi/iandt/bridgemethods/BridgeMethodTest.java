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

package org.springframework.osgi.iandt.bridgemethods;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.List;

import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.iandt.BaseIntegrationTest;

/**
 * Integration test for listeners with bridge methods.
 * 
 * @author Costin Leau
 * 
 */
public class BridgeMethodTest extends BaseIntegrationTest {

	protected String[] getConfigLocations() {
		return new String[] { "/org/springframework/osgi/iandt/bridgemethods/config.xml" };
	}

	public void testGenerifiedListener() throws Exception {
		assertEquals(Listener.BIND_CALLS, 0);
		assertEquals(Listener.UNBIND_CALLS, 0);

		// register a point
		ServiceRegistration reg = bundleContext.registerService(Shape.class.getName(), new Area(), null);
		List list = (List) applicationContext.getBean("collection");
		assertEquals(1, list.size());
		assertEquals(Listener.BIND_CALLS, 1);
		assertEquals(Listener.UNBIND_CALLS, 0);

		reg.unregister();
		assertEquals(Listener.BIND_CALLS, 1);
		assertEquals(Listener.UNBIND_CALLS, 1);
	}
}
