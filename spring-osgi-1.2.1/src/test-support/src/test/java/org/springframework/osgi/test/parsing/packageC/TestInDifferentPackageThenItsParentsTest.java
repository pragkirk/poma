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

package org.springframework.osgi.test.parsing.packageC;

import java.util.jar.Manifest;

import org.springframework.osgi.test.parsing.packageA.BaseClassFromAnotherPackageTest;
import org.springframework.osgi.test.parsing.packageB.BaseClassFromAnotherPackageAndBundleTest;

/**
 * Abstract since we don't want to execute the test per se.
 * 
 * @author Costin Leau
 * 
 */
// callback interface (no exception or custom method signature pulled in)
public abstract class TestInDifferentPackageThenItsParentsTest extends BaseClassFromAnotherPackageAndBundleTest {

	public void testCheckBaseClassesHierarchy() throws Exception {
		Manifest mf = getManifest();
		System.out.println(mf.getMainAttributes().entrySet());
	}

	public String[] getBundleContentPattern() {
		String pkg = TestInDifferentPackageThenItsParentsTest.class.getPackage().getName().replace('.', '/').concat("/");
		String[] patterns = new String[] { pkg,
			BaseClassFromAnotherPackageTest.class.getName().replace('.', '/').concat(".class") };
		return patterns;
	}
}
