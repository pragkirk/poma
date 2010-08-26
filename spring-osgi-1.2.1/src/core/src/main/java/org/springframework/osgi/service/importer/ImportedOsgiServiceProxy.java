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

/**
 * Infrastructure interface available on Spring-DM managed OSGi services. Gives
 * read-only access to the proxy backing object service reference.
 * 
 * @see ServiceReferenceProxy
 * @author Costin Leau
 * 
 */
public interface ImportedOsgiServiceProxy {

	/**
	 * Provides access to the service reference used for accessing the backing
	 * object. The returned object is a proxy over the native ServiceReference
	 * obtained from the OSGi platform, so that proper service tracking is
	 * obtained. This means that if the imported service change, the updates are
	 * reflected to the returned service reference automatically.
	 * 
	 * @return backing object service reference
	 */
	ServiceReferenceProxy getServiceReference();
}
