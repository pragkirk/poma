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

package org.springframework.osgi.iandt.cm.managedservicefactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.osgi.service.exporter.OsgiServiceRegistrationListener;

/**
 * Simple test listener.
 * 
 * @author Costin Leau
 */
public class Listener implements OsgiServiceRegistrationListener {

	public static final Map instances = Collections.synchronizedMap(new LinkedHashMap());
	public static final Object regBarrier = new Object();
	public static final Object unregBarrier = new Object();


	public void registered(Object service, Map serviceProperties) throws Exception {
		instances.put(service, serviceProperties);
		System.out.println("added service "+ serviceProperties);
		synchronized (regBarrier) {
			regBarrier.notify();
		}
	}

	public void unregistered(Object service, Map serviceProperties) throws Exception {
		instances.remove(service);
		System.out.println("removed service "+ serviceProperties);
		synchronized (unregBarrier) {
			unregBarrier.notify();
		}
	}
}
