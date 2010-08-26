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

package org.springframework.osgi.iandt.io;

import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.iandt.io.component.ComponentBean;

/**
 * Integration test for Spring 2.5 component scan.
 * 
 * @author Costin Leau
 * 
 */
public class ComponentScanTest extends BaseIntegrationTest {

	protected String[] getConfigLocations() {
		return new String[] { "/org/springframework/osgi/iandt/io/component-scan.xml" };
	}

	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt,component.scan.bundle,"
				+ getSpringDMVersion() };
	}

	public void testComponentScan() throws Exception {
		// force an import on component bean
		logger.debug(ComponentBean.class);
		assertTrue("component scan did not pick up all classes", applicationContext.containsBean("bean"));
		assertTrue("component scan did not pick up all classes", applicationContext.containsBean("componentBean"));
	}
}
