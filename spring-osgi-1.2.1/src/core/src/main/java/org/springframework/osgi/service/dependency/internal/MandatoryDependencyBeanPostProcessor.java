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

package org.springframework.osgi.service.dependency.internal;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.osgi.service.exporter.support.OsgiServiceFactoryBean;
import org.springframework.osgi.service.exporter.support.internal.controller.ExporterInternalActions;
import org.springframework.osgi.service.exporter.support.internal.controller.ExporterControllerUtils;

/**
 * BeanPostProcessor registered for detecting the dependency between service
 * importer and service exporters. Besides bean detection, this component also
 * listens to specific importer events to determine whether a potential
 * associated exporter needs to be disabled temporarily.
 * 
 * @author Costin Leau
 * 
 */
public class MandatoryDependencyBeanPostProcessor implements BeanFactoryAware, BeanPostProcessor, DestructionAwareBeanPostProcessor {

	private MandatoryServiceDependencyManager manager;
	private ConfigurableBeanFactory beanFactory;


	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof OsgiServiceFactoryBean) {
			manager.addServiceExporter(bean, beanName);
		}
		return bean;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		// disable publication until all the dependencies have been fulfilled
		// make sure we don't consider nested beans
		if (bean instanceof OsgiServiceFactoryBean && beanFactory.containsBean(beanName)) {
			String exporterName = beanName;
			if (beanFactory.isFactoryBean(beanName)) {
				exporterName = BeanFactory.FACTORY_BEAN_PREFIX + beanName;
			}
			// if it's a singleton, then disable publication, otherwise ignore it
			if (beanFactory.isSingleton(exporterName)) {
				// get controller
				ExporterInternalActions controller = ExporterControllerUtils.getControllerFor(bean);
				controller.registerServiceAtStartup(false);
			}
		}
		return bean;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		DefaultMandatoryDependencyManager manager = new DefaultMandatoryDependencyManager();
		manager.setBeanFactory(beanFactory);
		this.manager = manager;
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		if (bean instanceof OsgiServiceFactoryBean) {
			manager.removeServiceExporter(bean, beanName);
		}
	}
}