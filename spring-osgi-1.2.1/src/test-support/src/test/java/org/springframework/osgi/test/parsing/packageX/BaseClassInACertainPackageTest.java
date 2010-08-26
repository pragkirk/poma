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

package org.springframework.osgi.test.parsing.packageX;

import java.io.File;
import java.net.URI;

import javax.imageio.ImageIO;
import javax.print.URIException;

import org.springframework.osgi.test.parsing.CaseWithVisibleMethodsBaseTest;

/**
 * @author Costin Leau
 * 
 */
public abstract class BaseClassInACertainPackageTest extends CaseWithVisibleMethodsBaseTest implements URIException {

	// strange import that doesn't do anything
	private static File file = ImageIO.getCacheDirectory();


	public int getReason() {
		throw new UnsupportedOperationException();
	}

	public URI getUnsupportedURI() {
		throw new UnsupportedOperationException();
	}
}
