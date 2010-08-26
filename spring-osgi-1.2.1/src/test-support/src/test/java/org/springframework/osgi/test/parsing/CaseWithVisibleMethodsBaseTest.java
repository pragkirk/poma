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

import java.io.File;
import java.util.Properties;
import java.util.jar.Manifest;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;

/**
 * @author Costin Leau
 * 
 */
public abstract class CaseWithVisibleMethodsBaseTest extends AbstractConfigurableBundleCreatorTests {

	public String getRootPath() {
		ResourceLoader fileLoader = new DefaultResourceLoader();
		try {
			String classFile = CaseWithVisibleMethodsBaseTest.class.getName().replace('.', '/').concat(".class");
			Resource res = fileLoader.getResource(classFile);
			String fileLocation = "file:/" + res.getFile().getAbsolutePath();
			String classFileToPlatform = CaseWithVisibleMethodsBaseTest.class.getName().replace('.', File.separatorChar).concat(
				".class");
			return fileLocation.substring(0, fileLocation.indexOf(classFileToPlatform));
		}
		catch (Exception ex) {
		}

		return null;
	}

	public Manifest getManifest() {
		return super.getManifest();
	}

	public Properties getSettings() throws Exception {
		return super.getSettings();
	}

	public String[] getBundleContentPattern() {
		return super.getBundleContentPattern();
	}

}
