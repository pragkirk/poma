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

import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;

/**
 * Simple test for bundles that provide a configuration file with dots.
 * 
 * @author Costin Leau
 * 
 */
public class ConfigFileWithDotsTest extends AbstractConfigurableBundleCreatorTests {

	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt, config-with-dots.bundle,"
				+ getSpringDMVersion() };
	}

	public void testShapeServicePublished() throws Exception {
		assertNotNull(bundleContext.getServiceReference(Shape.class.getName()));
	}
}
