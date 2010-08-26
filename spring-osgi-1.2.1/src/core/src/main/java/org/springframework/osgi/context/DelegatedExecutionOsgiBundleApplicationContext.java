/*
 * Copyright 2006-2008 the original author or authors. Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.springframework.osgi.context;

import org.springframework.osgi.context.event.OsgiBundleApplicationContextEventMulticaster;

/**
 * Interface that redirect the application context crucial methods to a third
 * party executor to allow the initialization to be executed in stages. The
 * interface splits the <code>refresh</code> method in two parts:
 * {@link #startRefresh()} and {@link #completeRefresh()}.
 * 
 * <p/><strong>Note:</strong> This interface is intended for usage only inside
 * Spring-DM framework. Relying on this interface is highly discouraged.
 * 
 * @see DependencyAwareBeanFactoryPostProcessor
 * @see DependencyInitializationAwareBeanPostProcessor
 * @author Costin Leau
 */
public interface DelegatedExecutionOsgiBundleApplicationContext extends ConfigurableOsgiBundleApplicationContext {

	/**
	 * Non-delegated refresh operation (execute {@link #refresh} in the
	 * <em>traditional</em> way).
	 * 
	 * @see org.springframework.context.ConfigurableApplicationContext#refresh()
	 */
	void normalRefresh();

	/**
	 * Non-delegated close operation (execute {@link #close} in the
	 * <em>traditional</em> way).
	 * 
	 * @see org.springframework.context.ConfigurableApplicationContext#close()
	 */
	void normalClose();

	/**
	 * First phase of the refresh. Normally, this just prepares the
	 * <code>beanFactory</code> but does not instantiates any beans.
	 */
	void startRefresh();

	/**
	 * The second, last phase of the refresh. Executes after a certain
	 * condition, imposed by the executor, has been met. Finishes the rest of
	 * the <code>refresh</code> operation. Normally, this operations performs
	 * most of the <code>refresh work</code>, such as instantiating
	 * singletons.
	 */
	void completeRefresh();

	/**
	 * Assigns the {@link OsgiBundleApplicationContextExecutor} for this
	 * delegated context.
	 * 
	 * @param executor the executor of this application context, to which the
	 *        <code>refresh</code> method is delegated to
	 */
	void setExecutor(OsgiBundleApplicationContextExecutor executor);

	/**
	 * Synchronization monitor for this
	 * {@link org.springframework.context.ApplicationContext} in case multiple
	 * threads can work with the application context lifecycle.
	 * 
	 * @return monitor for this application context
	 * @deprecated any synchronization should be performed internally by the
	 *             application context. This method will be removed w/o a
	 *             replacement in future releases.
	 */
	Object getMonitor();

	/**
	 * Allows a delegated {@link OsgiBundleApplicationContextEventMulticaster},
	 * external to the application context, to be used for sending OSGi
	 * application context events regarding the application context life cycle.
	 * This method is mainly intended for monitoring the context lifecycle by
	 * third parties (such as the OSGi extender). It's up to the implementation
	 * to decide whether this setter method is required or not.
	 * 
	 * @param multicaster the application multicaster used for sending events
	 *        triggered by the delegated execution.
	 * @see org.springframework.osgi.context.event.OsgiBundleApplicationContextEvent
	 */
	void setDelegatedEventMulticaster(OsgiBundleApplicationContextEventMulticaster multicaster);

	/**
	 * Returns the OSGi event multicaster (if any) associated with this
	 * application context.
	 * 
	 * @return the OSGi event multicaster associated with this context
	 */
	OsgiBundleApplicationContextEventMulticaster getDelegatedEventMulticaster();
}
