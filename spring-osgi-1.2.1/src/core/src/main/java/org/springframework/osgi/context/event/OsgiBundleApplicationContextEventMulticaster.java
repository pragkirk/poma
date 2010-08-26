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

package org.springframework.osgi.context.event;

/**
 * Interface to be implemented by objects that can manage a number of
 * {@link OsgiBundleApplicationContextListener}s, and publish events to them.
 * 
 * <p/> The contract of this interface is very similar to that of
 * {@link org.springframework.context.event.ApplicationEventMulticaster} except
 * the type of listeners this multicaster can handle. Different from the
 * aforementioned class, this interface is used for broadcasting life cycle
 * events of application contexts started inside an OSGi environment, to outside
 * entities. This normally implies that the entities as well as the multicaster
 * are not managed by the application context triggering the event (so that a
 * destruction event can be properly propagated).
 * 
 * @see org.springframework.context.event.ApplicationEventMulticaster
 * 
 * @author Costin Leau
 */
public interface OsgiBundleApplicationContextEventMulticaster {

	/**
	 * Add an OSGi listener to be notified of all events.
	 * 
	 * @param osgiListener the listener to add
	 */
	void addApplicationListener(OsgiBundleApplicationContextListener osgiListener);

	/**
	 * Remove an OSGi listener from the notification list.
	 * 
	 * @param osgiListener the listener to remove
	 */
	void removeApplicationListener(OsgiBundleApplicationContextListener osgiListener);

	/**
	 * Remove all listeners registered with this multicaster. It will perform no
	 * action on event notification until more listeners are registered.
	 */
	void removeAllListeners();

	/**
	 * Multicast the given application event to appropriate listeners.
	 * 
	 * @param osgiListener the event to multicast
	 */
	void multicastEvent(OsgiBundleApplicationContextEvent osgiListener);
}
