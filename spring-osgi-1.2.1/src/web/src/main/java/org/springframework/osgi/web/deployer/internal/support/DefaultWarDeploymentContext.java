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

package org.springframework.osgi.web.deployer.internal.support;

import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.web.deployer.WarDeploymentContext;
import org.springframework.util.Assert;

/**
 * Simple, generic deployment context implementation.
 * 
 * @author Costin Leau
 * 
 */
public class DefaultWarDeploymentContext implements WarDeploymentContext {

	private final Bundle bundle;
	private final String contextPath;
	private final ServletContext servletContext;


	/**
	 * Constructs a new <code>DefaultWarDeploymentContext</code> instance.
	 * 
	 * @param bundle backing bundle
	 * @param contextPath context path associated with the web application
	 * @param servletContext servlet context backing the web application
	 */
	public DefaultWarDeploymentContext(Bundle bundle, String contextPath, ServletContext servletContext) {
		Assert.notNull(bundle, "bundle is required");
		Assert.hasText(contextPath, "a valid contextPath is required");
		Assert.notNull(servletContext, "servletContext is required");

		this.bundle = bundle;
		this.contextPath = contextPath;
		this.servletContext = servletContext;

		// bind the BundleContext as an attribute
		this.servletContext.setAttribute(WarDeploymentContext.OSGI_BUNDLE_CONTEXT_ATTRIBUTE,
			OsgiBundleUtils.getBundleContext(bundle));
	}

	public Bundle getBundle() {
		return bundle;
	}

	public String getContextPath() {
		return contextPath;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

}
