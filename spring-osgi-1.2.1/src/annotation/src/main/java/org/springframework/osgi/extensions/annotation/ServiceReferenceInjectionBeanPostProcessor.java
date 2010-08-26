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

package org.springframework.osgi.extensions.annotation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.service.importer.support.Cardinality;
import org.springframework.osgi.service.importer.support.CollectionType;
import org.springframework.osgi.service.importer.support.ImportContextClassLoader;
import org.springframework.osgi.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
import org.springframework.util.ReflectionUtils;

/**
 * <code>BeanPostProcessor</code> that processed annotation to inject
 * Spring-DM managed OSGi services.
 * 
 * @author Andy Piper
 */
public class ServiceReferenceInjectionBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements
		BundleContextAware, BeanFactoryAware, BeanClassLoaderAware {

	private BundleContext bundleContext;

	private static Log logger = LogFactory.getLog(ServiceReferenceInjectionBeanPostProcessor.class);

	private BeanFactory beanFactory;

	private ClassLoader classLoader;


	private abstract static class ImporterCallAdapter {

		static void setInterfaces(Object importer, Class<?>[] classes) {
			if (importer instanceof OsgiServiceProxyFactoryBean)
				((OsgiServiceProxyFactoryBean) importer).setInterfaces(classes);
			else
				((OsgiServiceCollectionProxyFactoryBean) importer).setInterfaces(classes);
		}

		static void setBundleContext(Object importer, BundleContext context) {
			((BundleContextAware) importer).setBundleContext(context);
		}

		static void setBeanClassLoader(Object importer, ClassLoader cl) {
			((BeanClassLoaderAware) importer).setBeanClassLoader(cl);
		}

		static void setCardinality(Object importer, Cardinality cardinality) {
			if (importer instanceof OsgiServiceProxyFactoryBean)
				((OsgiServiceProxyFactoryBean) importer).setCardinality(cardinality);
			else
				((OsgiServiceCollectionProxyFactoryBean) importer).setCardinality(cardinality);
		}

        static void setGreedyProxying(Object importer, boolean greedy) {
            if (importer instanceof OsgiServiceCollectionProxyFactoryBean) {
                ((OsgiServiceCollectionProxyFactoryBean) importer).setGreedyProxying(greedy);
            }
        }

		static void afterPropertiesSet(Object importer) throws Exception {
			((InitializingBean) importer).afterPropertiesSet();
		}

		static void setFilter(Object importer, String filter) throws Exception {
			if (importer instanceof OsgiServiceProxyFactoryBean)
				((OsgiServiceProxyFactoryBean) importer).setFilter(filter);
			else
				((OsgiServiceCollectionProxyFactoryBean) importer).setFilter(filter);
		}

		static void setContextClassLoader(Object importer, ImportContextClassLoader ccl) {
			if (importer instanceof OsgiServiceProxyFactoryBean)
				((OsgiServiceProxyFactoryBean) importer).setContextClassLoader(ccl);
			else
				((OsgiServiceCollectionProxyFactoryBean) importer).setContextClassLoader(ccl);
		}

		static void setServiceBean(Object importer, String name) {
			if (importer instanceof OsgiServiceProxyFactoryBean)
				((OsgiServiceProxyFactoryBean) importer).setServiceBeanName(name);
			else
				((OsgiServiceCollectionProxyFactoryBean) importer).setServiceBeanName(name);
		}
	}


	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * process FactoryBean created objects, since these will not have had
	 * services injected.
	 * 
	 * @param bean
	 * @param beanName
	 */
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
		if (logger.isDebugEnabled())
			logger.debug("processing [" + bean.getClass().getName() + ", " + beanName + "]");
		// Catch FactoryBean created instances.
		if (!(bean instanceof FactoryBean) && beanFactory.containsBean(BeanFactory.FACTORY_BEAN_PREFIX + beanName)) {
			injectServices(bean, beanName);
		}
		return bean;
	}

	/* private version of the injector can use */
	private void injectServices(final Object bean, final String beanName) {
		ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {

			public void doWith(Method method) {
				ServiceReference s = AnnotationUtils.getAnnotation(method, ServiceReference.class);
				if (s != null && method.getParameterTypes().length == 1) {
					try {
						if (logger.isDebugEnabled())
							logger.debug("Processing annotation [" + s + "] for [" + bean.getClass().getName() + "."
									+ method.getName() + "()] on bean [" + beanName + "]");
						method.invoke(bean, getServiceImporter(s, method, beanName).getObject());
					}
					catch (Exception e) {
						throw new IllegalArgumentException("Error processing service annotation", e);
					}
				}
			}
		});
	}

	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean,
			String beanName) throws BeansException {

		MutablePropertyValues newprops = new MutablePropertyValues(pvs);
		for (PropertyDescriptor pd : pds) {
			ServiceReference s = hasServiceProperty(pd);
			if (s != null && !pvs.contains(pd.getName())) {
				try {
					if (logger.isDebugEnabled())
						logger.debug("Processing annotation [" + s + "] for [" + beanName + "." + pd.getName() + "]");
					FactoryBean importer = getServiceImporter(s, pd.getWriteMethod(), beanName);
					// BPPs are created in stageOne(), even though they are run in stageTwo(). This check means that
					// the call to getObject() will not fail with ServiceUnavailable. This is safe to do because
					// ServiceReferenceDependencyBeanFactoryPostProcessor will ensure that mandatory services are
					// satisfied before stageTwo() is run.
					if (bean instanceof BeanPostProcessor) {
						ImporterCallAdapter.setCardinality(importer, Cardinality.C_0__1);
					}
					newprops.addPropertyValue(pd.getName(), importer.getObject());
				}
				catch (Exception e) {
					throw new FatalBeanException("Could not create service reference", e);
				}
			}
		}
		return newprops;
	}

	private FactoryBean getServiceImporter(ServiceReference s, Method writeMethod, String beanName) throws Exception {
		// Invocations will block here, so although the ApplicationContext is
		// created nothing will
		// proceed until all the dependencies are satisfied.
		Class<?>[] params = writeMethod.getParameterTypes();
		if (params.length != 1) {
			throw new IllegalArgumentException("Setter for [" + beanName + "] must have only one argument");
		}
		if (Collection.class.isAssignableFrom(params[0])) {
			return getServiceProperty(new OsgiServiceCollectionProxyFactoryBean(), s, writeMethod, beanName);
		}
		else {
			return getServiceProperty(new OsgiServiceProxyFactoryBean(), s, writeMethod, beanName);
		}
	}

	private boolean impliedServiceType(ServiceReference s) {
		return (s.serviceTypes() == null || s.serviceTypes().length == 0 || (s.serviceTypes().length == 1 && s.serviceTypes()[0].equals(ServiceReference.class)));
	}

	// Package protected for testing
	private FactoryBean getServicePropertyInternal(FactoryBean pfb, ServiceReference s, Method writeMethod,
			String beanName) throws Exception {
		if (s.filter().length() > 0) {
			ImporterCallAdapter.setFilter(pfb, s.filter());
		}
		if (impliedServiceType(s)) {
			Class<?>[] params = writeMethod.getParameterTypes();
			if (params.length != 1) {
				throw new IllegalArgumentException("Setter for [" + beanName + "] must have only one argument");
			}
			if (Collection.class.isAssignableFrom(params[0])) {
				throw new IllegalArgumentException("Cannot infer type for collection-based reference [" + beanName
						+ "]");
			}
			else {
				ImporterCallAdapter.setInterfaces(pfb, new Class<?>[] { params[0] });
			}
		}
		else {
			ImporterCallAdapter.setInterfaces(pfb, s.serviceTypes());
		}
		ImporterCallAdapter.setCardinality(pfb, s.cardinality().toCardinality());
		ImporterCallAdapter.setContextClassLoader(pfb, s.contextClassLoader().toImportContextClassLoader());
		ImporterCallAdapter.setBundleContext(pfb, bundleContext);

		if (s.serviceBeanName().length() > 0) {
			ImporterCallAdapter.setServiceBean(pfb, s.serviceBeanName());
		}
		ImporterCallAdapter.setBeanClassLoader(pfb, classLoader);
		ImporterCallAdapter.afterPropertiesSet(pfb);
		return pfb;
	}

	/* package */FactoryBean getServiceProperty(OsgiServiceProxyFactoryBean pfb, ServiceReference s,
			Method writeMethod, String beanName) throws Exception {
		pfb.setTimeout(s.timeout());
		return getServicePropertyInternal(pfb, s, writeMethod, beanName);
	}

	/* package */FactoryBean getServiceProperty(OsgiServiceCollectionProxyFactoryBean pfb, ServiceReference s,
			Method writeMethod, String beanName) throws Exception {
		Class<?>[] params = writeMethod.getParameterTypes();
		if (SortedSet.class.isAssignableFrom(params[0])) {
			pfb.setCollectionType(CollectionType.SORTED_SET);
		}
		else if (Set.class.isAssignableFrom(params[0])) {
			pfb.setCollectionType(CollectionType.SET);
		}
		else if (List.class.isAssignableFrom(params[0])) {
			pfb.setCollectionType(CollectionType.LIST);
		}
		else {
			throw new IllegalArgumentException("Setter for [" + beanName
					+ "] does not have a valid Collection type argument");
		}
		return getServicePropertyInternal(pfb, s, writeMethod, beanName);
	}

	protected ServiceReference hasServiceProperty(PropertyDescriptor propertyDescriptor) {
		Method setter = propertyDescriptor.getWriteMethod();
		return setter != null ? AnnotationUtils.getAnnotation(setter, ServiceReference.class) : null;
	}

	public void setBundleContext(BundleContext context) {
		this.bundleContext = context;

	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}
