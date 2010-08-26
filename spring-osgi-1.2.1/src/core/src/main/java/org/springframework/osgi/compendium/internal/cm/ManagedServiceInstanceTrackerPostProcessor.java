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

package org.springframework.osgi.compendium.internal.cm;

import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.osgi.context.BundleContextAware;

/**
 * Post processor tracking the creation and destruction of managed service
 * instances. The instances tracked are subject to Configuration Admin based
 * injection.
 * 
 * @author Costin Leau
 * 
 */
public class ManagedServiceInstanceTrackerPostProcessor implements BeanFactoryAware, BundleContextAware,
		InitializingBean, BeanPostProcessor, DestructionAwareBeanPostProcessor, DisposableBean {

	private final String trackedBean;
	private DefaultManagedServiceBeanManager managedServiceManager;
	private String pid;
	private String updateMethod;
	private UpdateStrategy updateStrategy;
	private BundleContext bundleContext;
	private BeanFactory beanFactory;


	public ManagedServiceInstanceTrackerPostProcessor(String beanNameToTrack) {
		this.trackedBean = beanNameToTrack;
	}

	public void afterPropertiesSet() throws Exception {
		ConfigurationAdminManager cam = new ConfigurationAdminManager(pid, bundleContext);
		managedServiceManager = new DefaultManagedServiceBeanManager(updateStrategy, updateMethod, cam, beanFactory);
	}

	public void destroy() throws Exception {
		managedServiceManager.destroy();
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		// register the instance (if needed)
		if (trackedBean.equals(beanName)) {
			return managedServiceManager.register(bean);
		}
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		if (trackedBean.equals(beanName)) {
			managedServiceManager.unregister(bean);
		}
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * Sets the pid for the configuration manager.
	 * 
	 * @param pid The pid to set.
	 */
	public void setPersistentId(String pid) {
		this.pid = pid;
	}

	/**
	 * Sets the method name, for bean-managed update strategy.
	 * 
	 * @param updateMethod The updateMethod to set.
	 */
	public void setUpdateMethod(String methodName) {
		this.updateMethod = methodName;
	}

	/**
	 * Sets the update strategy.
	 * 
	 * @param updateStrategy The updateStrategy to set.
	 */
	public void setUpdateStrategy(UpdateStrategy updateStrategy) {
		this.updateStrategy = updateStrategy;
	}
}