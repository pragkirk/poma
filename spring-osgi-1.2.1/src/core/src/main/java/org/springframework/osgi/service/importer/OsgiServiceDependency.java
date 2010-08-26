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

package org.springframework.osgi.service.importer;

import org.osgi.framework.Filter;

/**
 * Dependency contract to an OSGi service.
 * 
 * @author Costin Leau
 * @author Andy Piper
 */
public interface OsgiServiceDependency {

	/**
	 * Returns the OSGi filter for the service dependency.
	 * 
	 * @return filter describing the dependent OSGi service
	 */
	Filter getServiceFilter();

	/**
	 * Returns the bean name (if any) that declares this dependency.
	 * 
	 * @return the name of bean declaring the dependency. Can be null.
	 */
	String getBeanName();

	/**
	 * Indicates if the dependency is mandatory or not.
	 * 
	 * @return true if the dependency is mandatory, false otherwise.
	 */
	boolean isMandatory();
}
