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
 * Event raised when an OSGi service dependency could not be found in a certain
 * amount of time. Normally thrown by OSGi importers, this event allows
 * notifications of potential failures inside the application context due to
 * missing (but required) OSGi dependencies.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceDependencyWaitTimedOutEvent extends OsgiServiceDependencyEvent {

	private final long elapsedTime;


	/**
	 * Constructs a new <code>OsgiServiceDependencyWaitTimedOutEvent</code>
	 * instance.
	 * 
	 * @param source event source (usually a service importer)
	 * @param dependency service dependency description
	 * @param elapsedTime time spent waiting
	 */
	public OsgiServiceDependencyWaitTimedOutEvent(Object source, OsgiServiceDependency dependency, long elapsedTime) {
		super(source, dependency);
		this.elapsedTime = elapsedTime;
	}

	/**
	 * Returns the time (in milliseconds) the source waited for OSGi service to
	 * appear before failing.
	 * 
	 * @return Returns the timeToWait
	 */
	public long getElapsedTime() {
		return elapsedTime;
	}
}
