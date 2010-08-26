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

package org.springframework.osgi.config.internal.adapter;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.service.importer.ImportedOsgiServiceProxy;
import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;
import org.springframework.osgi.util.internal.ReflectionUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * OsgiServiceLifecycleListener wrapper for custom beans, useful when custom
 * methods are being used.
 * 
 * <p/> <strong>Note:</strong> To support cyclic injection, this adapter does
 * dependency lookup for the actual listener.
 * 
 * @author Costin Leau
 */
public class OsgiServiceLifecycleListenerAdapter implements OsgiServiceLifecycleListener, InitializingBean,
		BeanFactoryAware {

	private static final Log log = LogFactory.getLog(OsgiServiceLifecycleListenerAdapter.class);

	/**
	 * Map of methods keyed by the first parameter which indicates the service
	 * type expected.
	 */
	private Map bindMethods, unbindMethods;

	/**
	 * anyName(ServiceReference reference) method signature.
	 */
	private Method bindReference, unbindReference;

	private String bindMethod, unbindMethod;

	/** does the target implement the listener interface */
	private boolean isLifecycleListener;

	/** target bean factory */
	private BeanFactory beanFactory;

	/** used when dealing with a cycle */
	private String targetBeanName;

	/** target object (can be null at first when dealing with a cycle */
	private Object target;

	/** init flag */
	private boolean initialized;


	public void afterPropertiesSet() {
		Assert.notNull(beanFactory);
		Assert.isTrue(target != null || StringUtils.hasText(targetBeanName),
			"one of 'target' or 'targetBeanName' properties has to be set");

		if (target != null)
			initialized = true;

		// do validation (on the target type)
		initialize();
		// postpone target initialization until one of bind/unbind method is called
	}

	private void retrieveTarget() {
		target = beanFactory.getBean(targetBeanName);
		initialized = true;
	}

	/**
	 * Initialise adapter. Determine custom methods and do validation.
	 */
	private void initialize() {

		Class clazz = (target == null ? beanFactory.getType(targetBeanName) : target.getClass());

		isLifecycleListener = OsgiServiceLifecycleListener.class.isAssignableFrom(clazz);
		if (isLifecycleListener)
			if (log.isDebugEnabled())
				log.debug(clazz.getName() + " is a lifecycle listener");

		bindMethods = CustomListenerAdapterUtils.determineCustomMethods(clazz, bindMethod);

		if (StringUtils.hasText(bindMethod)) {
			// determine methods using ServiceReference signature
			bindReference = org.springframework.util.ReflectionUtils.findMethod(clazz, bindMethod,
				new Class[] { ServiceReference.class });

			if (bindReference != null)
				org.springframework.util.ReflectionUtils.makeAccessible(bindReference);

			else if (bindMethods.isEmpty()) {
				String beanName = (target == null ? "" : " bean [" + targetBeanName + "] ;");
				throw new IllegalArgumentException("Custom bind method [" + bindMethod
						+ "] (with proper signature) not found on " + beanName + "class " + clazz);
			}
		}

		unbindMethods = CustomListenerAdapterUtils.determineCustomMethods(clazz, unbindMethod);

		if (StringUtils.hasText(unbindMethod)) {
			unbindReference = org.springframework.util.ReflectionUtils.findMethod(clazz, unbindMethod,
				new Class[] { ServiceReference.class });

			if (unbindReference != null)
				org.springframework.util.ReflectionUtils.makeAccessible(unbindReference);

			else if (unbindMethods.isEmpty()) {
				String beanName = (target == null ? "" : " bean [" + targetBeanName + "] ;");
				throw new IllegalArgumentException("Custom unbind method [" + unbindMethod
						+ "] (with proper signature) not found on " + beanName + "class " + clazz);
			}
		}

		if (!isLifecycleListener
				&& (bindMethods.isEmpty() && unbindMethods.isEmpty() && bindReference == null && unbindReference == null))
			throw new IllegalArgumentException("target object needs to implement "
					+ OsgiServiceLifecycleListener.class.getName()
					+ " or custom bind/unbind methods have to be specified");
	}

	/**
	 * Invoke method with signature <code>bla(ServiceReference ref)</code>.
	 * 
	 * @param target
	 * @param method
	 * @param service
	 */
	private void invokeCustomServiceReferenceMethod(Object target, Method method, Object service) {
		if (method != null) {
			boolean trace = log.isTraceEnabled();

			// get the service reference
			// find the compatible types (accept null service)
			if (trace)
				log.trace("invoking listener custom method " + method);

			ServiceReference ref = (service != null ? ((ImportedOsgiServiceProxy) service).getServiceReference() : null);

			try {
				ReflectionUtils.invokeMethod(method, target, new Object[] { ref });
			}
			// make sure to log exceptions and continue with the
			// rest of
			// the listeners
			catch (Exception ex) {
				Exception cause = ReflectionUtils.getInvocationException(ex);
				log.warn("custom method [" + method + "] threw exception when passing service reference ["
						+ (service != null ? service.getClass().getName() : null) + "]", cause);
			}
		}
	}

	public void bind(Object service, Map properties) throws Exception {
		boolean trace = log.isTraceEnabled();
		if (trace)
			log.trace("invoking bind method for service " + service + " with props=" + properties);

		if (!initialized)
			retrieveTarget();

		// first call interface method (if it exists)
		if (isLifecycleListener) {
			if (trace)
				log.trace("invoking listener interface methods");

			try {
				((OsgiServiceLifecycleListener) target).bind(service, properties);
			}
			catch (Exception ex) {
				log.warn("standard bind method on [" + target.getClass().getName() + "] threw exception", ex);
			}
		}

		CustomListenerAdapterUtils.invokeCustomMethods(target, bindMethods, service, properties);
		invokeCustomServiceReferenceMethod(target, bindReference, service);
	}

	public void unbind(Object service, Map properties) throws Exception {
		boolean trace = log.isTraceEnabled();
		if (!initialized)
			retrieveTarget();

		if (trace)
			log.trace("invoking unbind method for service " + service + " with props=" + properties);

		// first call interface method (if it exists)
		if (isLifecycleListener) {
			if (trace)
				log.trace("invoking listener interface methods");
			try {
				((OsgiServiceLifecycleListener) target).unbind(service, properties);
			}
			catch (Exception ex) {
				log.warn("standard unbind method on [" + target.getClass().getName() + "] threw exception", ex);
			}
		}

		CustomListenerAdapterUtils.invokeCustomMethods(target, unbindMethods, service, properties);
		invokeCustomServiceReferenceMethod(target, unbindReference, service);
	}

	/**
	 * @param bindMethod The bindMethod to set.
	 */
	public void setBindMethod(String bindMethod) {
		this.bindMethod = bindMethod;
	}

	/**
	 * @param unbindMethod The unbindMethod to set.
	 */
	public void setUnbindMethod(String unbindMethod) {
		this.unbindMethod = unbindMethod;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public void setTargetBeanName(String targetName) {
		this.targetBeanName = targetName;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}