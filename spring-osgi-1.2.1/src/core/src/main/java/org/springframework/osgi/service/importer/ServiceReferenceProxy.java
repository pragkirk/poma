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

import org.osgi.framework.ServiceReference;

/**
 * {@link ServiceReference} extension used by Spring-DM service importers.
 * 
 * <p/> The interface aim is to decouple clients from the dynamics of the
 * imported services. Without such a proxy, a target service change (when
 * dealing with dynamic proxies), would force the corresponding service
 * reference to changes as well causing returned references to become stale. To
 * avoid this situation, the importer returns a proxy which is updated
 * automatically so the client always calls the updated, correct service
 * reference.
 * 
 * <p/> In most cases, users should not be aware of this interface. However, for
 * cases where the service reference has to be reused for going service lookups
 * and the OSGi platform demands the raw service reference class, this interface
 * allows access to the target, unwrapped service reference instance (which does
 * does not support service tracking and might become stale as explained above).
 * 
 * @author Costin Leau
 * 
 */
public interface ServiceReferenceProxy extends ServiceReference {

	/**
	 * Returns the target, native service reference used, at the moment of the
	 * call, by the proxy.
	 * 
	 * @return target service reference
	 */
	ServiceReference getTargetServiceReference();
}
