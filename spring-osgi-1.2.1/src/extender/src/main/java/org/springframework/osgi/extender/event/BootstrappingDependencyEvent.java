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

package org.springframework.osgi.extender.event;

import org.osgi.framework.Bundle;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEvent;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyEvent;
import org.springframework.util.Assert;

/**
 * Spring-DM Extender bootstrapping event. This event is used during the
 * application context discovery phase, before an application context is fully
 * initialized.
 * 
 * <p/> It can be used to receive status updates for contexts started by the
 * extender.
 * 
 * @author Costin Leau
 * 
 */
public class BootstrappingDependencyEvent extends OsgiBundleApplicationContextEvent {

	private final OsgiServiceDependencyEvent dependencyEvent;


	/**
	 * Constructs a new <code>BootstrappingDependencyEvent</code> instance.
	 * 
	 * @param source
	 */
	public BootstrappingDependencyEvent(ApplicationContext source, Bundle bundle, OsgiServiceDependencyEvent nestedEvent) {
		super(source, bundle);
		Assert.notNull(nestedEvent);
		this.dependencyEvent = nestedEvent;
	}

	/**
	 * Returns the nested, dependency event that caused the bootstrapping event
	 * to be raised.
	 * 
	 * @return associated dependency event
	 */
	public OsgiServiceDependencyEvent getDependencyEvent() {
		return dependencyEvent;
	}
}
