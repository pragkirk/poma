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

package org.springframework.osgi.extender.support;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.springframework.osgi.extender.OsgiApplicationContextCreator;
import org.springframework.util.Assert;

/**
 * Useful {@link OsgiApplicationContextCreator} implementation that dictates
 * whether the default application context used by the Spring-DM extender should
 * be created (or not) based on a <code>boolean</code> value. This allows
 * clients to handle only the bundleContext filtering while being decoupled from
 * the context creation process:
 * 
 * <pre class="code">
 * 
 * ConditionalApplicationContextCreator creator = new ConditionalApplicationContextCreator();
 * 
 * creator.setFilter(new ConditionalApplicationContextCreator.BundleContextFilter() {
 * 	// filter bundles with no copyright
 * 	public boolean matches(BundleContext bundleContext) {
 * 		return bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_COPYRIGHT) != null)
 * 	}
 * }
 * 
 * creator.createApplicationContext(bundleContext); 
 * </pre>
 * 
 * @see OsgiApplicationContextCreator
 * @author Costin Leau
 */
public class ConditionalApplicationContextCreator implements OsgiApplicationContextCreator, InitializingBean {

	/**
	 * Callback used to filter the bundle contexts for which the default
	 * application contexts are created.
	 * 
	 * @author Costin Leau
	 * 
	 */
	public static interface BundleContextFilter {

		/**
		 * Determines if the given bundle context matches the filter criteria.
		 * 
		 * @param bundleContext the OSGi bundle context to check
		 * @return true if the bundle context matches, false otherwise.
		 */
		boolean matches(BundleContext bundleContext);
	}


	private BundleContextFilter filter;

	private OsgiApplicationContextCreator delegatedContextCreator;


	public void afterPropertiesSet() throws Exception {
		Assert.notNull(filter, "filter property is required");
		if (delegatedContextCreator == null)
			delegatedContextCreator = new DefaultOsgiApplicationContextCreator();
	}

	public DelegatedExecutionOsgiBundleApplicationContext createApplicationContext(BundleContext bundleContext)
			throws Exception {
		if (filter.matches(bundleContext))
			return delegatedContextCreator.createApplicationContext(bundleContext);
		else
			return null;
	}

	/**
	 * Sets the {@link BundleContextFilter} used by this context creator.
	 * 
	 * @param filter The bundle context filter to set.
	 */
	public void setFilter(BundleContextFilter filter) {
		this.filter = filter;
	}

	/**
	 * Sets the {@link OsgiApplicationContextCreator} used by this context
	 * creator for the actual creation. If none is specified,
	 * {@link DefaultOsgiApplicationContextCreator} is used.
	 * 
	 * @param delegatedContextCreator the instance used for creating the
	 * application context
	 */
	public void setDelegatedApplicationContextCreator(OsgiApplicationContextCreator delegatedContextCreator) {
		this.delegatedContextCreator = delegatedContextCreator;
	}
}
