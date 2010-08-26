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

package org.springframework.osgi.iandt.componentscanning;

import java.awt.Shape;

import org.springframework.osgi.iandt.BaseIntegrationTest;

/**
 * @author Costin Leau
 */
public class OrderedComponentScanningTest extends BaseIntegrationTest {

	private static final String BEAN_NAME = "componentBean";

	private ComponentBean bean;

	private Shape shape;


	@Override
	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt, sync-tail-bundle," + getSpringDMVersion() };
	}

	public void testComponentExistence() throws Exception {
		assertTrue("component not found", applicationContext.containsBean(BEAN_NAME));
		assertNotNull("component not injected in the test", bean);
		assertNotNull("shape not injected in the test", shape);
	}

	public void testAutowireInjection() throws Exception {
		assertNotNull(bean.getSetterInjection());
		assertNotNull(bean.getConstructorInjection());
		assertNotNull(bean.getFieldInjection());
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] { "/org/springframework/osgi/iandt/componentscanning/context.xml" };
	}

	/**
	 * @param bean The bean to set.
	 */
	public void setBean(ComponentBean bean) {
		this.bean = bean;
	}

	/**
	 * @param shape The shape to set.
	 */
	public void setShape(Shape shape) {
		this.shape = shape;
	}

	protected boolean createManifestOnlyFromTestClass() {
		return false;
	}
}