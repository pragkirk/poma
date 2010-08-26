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

import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.util.Assert;

/**
 * Adapter class between Spring {@link ApplicationEventMulticaster} and
 * Spring-DM {@link OsgiBundleApplicationContextEventMulticaster}. Allows
 * reusage (especially considering the contractual similarities between the two
 * interfaces) of existing implementations for propagating Spring-DM events.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiBundleApplicationContextEventMulticasterAdapter implements
		OsgiBundleApplicationContextEventMulticaster {

	private final ApplicationEventMulticaster delegatedMulticaster;


	/**
	 * Constructs a new
	 * <code>OsgiBundleApplicationContextEventMulticasterAdapter</code>
	 * instance.
	 * 
	 * @param delegatedMulticaster
	 */
	public OsgiBundleApplicationContextEventMulticasterAdapter(ApplicationEventMulticaster delegatedMulticaster) {
		Assert.notNull(delegatedMulticaster);
		this.delegatedMulticaster = delegatedMulticaster;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The given listener is wrapped with an adapter class that delegates the
	 * equals/hashcode methods to the wrapped listener instance. However,
	 * depending on the equals implementation, this might affect the object
	 * identity.
	 */
	public void addApplicationListener(OsgiBundleApplicationContextListener osgiListener) {
		Assert.notNull(osgiListener);
		delegatedMulticaster.addApplicationListener(new ApplicationListenerAdapter(osgiListener));
	}

	public void multicastEvent(OsgiBundleApplicationContextEvent osgiEvent) {
		delegatedMulticaster.multicastEvent(osgiEvent);
	}

	public void removeAllListeners() {
		delegatedMulticaster.removeAllListeners();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The given listener is wrapped with an adapter class that delegates the
	 * equals/hashcode methods to the wrapped listener instance. However,
	 * depending on the equals implementation, this might affect the object
	 * identity.
	 */
	public void removeApplicationListener(OsgiBundleApplicationContextListener osgiListener) {
		Assert.notNull(null);
		delegatedMulticaster.removeApplicationListener(new ApplicationListenerAdapter(osgiListener));
	}
}
