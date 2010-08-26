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

import org.osgi.framework.Bundle;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.util.Assert;

/**
 * Base class for events raised for an <code>ApplicationContext</code> created
 * inside an OSGi environment. Normally, events of this type are raised by the
 * OSGi extender to notify 3rd parties, external to the context, about changes
 * in the life cycle of the application context.
 * 
 * <p/><b>Note:</b>While the context source is likely to be an implementation
 * of {@link ConfigurableOsgiBundleApplicationContext}, this is not mandatory
 * (it's entirely possible to have a non-OSGi aware {@link ApplicationContext}
 * implementation).
 * 
 * @author Costin Leau
 */
public abstract class OsgiBundleApplicationContextEvent extends ApplicationContextEvent {

	private final Bundle bundle;


	/**
	 * Constructs a new <code>OsgiApplicationContextEvent</code> instance.
	 * 
	 * @param source the <code>ConfigurableOsgiBundleApplicationContext</code>
	 * that the event is raised for (must not be <code>null</code>)
	 */
	public OsgiBundleApplicationContextEvent(ApplicationContext source, Bundle bundle) {
		super(source);
		Assert.notNull(bundle);
		this.bundle = bundle;
	}

	/**
	 * Returns the OSGi {@link Bundle} associated with the application context
	 * that triggers the event.
	 * 
	 * @return associated OSGi bundle
	 */
	public Bundle getBundle() {
		return bundle;
	}
}
