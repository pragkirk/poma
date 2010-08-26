
package org.springframework.osgi.extender;

import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.osgi.service.importer.OsgiServiceDependency;

/**
 * Interface to be implemented by beans wishing to provide OSGi service
 * dependencies required by the
 * {@link org.springframework.context.ApplicationContext}. By default, the
 * extender will postpone the context initialization until the dependencies (to
 * OSGi services) are all satisfied at the same time.
 * 
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor *
 * 
 * @author Andy Piper
 * @author Costin Leau
 */
public interface OsgiServiceDependencyFactory {

	/**
	 * Returns the OSGi service dependencies applying for the given bean factory
	 * running inside the given bundle context. The returned collection should
	 * contain only {@link OsgiServiceDependency} objects.
	 * 
	 * @param bundleContext bundle
	 * @param beanFactory the bean factory used by the application context
	 * @return collection of service dependencies
	 * @throws BeansException in case of factory errors
	 * @throws InvalidSyntaxException in case of OSGi filters errors
	 * @throws BundleException in case of OSGi bundle errors
	 */
	Collection getServiceDependencies(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory)
			throws BeansException, InvalidSyntaxException, BundleException;
}
