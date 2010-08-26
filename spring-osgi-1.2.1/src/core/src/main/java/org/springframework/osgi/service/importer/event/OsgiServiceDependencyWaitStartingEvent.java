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
 * Dedicated event for OSGi dependencies that are imported in a timed manner.
 * The event indicates that a dependency is missing and a bean inside the
 * application context will start waiting for it, for a specified amount of time
 * (given as a maximum).
 * 
 * <p/> Note that the actual waiting starts shortly after the event is
 * dispatched however, there are no guarantees on when this will happen as it
 * depends on the number of listeners interested in this event (and the amount
 * of work done once the event is received).
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceDependencyWaitStartingEvent extends OsgiServiceDependencyEvent {

	private final long timeToWait;


	/**
	 * Constructs a new <code>OsgiServiceDependencyWaitStartingEvent</code>
	 * instance.
	 * 
	 * @param source event source (usually the service importer)
	 * @param dependency dependency description
	 * @param timeToWait wait duration
	 */
	public OsgiServiceDependencyWaitStartingEvent(Object source, OsgiServiceDependency dependency, long timeToWait) {
		super(source, dependency);
		this.timeToWait = timeToWait;
	}

	/**
	 * Returns the time (in milliseconds) the source will wait for the OSGi
	 * service to appear.
	 * 
	 * @return Returns the timeToWait
	 */
	public long getTimeToWait() {
		return timeToWait;
	}
}
