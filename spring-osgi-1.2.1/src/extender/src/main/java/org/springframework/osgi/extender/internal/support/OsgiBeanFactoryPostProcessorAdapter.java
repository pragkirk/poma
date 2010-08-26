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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.osgi.extender.OsgiBeanFactoryPostProcessor;

/**
 * Simple adapter for wrapping OsgiBeanPostProcessors to normal Spring post
 * processors.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiBeanFactoryPostProcessorAdapter implements BeanFactoryPostProcessor {

	/** logger */
	private static final Log log = LogFactory.getLog(OsgiBeanFactoryPostProcessorAdapter.class);

	private final BundleContext bundleContext;

	private List osgiPostProcessors;


	public OsgiBeanFactoryPostProcessorAdapter(BundleContext bundleContext, List postProcessors) {
		this.bundleContext = bundleContext;
		this.osgiPostProcessors = postProcessors;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		boolean trace = log.isTraceEnabled();

		Exception processingException = null;

		for (Iterator iterator = osgiPostProcessors.iterator(); iterator.hasNext();) {
			OsgiBeanFactoryPostProcessor osgiPostProcessor = (OsgiBeanFactoryPostProcessor) iterator.next();
			if (trace)
				log.trace("Calling OsgiBeanFactoryPostProcessor " + osgiPostProcessor + " for bean factory "
						+ beanFactory);

			try {
				osgiPostProcessor.postProcessBeanFactory(bundleContext, beanFactory);
			}
			catch (InvalidSyntaxException ex) {
				processingException = ex;
			}
			catch (BundleException ex) {
				processingException = ex;
			}

			if (processingException != null) {
				if (log.isDebugEnabled())
					log.debug("PostProcessor " + osgiPostProcessor + " threw exception", processingException);
				throw new FatalBeanException("Error encountered while executing OSGi post processing",
					processingException);
			}
		}
	}
}