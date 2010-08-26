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

package org.springframework.osgi.service.exporter.support;

import java.util.Map;

import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.osgi.service.exporter.OsgiServiceRegistrationListener;
import org.springframework.osgi.service.exporter.support.internal.support.ServiceRegistrationDecorator;

/**
 * Base exporter class providing common functionality for registering (also
 * known as exporting) Spring beans as OSGi services.
 * 
 * @author Costin Leau
 */
abstract class AbstractOsgiServiceExporter implements DisposableBean {

	/** listeners */
	private OsgiServiceRegistrationListener[] listeners = new OsgiServiceRegistrationListener[0];


	/**
	 * Takes care of notifying the listeners on both startup and shutdown (by
	 * wrapping the service registration).
	 * 
	 * @param service object published as OSGi service
	 * @param properties exported OSGi service properties
	 * @param registration original service registration
	 * @return
	 */
	ServiceRegistration notifyListeners(Object service, Map properties, ServiceRegistration registration) {
		// notify listeners
		callRegisteredOnListeners(service, properties);
		// wrap registration to be notified of unregistration
		return new ServiceRegistrationDecorator(service, registration, listeners);
	}

	/**
	 * Call registration on listeners.
	 * 
	 * @param properties
	 */
	private void callRegisteredOnListeners(Object service, Map properties) {
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] != null) {
				try {
					listeners[i].registered(service, properties);
				}
				catch (Exception ex) {
					// no need to log exceptions, the listener wrapper already
					// does this for us
				}
			}
		}
	}

	/**
	 * Sets the listeners interested in registration and unregistration events.
	 * 
	 * @param listeners registration/unregistration listeners.
	 */
	public void setListeners(OsgiServiceRegistrationListener[] listeners) {
		if (listeners != null)
			this.listeners = listeners;
	}

	public void destroy() {
		unregisterService();
	}

	/**
	 * Registers/Exports the OSGi service.
	 */
	abstract void registerService();

	/**
	 * Unregisters/de-exports the OSGi service.
	 */
	abstract void unregisterService();
}
