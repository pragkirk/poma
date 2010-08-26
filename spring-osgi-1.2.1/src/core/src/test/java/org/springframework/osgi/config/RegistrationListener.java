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
package org.springframework.osgi.config;

import java.util.Map;

import org.springframework.osgi.service.exporter.OsgiServiceRegistrationListener;

/**
 * Registration listener.
 * 
 * @author Costin Leau
 * 
 */
public class RegistrationListener implements OsgiServiceRegistrationListener {

	static int BIND_CALLS = 0;

	static int UNBIND_CALLS = 0;

	static Object SERVICE_UNREG;

	static Object SERVICE_REG;

	public void registered(Object service, Map serviceProperties) {
		BIND_CALLS++;
		SERVICE_REG = service;
	}

	public void unregistered(Object service, Map serviceProperties) {
		UNBIND_CALLS++;
		SERVICE_UNREG = service;
	}

}
