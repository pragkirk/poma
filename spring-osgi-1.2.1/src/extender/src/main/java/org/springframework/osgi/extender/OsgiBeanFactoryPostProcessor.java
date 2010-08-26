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

package org.springframework.osgi.extender;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Extender hook that allows custom modification of an application context's
 * bean definitions. New beans can be created, removed or existing definitions
 * modified.
 * 
 * <p/> Similar in functionality with Spring's BeanFactoryPostProcessor, this
 * interface also considers the BundleContext in which the beanFactory runs.
 * 
 * <p/>Just like the BeanFactoryPostProcessor, the post processing happens
 * during the creation of the bean factory but before any beans (including
 * declared BeanFactoryPostProcessors) are initialized.
 * 
 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor
 * @see BundleContext
 * 
 * @author Costin Leau
 */
public interface OsgiBeanFactoryPostProcessor {

	/**
	 * 
	 * Modifies the application context's internal bean factory after its
	 * standard initialization. All bean definitions will have been loaded, but
	 * no beans will have been instantiated yet. This allows for overriding or
	 * adding properties even to eager-initializing beans.
	 * 
	 * @param bundleContext bundle
	 * @param beanFactory the bean factory used by the application context
	 * @throws BeansException in case of factory errors
	 * @throws InvalidSyntaxException in case of OSGi filters errors
	 * @throws BundleException in case of OSGi bundle errors
	 */
	void postProcessBeanFactory(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory)
			throws BeansException, InvalidSyntaxException, BundleException;
}
