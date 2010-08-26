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
 * Update callback that encapsulates the update-method/strategy. In general, it
 * is expected that the callbacks are thread-safe (and thus
 * stateless/immutable).
 * 
 * @author Costin Leau
 */
interface UpdateCallback {

	/**
	 * Performs an update using the given properties. It's up to each
	 * implementation to decide what other parameters are needed.
	 * 
	 * @param instance the updated instance
	 * @param properties update properties
	 */
	void update(Object instance, Map properties);
}
