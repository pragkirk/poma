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

package org.springframework.osgi.test.parsing;

import java.lang.reflect.Field;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.osgi.framework.Constants;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
import org.springframework.osgi.test.parsing.packageA.BaseClassFromAnotherPackageTest;
import org.springframework.osgi.test.parsing.packageB.BaseClassFromAnotherPackageAndBundleTest;
import org.springframework.osgi.test.parsing.packageC.TestInDifferentPackageThenItsParentsTest;
import org.springframework.osgi.test.parsing.packageZ.FinalTestClassTest;
import org.springframework.util.ObjectUtils;

/**
 * Integration that checks if the class hierarchy is properly parsed. Note this
 * test doesn't run in OSGi, it just invokes the bytecode parsing.
 * 
 * @author Costin Leau
 * 
 */
public class DifferentParentsInDifferentBundlesTest extends TestCase {

	public void testCheckBaseClassesHierarchy() throws Exception {
		// create class
		// make sure the packaging puts some of the tests parent in a different class
		TestInDifferentPackageThenItsParentsTest test = new TestInDifferentPackageThenItsParentsTest() {
		};

		String importPackage = getImportPackage(test);

		// check parent package
		// parent in a different bundle
		assertTrue("missing parent package not considered", contains(importPackage,
			BaseClassFromAnotherPackageAndBundleTest.class.getPackage().getName()));
		// parent in a different package but the same bundle (i.e. no import)
		assertFalse("contained parent not considered", contains(importPackage,
			BaseClassFromAnotherPackageTest.class.getPackage().getName()));
		// check present parent dependencies
		assertTrue("contained parent dependencies not considered", contains(importPackage, "javax.imageio"));
	}

	public void testSuperClassInterfacesConsidered() throws Exception {
		FinalTestClassTest test = new FinalTestClassTest() {
		};

		String importPackage = getImportPackage(test);
		// check test interface package
		assertTrue("interface present on the test class ignored", contains(importPackage, "javax.swing.text"));
		// check super class interface package
		assertTrue("interface present on the test class ignored", contains(importPackage,
			"javax.security.auth.callback"));
		// check super class interface package
		assertTrue("interface present on superclass ignored", contains(importPackage, "javax.print"));
	}

	private Manifest getParsedManifestFor(CaseWithVisibleMethodsBaseTest testCase) throws Exception {

		System.out.println(ObjectUtils.nullSafeToString(testCase.getBundleContentPattern()));
		Field jarSettings = AbstractConfigurableBundleCreatorTests.class.getDeclaredField("jarSettings");
		// initialize settings
		jarSettings.setAccessible(true);
		jarSettings.set(null, testCase.getSettings());

		Manifest mf = testCase.getManifest();

		return mf;
	}

	private String getImportPackage(CaseWithVisibleMethodsBaseTest test) throws Exception {
		Manifest mf = getParsedManifestFor(test);
		String importPackage = mf.getMainAttributes().getValue(Constants.IMPORT_PACKAGE);
		System.out.println("import package value is " + importPackage);
		return importPackage;
	}

	private boolean contains(String text, String item) {
		return text.indexOf(item) > -1;
	}
}
