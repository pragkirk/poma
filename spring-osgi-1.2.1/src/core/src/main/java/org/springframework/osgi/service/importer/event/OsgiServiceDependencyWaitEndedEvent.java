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

package org.springframework.osgi.service.importer.event;

import org.springframework.osgi.service.importer.OsgiServiceDependency;

/**
 * Importer event indicating that the wait for a given dependency has ended
 * (successfully), namely the dependency was found before the time allocated for
 * it elapsed.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceDependencyWaitEndedEvent extends OsgiServiceDependencyEvent {

	private final long waitedTime;


	/**
	 * Constructs a new <code>OsgiServiceDependencyWaitEndedEvent</code>
	 * instance.
	 * 
	 * @param source event source (usually the service importer)
	 * @param dependency dependency description
	 * @param elapsedTime time to wait
	 */
	public OsgiServiceDependencyWaitEndedEvent(Object source, OsgiServiceDependency dependency, long elapsedTime) {
		super(source, dependency);
		this.waitedTime = elapsedTime;
	}

	/**
	 * Returns the time spent (in milliseconds) waiting, until service was found
	 * (and the dependency considered satisfied).
	 * 
	 * @return Returns the time (in milliseconds) spent waiting for the OSGi
	 * service to appear
	 */
	public long getElapsedTime() {
		return waitedTime;
	}
}
