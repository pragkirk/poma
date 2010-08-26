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
 *
 * Created on 25-Jan-2006 by Adrian Colyer
 */

package org.springframework.osgi.util;

import org.osgi.framework.ServiceRegistration;

/**
 * Utility class offering easy access to OSGi services.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
public abstract class OsgiServiceUtils {

	/**
	 * Unregisters the given service registration from the given bundle. Returns
	 * true if the unregistration process succeeded, false otherwise.
	 * 
	 * @param registration service registration (can be null)
	 * @return true if the unregistration successeded, false otherwise
	 */
	public static boolean unregisterService(ServiceRegistration registration) {
		try {
			if (registration != null) {
				registration.unregister();
				return true;
			}
		}
		catch (IllegalStateException alreadyUnregisteredException) {
		}
		return false;
	}

}
