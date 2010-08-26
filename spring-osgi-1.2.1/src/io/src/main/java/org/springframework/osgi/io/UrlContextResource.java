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

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.core.io.ContextResource;
import org.springframework.core.io.UrlResource;
import org.springframework.osgi.io.internal.OsgiResourceUtils;

/**
 * Extension to {@link UrlResource} that adds support for
 * {@link ContextResource}. This resource is used by the
 * {@link OsgiBundleResourcePatternResolver} with the URLs returned by the OSGi
 * API.
 * 
 * @author Costin Leau
 */
class UrlContextResource extends UrlResource implements ContextResource {

	private final String pathWithinContext;


	/**
	 * Constructs a new <code>UrlContextResource</code> instance.
	 * 
	 * @param path
	 * @throws MalformedURLException
	 */
	public UrlContextResource(String path) throws MalformedURLException {
		super(path);
		pathWithinContext = checkPath(path);
	}

	private String checkPath(String path) {
		return (path.startsWith(OsgiResourceUtils.FOLDER_DELIMITER) ? path : OsgiResourceUtils.FOLDER_DELIMITER + path);
	}

	/**
	 * Constructs a new <code>UrlContextResource</code> instance.
	 * 
	 * @param url
	 */
	public UrlContextResource(URL url, String path) {
		super(url);
		this.pathWithinContext = checkPath(path);
	}

	public String getPathWithinContext() {
		return pathWithinContext;
	}
}
