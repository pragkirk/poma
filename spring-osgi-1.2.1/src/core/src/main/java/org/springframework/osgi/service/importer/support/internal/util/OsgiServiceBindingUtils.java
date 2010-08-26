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

package org.springframework.osgi.service.importer.support.internal.util;

import java.util.Dictionary;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author Costin Leau
 * 
 */
public abstract class OsgiServiceBindingUtils {

	private static final Log log = LogFactory.getLog(OsgiServiceBindingUtils.class);


	public static void callListenersBind(BundleContext context, Object serviceProxy, ServiceReference reference,
			OsgiServiceLifecycleListener[] listeners) {
		if (!ObjectUtils.isEmpty(listeners)) {
			boolean debug = log.isDebugEnabled();

			// get a Dictionary implementing a Map
			Dictionary properties = OsgiServiceReferenceUtils.getServicePropertiesSnapshot(reference);
			for (int i = 0; i < listeners.length; i++) {
				if (debug)
					log.debug("Calling bind on " + listeners[i] + " w/ reference " + reference);
				try {
					listeners[i].bind(serviceProxy, (Map) properties);
				}
				catch (Exception ex) {
					log.warn("Bind method on listener " + listeners[i] + " threw exception ", ex);
				}
				if (debug)
					log.debug("Called bind on " + listeners[i] + " w/ reference " + reference);
			}
		}
	}

	public static void callListenersUnbind(BundleContext context, Object serviceProxy, ServiceReference reference,
			OsgiServiceLifecycleListener[] listeners) {
		if (!ObjectUtils.isEmpty(listeners)) {
			boolean debug = log.isDebugEnabled();
			// get a Dictionary implementing a Map
			Dictionary properties = OsgiServiceReferenceUtils.getServicePropertiesSnapshot(reference);
			for (int i = 0; i < listeners.length; i++) {
				if (debug)
					log.debug("Calling unbind on " + listeners[i] + " w/ reference " + reference);
				try {
					listeners[i].unbind(serviceProxy, (Map) properties);
				}
				catch (Exception ex) {
					log.warn("Unbind method on listener " + listeners[i] + " threw exception ", ex);
				}
				if (debug)
					log.debug("Called unbind on " + listeners[i] + " w/ reference " + reference);
			}
		}
	}
}
