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

package org.springframework.osgi.web.deployer;

import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Context associated with a war deployment. Provides access to various war
 * properties such as the owning bundle, context path or associated servlet
 * context.
 * 
 * @see WarDeployment
 * @see ServletContext
 * 
 * @author Costin Leau
 * 
 */
public interface WarDeploymentContext {

	/**
	 * Convenience context attribute under which the OSGi BundleContext is
	 * bound. Implementations are required to support this attribute.
	 */
	static final String OSGI_BUNDLE_CONTEXT_ATTRIBUTE = "org.springframework.osgi.web." + BundleContext.class.getName();


	/**
	 * Returns the bundle associated with this war deployment.
	 * 
	 * @return bundle associated with this war deployment
	 */
	Bundle getBundle();

	/**
	 * Returns the context path under which this war deployment resides.
	 * 
	 * @return the context path for this war deployment
	 */
	String getContextPath();

	/**
	 * Returns the ServletContext used by the war backing this deployment.
	 * 
	 * @return the servlet context associated with this war
	 */
	ServletContext getServletContext();
}
