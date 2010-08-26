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

package org.springframework.osgi.io;

import org.osgi.framework.Bundle;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * OSGi specific {@link org.springframework.core.io.ResourceLoader}
 * implementation.
 * 
 * This loader resolves paths inside an OSGi bundle using the bundle native
 * methods. Please see {@link OsgiBundleResource} javadoc for information on
 * what prefixes are supported.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 * 
 * @see org.osgi.framework.Bundle
 * @see org.springframework.osgi.io.OsgiBundleResource
 * 
 */
public class OsgiBundleResourceLoader extends DefaultResourceLoader {

	private final Bundle bundle;


	/**
	 * Creates a OSGi aware <code>ResourceLoader</code> using the given
	 * bundle.
	 * 
	 * @param bundle OSGi <code>Bundle</code> to be used by this loader
	 * loader.
	 */
	public OsgiBundleResourceLoader(Bundle bundle) {
		this.bundle = bundle;
	}

	protected Resource getResourceByPath(String path) {
		Assert.notNull(path, "Path is required");
		return new OsgiBundleResource(this.bundle, path);
	}

	public Resource getResource(String location) {
		Assert.notNull(location, "location is required");
		return new OsgiBundleResource(bundle, location);
	}

	/**
	 * Returns the bundle used by this loader.
	 * 
	 * @return OSGi <code>Bundle</code> used by this resource
	 */
	public final Bundle getBundle() {
		return bundle;
	}

}
