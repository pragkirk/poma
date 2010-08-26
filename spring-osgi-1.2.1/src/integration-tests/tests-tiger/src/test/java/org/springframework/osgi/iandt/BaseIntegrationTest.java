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

package org.springframework.osgi.iandt;

import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;

/**
 * Base test class used for improving performance of integration tests by
 * creating bundles only with the classes within a package as opposed to all
 * resources available in the target folder.
 * 
 * @author Costin Leau
 * 
 */
public abstract class BaseIntegrationTest extends AbstractConfigurableBundleCreatorTests {

	protected String[] getBundleContentPattern() {
		String pkg = getClass().getPackage().getName().replace('.', '/').concat("/");
		String[] patterns = new String[] { BaseIntegrationTest.class.getName().replace('.', '/').concat(".class"),
			pkg + "**/*" };
		return patterns;
	}
}
