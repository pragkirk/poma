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

import java.io.IOException;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.osgi.io.OsgiBundleResourceLoader;
import org.springframework.osgi.io.OsgiBundleResourcePatternResolver;
import org.springframework.util.ObjectUtils;

/**
 * @author Costin Leau
 * 
 */
public class InvalidLocationsTest extends BaseIoTest {
	private static final String NON_EXISTING = "/non-existing";

	private ResourceLoader osgiRL;

	private ResourcePatternResolver osgiRPR;

	protected void onSetUp() throws Exception {
		super.onSetUp();
		osgiRL = new OsgiBundleResourceLoader(bundle);
		osgiRPR = new OsgiBundleResourcePatternResolver(bundle);
	}

	protected void onTearDown() throws Exception {
		super.onTearDown();
		osgiRL = null;
		osgiRPR = null;
	}

	protected String getManifestLocation() {
		return null;
	}

	protected String[] getTestBundlesNames() {
		return null;
	}

	public void testDefaultClassLoader() throws Exception {
		testOneResource(new DefaultResourceLoader());
	}

	public void testOsgiResourceLoader() throws Exception {
		testOneResource(osgiRL);
	}

	private void testOneResource(ResourceLoader loader) {
		Resource res = loader.getResource(NON_EXISTING);
		assertFalse(res.exists());
	}

	public void testDefaultPatternResourceLoader() throws Exception {
		testMultipleResources(new PathMatchingResourcePatternResolver());
	}

	public void testPatternResourceLoader() throws Exception {
		testMultipleResources(osgiRPR);
	}

	private void testMultipleResources(ResourcePatternResolver loader) throws IOException {
		Resource[] res = loader.getResources(NON_EXISTING);
		assertEquals("invalid resource array " + ObjectUtils.nullSafeToString(res), 1, res.length);
	}

}
