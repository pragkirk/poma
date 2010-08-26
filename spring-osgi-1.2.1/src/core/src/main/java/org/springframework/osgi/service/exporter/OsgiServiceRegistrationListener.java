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

package org.springframework.osgi.service.exporter;

import java.util.Map;

/**
 * Registration listener that needs notifications of registration and
 * unregistration of OSGi services exported through Spring OSGi. Implementations
 * can throws exceptions if they need/have to but they are not be propagated to
 * other listeners nor do they stop the other listeners from being notified.
 * 
 * @author Costin Leau
 * @see org.springframework.osgi.service.exporter.support.OsgiServiceFactoryBean
 */
public interface OsgiServiceRegistrationListener {

	/**
	 * Called when the the service exported has been registered in the OSGi
	 * space. The service properties are made available as a {@link Map} which
	 * can be safely cast to a {@link java.util.Dictionary} if needed.
	 * 
	 * @param service object registered as an OSGi service
	 * @param serviceProperties OSGi service registration properties
	 * @throws Exception custom exception that is logged but not propagated to
	 * other listeners
	 */
	void registered(Object service, Map serviceProperties) throws Exception;

	/**
	 * Called when the OSGi service has been unregistered (removed from OSGi
	 * space). The service properties are made available as a {@link Map} which
	 * can be safely cast to a {@link java.util.Dictionary} if needed.
	 * 
	 * @param service object unregistered as a service from the OSGi space
	 * @param serviceProperties OSGi service registration properties
	 * @throws Exception custom exception that is logged but not propagated to
	 * other listeners
	 */
	void unregistered(Object service, Map serviceProperties) throws Exception;

}
