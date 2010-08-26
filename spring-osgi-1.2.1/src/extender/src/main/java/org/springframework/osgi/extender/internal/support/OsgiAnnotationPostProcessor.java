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

package org.springframework.osgi.extender.internal.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.osgi.OsgiException;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.extender.OsgiBeanFactoryPostProcessor;
import org.springframework.osgi.util.OsgiStringUtils;

/**
 * Post processor used for processing Spring-DM annotations.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiAnnotationPostProcessor implements OsgiBeanFactoryPostProcessor {

	/** logger */
	private static final Log log = LogFactory.getLog(OsgiAnnotationPostProcessor.class);

	/** service reference bpp */
	private static final String ANNOTATION_BPP_CLASS = "org.springframework.osgi.extensions.annotation.ServiceReferenceInjectionBeanPostProcessor";


	public void postProcessBeanFactory(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory)
			throws BeansException, OsgiException {

		Bundle bundle = bundleContext.getBundle();
		try {
			// Try and load the annotation code using the bundle classloader
			Class annotationBppClass = bundle.loadClass(ANNOTATION_BPP_CLASS);
			// instantiate the class
			final BeanPostProcessor annotationBeanPostProcessor = (BeanPostProcessor) BeanUtils.instantiateClass(annotationBppClass);

			// everything went okay so configure the BPP and add it to the BF
			((BeanFactoryAware) annotationBeanPostProcessor).setBeanFactory(beanFactory);
			((BeanClassLoaderAware) annotationBeanPostProcessor).setBeanClassLoader(beanFactory.getBeanClassLoader());
			((BundleContextAware) annotationBeanPostProcessor).setBundleContext(bundleContext);
			beanFactory.addBeanPostProcessor(annotationBeanPostProcessor);
		}
		catch (ClassNotFoundException exception) {
			log.info("Spring-DM annotation package could not be loaded from bundle ["
					+ OsgiStringUtils.nullSafeNameAndSymName(bundle) + "]; annotation processing disabled...");
			if (log.isDebugEnabled())
				log.debug("Cannot load annotation injection processor", exception);
		}
	}
}
