
package org.springframework.osgi.extensions.annotation;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.osgi.extender.OsgiServiceDependencyFactory;
import org.springframework.osgi.service.exporter.OsgiServicePropertiesResolver;
import org.springframework.osgi.service.exporter.support.OsgiServiceFactoryBean;
import org.springframework.osgi.service.importer.OsgiServiceDependency;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
import org.springframework.osgi.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.springframework.osgi.util.OsgiFilterUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Calculate service dependencies for annotation-style injected proxies.
 *
 * @author Andy Piper
 */
class ServiceReferenceDependencyBeanFactoryPostProcessor implements OsgiServiceDependencyFactory {

	private static Log logger = LogFactory.getLog(ServiceReferenceDependencyBeanFactoryPostProcessor.class);


	public Collection<OsgiServiceDependency> getServiceDependencies(BundleContext bundleContext,
			ConfigurableListableBeanFactory beanFactory) throws BeansException, InvalidSyntaxException, BundleException {

		Set<OsgiServiceDependency> dependencies = new LinkedHashSet<OsgiServiceDependency>();

		String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
		for (String definitionName : beanDefinitionNames) {
			BeanDefinition definition = beanFactory.getBeanDefinition(definitionName);
			String className = definition.getBeanClassName();
			// Ignore internal stuff
			if (className == null
				|| className.equals(OsgiServiceProxyFactoryBean.class.getName())
				|| className.equals(OsgiServiceFactoryBean.class.getName())
				|| className.equals(OsgiServiceCollectionProxyFactoryBean.class.getName())) {
				continue;
			}
			try {
				Class<?> clazz = Class.forName(className, true, beanFactory.getBeanClassLoader());
				dependencies.addAll(getClassServiceDependencies(clazz, definitionName, definition));
			}
			catch (ClassNotFoundException cnfe) {
				if (logger.isWarnEnabled())
					logger.warn("Could not load class [" + className + "] for ["
							+ bundleContext.getBundle().getSymbolicName() + "]");
			}
		}
		if (logger.isDebugEnabled())
			logger.debug("Processing annotations for [" + bundleContext.getBundle().getSymbolicName() + "] found "
					+ dependencies);

		return dependencies;
	}

	protected boolean hasServiceProperty(Class<?> clazz) {
		return AnnotationUtils.findAnnotation(clazz, ServiceReference.class) != null;
	}

	private Set<OsgiServiceDependency> getClassServiceDependencies(final Class<?> beanClass, final String beanName,
			final BeanDefinition definition) {
		final Set<OsgiServiceDependency> dependencies = new LinkedHashSet<OsgiServiceDependency>();
		ReflectionUtils.doWithMethods(beanClass, new ReflectionUtils.MethodCallback() {

			public void doWith(final Method method) {
				final ServiceReference s = AnnotationUtils.getAnnotation(method, ServiceReference.class);
				if (s != null && method.getParameterTypes().length == 1
						&& !Collection.class.isAssignableFrom(method.getParameterTypes()[0])
						// Ignore definitions overriden in the XML config
						&& !definition.getPropertyValues().contains(getPropertyName(method))) {
					try {
						if (logger.isDebugEnabled())
							logger.debug("Processing annotation [" + s + "] for [" + beanClass.getName() + "."
									+ method.getName() + "()] on bean [" + beanName + "]");
						dependencies.add(new OsgiServiceDependency() {

							public Filter getServiceFilter() {
								return getUnifiedFilter(s, method, beanName);
							}

							public boolean isMandatory() {
								return s.cardinality().toCardinality().isMandatory();
							}

							public String getBeanName() {
								return beanName;
							}

							public String toString() {
								return beanName + "." + method.getName() + ": " + getServiceFilter()
										+ (isMandatory() ? " (mandatory)" : " (optional)");
							}
						});
					}
					catch (Exception e) {
						throw new IllegalArgumentException("Error processing service annotation", e);
					}
				}
			}
		});
		return dependencies;
	}

	private String getPropertyName(Method method) {
		String name = method.getName();
		if (name.startsWith("set")) {
			return Character.toLowerCase(name.charAt(3)) + name.substring(4);
		}
		return name;
	}

	private Filter getUnifiedFilter(ServiceReference s, Method writeMethod, String beanName) {
		String filter;
		if (s.serviceTypes() == null || s.serviceTypes().length == 0
				|| (s.serviceTypes().length == 1 && s.serviceTypes()[0].equals(ServiceReference.class))) {
			Class<?>[] params = writeMethod.getParameterTypes();
			if (params.length != 1) {
				throw new IllegalArgumentException("Setter for [" + beanName + "] must have only one argument");
			}
			filter = OsgiFilterUtils.unifyFilter(new Class<?>[] { params[0] }, s.filter());
		}
		else {
			filter = OsgiFilterUtils.unifyFilter(s.serviceTypes(), s.filter());
		}

		if (logger.isTraceEnabled())
			logger.trace("unified classes=[" + filter + "]");

		// add the serviceBeanName constraint
		if (s.serviceBeanName().length() > 0) {
			filter = OsgiFilterUtils.unifyFilter(OsgiServicePropertiesResolver.BEAN_NAME_PROPERTY_KEY,
				new String[] { s.serviceBeanName() }, filter);
			if (logger.isTraceEnabled())
				logger.trace("unified serviceBeanName [" + ObjectUtils.nullSafeToString(s.serviceBeanName())
						+ "] and filter=[" + filter + "]");
		}

		// create (which implies validation) the actual filter
		return OsgiFilterUtils.createFilter(filter);
	}
}
