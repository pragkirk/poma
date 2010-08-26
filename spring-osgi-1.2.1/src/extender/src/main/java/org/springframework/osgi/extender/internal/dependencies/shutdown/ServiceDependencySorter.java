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
package org.springframework.osgi.extender.internal.dependencies.shutdown;

import org.osgi.framework.Bundle;

/**
 * SPI for sorting OSGi bundles based on their service dependency. Given a
 * number of bundles, implementors of this interface will return a list
 * referencing the bundles in the order in which they should be shutdown based
 * on their OSGi service dependencies.
 * <p/>
 * It is considered that bundle A depends on bundle B if A uses a service that
 * belongs to a bundle which depends on B or is B itself. Note that bundles can
 * depend on each other : A -> B -> A.
 * <p/>
 * Thus implementations should 'sort' direct, circular graphs without any
 * guarantee on the node used for start.
 *  
 * @author Costin Leau
 * 
 */
public interface ServiceDependencySorter {

	/**
	 * Given a number of bundles, determine the dependency between each other and compute
	 * the dependency tree.
	 * 
	 * @param bundles array of bundles
	 * @return an array of bundles, sorted out by their dependency. 
	 */
	Bundle[] computeServiceDependencyGraph(Bundle[] bundles);
}
