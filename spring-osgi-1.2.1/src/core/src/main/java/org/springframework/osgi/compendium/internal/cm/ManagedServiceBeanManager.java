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

package org.springframework.osgi.compendium.internal.cm;

import java.util.Map;

/**
 * Manager dealing with injection and updates of Spring beans configured through
 * the Configuration Admin.
 * 
 * <p/> Implementations are responsible for interacting with the Configuration
 * Admin service, for injection/reinjection of properties into the managed
 * beans.
 * 
 * @author Costin Leau
 * 
 */
public interface ManagedServiceBeanManager {

	/**
	 * Registers the given Spring-managed bean instance with the manager. The
	 * manager will apply any existing configuration to the given bean and
	 * return the newly configured instance back.
	 * 
	 * @param bean Spring-managed bean instance
	 * @return reinjected bean instace
	 */
	Object register(Object bean);

	/**
	 * Deregisters the given Spring-managed instance from the manager. Once
	 * deregistered, no configuration updates will be propagated to the given
	 * instance.
	 * 
	 * @param bean
	 */
	void unregister(Object bean);

	/**
	 * Re-applies injection on the Spring-managed instances using the given
	 * properties.
	 * 
	 * @param properties new properties
	 */
	void updated(Map properties);
}
