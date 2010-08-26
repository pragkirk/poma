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
 *
 */

package org.springframework.osgi.service.exporter.support;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.context.internal.classloader.ClassLoaderFactory;
import org.springframework.osgi.context.support.internal.OsgiBundleScope;
import org.springframework.osgi.service.exporter.OsgiServicePropertiesResolver;
import org.springframework.osgi.service.exporter.support.internal.controller.ExporterController;
import org.springframework.osgi.service.exporter.support.internal.controller.ExporterInternalActions;
import org.springframework.osgi.service.exporter.support.internal.support.PublishingServiceFactory;
import org.springframework.osgi.util.OsgiServiceUtils;
import org.springframework.osgi.util.internal.ClassUtils;
import org.springframework.osgi.util.internal.MapBasedDictionary;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * FactoryBean that transparently publishes other beans in the same application
 * context as OSGi services returning the ServiceRegistration for the given
 * object. Also known as an <em>exporter</em> this class handle the
 * registration and unregistration of an OSGi service for the backing/target
 * object.
 * 
 * <p/> The service properties used when publishing the service are determined
 * by the OsgiServicePropertiesResolver. The default implementation uses
 * <ul>
 * <li>BundleSymbolicName=&lt;bundle symbolic name&gt;</li>
 * <li>BundleVersion=&lt;bundle version&gt;</li>
 * <li>org.springframework.osgi.bean.name="&lt;bean name&gt;</li>
 * </ul>
 * 
 * <p/> <strong>Note:</strong>If thread context class loader management is used ({@link #setContextClassLoader(ExportContextClassLoader)},
 * since proxying is required, the target class has to meet certain criterion
 * described in the Spring AOP documentation. In short, final classes are not
 * supported when class enhancement is used.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 * @author Hal Hildebrand
 * @author Andy Piper
 * 
 */
public class OsgiServiceFactoryBean extends AbstractOsgiServiceExporter implements BeanClassLoaderAware,
		BeanFactoryAware, BeanNameAware, BundleContextAware, FactoryBean, InitializingBean, Ordered {

	/**
	 * Wrapper around internal commands.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private class Executor implements ExporterInternalActions {

		public void registerService() {
			OsgiServiceFactoryBean.this.registerService();
		}

		public void registerServiceAtStartup(boolean register) {
			synchronized (lock) {
				registerAtStartup = register;
			}
		}

		public void unregisterService() {
			OsgiServiceFactoryBean.this.unregisterService();
		}
	};


	private static final Log log = LogFactory.getLog(OsgiServiceFactoryBean.class);

	private BundleContext bundleContext;
	private OsgiServicePropertiesResolver propertiesResolver;
	private BeanFactory beanFactory;
	private ServiceRegistration serviceRegistration;
	private Map serviceProperties;
	private int ranking;
	private String targetBeanName;
	private boolean hasNamedBean;
	private Class[] interfaces;
	private AutoExport autoExport = AutoExport.DISABLED;
	private ExportContextClassLoader contextClassLoader = ExportContextClassLoader.UNMANAGED;
	private Object target;
	private Class targetClass;
	/** Default value is same as non-ordered */
	private int order = Ordered.LOWEST_PRECEDENCE;
	private ClassLoader classLoader;
	/** class loader used by the aop infrastructure */
	private ClassLoader aopClassLoader;

	/** exporter bean name */
	private String beanName;
	/** registration sanity flag */
	private boolean serviceRegistered = false;
	/** register at startup by default */
	private boolean registerAtStartup = true;
	/** register the service by default */
	private boolean registerService = true;
	/** synchronization lock */
	private final Object lock = new Object();
	/** internal behaviour controller */
	private final ExporterController controller;


	public OsgiServiceFactoryBean() {
		controller = new ExporterController(new Executor());
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(bundleContext, "required property 'bundleContext' has not been set");

		hasNamedBean = StringUtils.hasText(targetBeanName);

		Assert.isTrue(hasNamedBean || target != null, "Either 'targetBeanName' or 'target' properties have to be set.");

		// if we have a name, we need a bean factory
		if (hasNamedBean) {
			Assert.notNull(beanFactory, "Required property 'beanFactory' has not been set.");
		}

		// initialize bean only when dealing with singletons and named beans
		if (hasNamedBean) {
			Assert.isTrue(beanFactory.containsBean(targetBeanName), "Cannot locate bean named '" + targetBeanName
					+ "' inside the running bean factory.");

			if (beanFactory.isSingleton(targetBeanName)) {
				target = beanFactory.getBean(targetBeanName);
				targetClass = target.getClass();
			}
			else {
				targetClass = beanFactory.getType(targetBeanName);
			}

			// when running inside a container, add the dependency between this bean and the target one
			addBeanFactoryDependency();
		}
		else
			targetClass = target.getClass();

		if (propertiesResolver == null) {
			propertiesResolver = new BeanNameServicePropertiesResolver();
			((BeanNameServicePropertiesResolver) propertiesResolver).setBundleContext(bundleContext);
		}

		// sanity check
		if (interfaces == null) {
			if (AutoExport.DISABLED.equals(autoExport))
				throw new IllegalArgumentException(
					"No service interface(s) specified and auto-export discovery disabled; change at least one of these properties.");
			interfaces = new Class[0];
		}
		// check visibility type
		else {
			if (!ServiceFactory.class.isAssignableFrom(targetClass)) {
				for (int interfaceIndex = 0; interfaceIndex < interfaces.length; interfaceIndex++) {
					Class intf = interfaces[interfaceIndex];
					Assert.isAssignable(intf, targetClass,
						"Exported service object does not implement the given interface: ");
				}
			}
		}

		boolean shouldRegisterAtStartup;
		synchronized (lock) {
			shouldRegisterAtStartup = registerAtStartup;
		}

		if (shouldRegisterAtStartup)
			registerService();
	}

	private void addBeanFactoryDependency() {
		if (beanFactory instanceof ConfigurableBeanFactory) {
			ConfigurableBeanFactory cbf = (ConfigurableBeanFactory) beanFactory;
			if (StringUtils.hasText(beanName) && cbf.containsBean(beanName)) {
				// no need to validate targetBeanName (already did)
				cbf.registerDependentBean(targetBeanName, BeanFactory.FACTORY_BEAN_PREFIX + beanName);
				cbf.registerDependentBean(targetBeanName, beanName);
			}
		}
		else {
			log.warn("The running bean factory cannot support dependencies between beans - importer/exporter dependency cannot be enforced");
		}
	}

	private Dictionary mergeServiceProperties(String beanName) {
		MapBasedDictionary props = new MapBasedDictionary(propertiesResolver.getServiceProperties(beanName));

		props.putAll((Map) props);

		// add service properties
		if (serviceProperties != null)
			props.putAll(serviceProperties);

		if (ranking != 0) {
			props.put(org.osgi.framework.Constants.SERVICE_RANKING, new Integer(ranking));
		}
		return props;
	}

	/**
	 * Publishes the given object as an OSGi service. It simply assembles the
	 * classes required for publishing and then delegates the actual
	 * registration to a dedicated method.
	 */
	void registerService() {

		synchronized (lock) {
			if (serviceRegistered || !registerService)
				return;
			else
				serviceRegistered = true;
		}

		// if we have a nested bean / non-Spring managed object
		String beanName = (!hasNamedBean ? ObjectUtils.getIdentityHexString(target) : targetBeanName);

		Dictionary serviceProperties = mergeServiceProperties(beanName);

		Class[] intfs = interfaces;

		// filter classes based on visibility
		ClassLoader beanClassLoader = ClassUtils.getClassLoader(targetClass);
		Class[] autoDetectedClasses = ClassUtils.getVisibleClasses(autoExport.getExportedClasses(targetClass),
			beanClassLoader);

		if (log.isTraceEnabled())
			log.trace("Autoexport mode [" + autoExport + "] discovered on class [" + targetClass + "] classes "
					+ ObjectUtils.nullSafeToString(autoDetectedClasses));

		// filter duplicates
		Set classes = new LinkedHashSet(intfs.length + autoDetectedClasses.length);

		CollectionUtils.mergeArrayIntoCollection(intfs, classes);
		CollectionUtils.mergeArrayIntoCollection(autoDetectedClasses, classes);

		Class[] mergedClasses = (Class[]) classes.toArray(new Class[classes.size()]);

		ServiceRegistration reg = registerService(mergedClasses, serviceProperties);

		serviceRegistration = notifyListeners(target, (Map) serviceProperties, reg);
	}

	/**
	 * Registration method.
	 * 
	 * @param classes
	 * @param serviceProperties
	 * @return the ServiceRegistration
	 */
	ServiceRegistration registerService(Class[] classes, Dictionary serviceProperties) {
		Assert.notEmpty(
			classes,
			"at least one class has to be specified for exporting (if autoExport is enabled then maybe the object doesn't implement any interface)");

		// create an array of classnames (used for registering the service)
		String[] names = ClassUtils.toStringArray(classes);
		// sort the names in alphabetical order (eases debugging)
		Arrays.sort(names);

		log.info("Publishing service under classes [" + ObjectUtils.nullSafeToString(names) + "]");

		ServiceFactory serviceFactory = new PublishingServiceFactory(classes, target, beanFactory, targetBeanName,
			(contextClassLoader == ExportContextClassLoader.SERVICE_PROVIDER), classLoader, aopClassLoader,
			bundleContext);

		if (isBeanBundleScoped())
			serviceFactory = new OsgiBundleScope.BundleScopeServiceFactory(serviceFactory);

		return bundleContext.registerService(names, serviceFactory, serviceProperties);
	}

	private boolean isBeanBundleScoped() {
		boolean bundleScoped = false;
		// if we do have a bundle scope, use ServiceFactory decoration
		if (targetBeanName != null) {
			if (beanFactory instanceof ConfigurableListableBeanFactory) {
				String beanScope = ((ConfigurableListableBeanFactory) beanFactory).getMergedBeanDefinition(
					targetBeanName).getScope();
				bundleScoped = OsgiBundleScope.SCOPE_NAME.equals(beanScope);
			}
			else
				// if for some reason, the passed in BeanFactory can't be
				// queried for scopes and we do
				// have a bean reference, apply scoped decoration.
				bundleScoped = true;
		}
		return bundleScoped;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
		this.aopClassLoader = ClassLoaderFactory.getAopClassLoaderFor(classLoader);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p/> Returns a {@link ServiceRegistration} to the OSGi service for the
	 * target object.
	 */
	public Object getObject() throws Exception {
		return serviceRegistration;
	}

	public Class getObjectType() {
		return (serviceRegistration != null ? serviceRegistration.getClass() : ServiceRegistration.class);
	}

	public boolean isSingleton() {
		return false;
	}

	void unregisterService() {
		synchronized (lock) {
			if (!serviceRegistered)
				return;
			else
				serviceRegistered = false;
		}

		unregisterService(serviceRegistration);
		serviceRegistration = null;
	}

	/**
	 * Unregisters (literally stops) a service.
	 * 
	 * @param registration
	 */
	void unregisterService(ServiceRegistration registration) {
		if (OsgiServiceUtils.unregisterService(registration)) {
			log.info("Unregistered service [" + registration + "]");
		}
	}

	/**
	 * Sets the context class loader management strategy to use when invoking
	 * operations on the exposed target bean. By default,
	 * {@link ExportContextClassLoader#UNMANAGED} is used.
	 * 
	 * <p/> <strong>Note:</strong> Since proxying is required for context class
	 * loader manager, the target class has to meet certain criterias described
	 * in the Spring AOP documentation. In short, final classes are not
	 * supported when class enhancement is used.
	 * 
	 * @param ccl context class loader strategy to use
	 * @see ExportContextClassLoader
	 */
	public void setContextClassLoader(ExportContextClassLoader ccl) {
		Assert.notNull(ccl);
		this.contextClassLoader = ccl;
	}

	/**
	 * Returns the object exported as an OSGi service.
	 * 
	 * @return the object exported as an OSGi service
	 */
	public Object getTarget() {
		return target;
	}

	/**
	 * Sets the given object to be export as an OSGi service. Normally used when
	 * the exported service is a nested bean or an object not managed by the
	 * Spring container. Note that the passed target instance is ignored if
	 * {@link #setTargetBeanName(String)} is used.
	 * 
	 * @param target the object to be exported as an OSGi service
	 */
	public void setTarget(Object target) {
		this.target = target;
	}

	/**
	 * Returns the target bean name.
	 * 
	 * @return the target object bean name
	 */
	public String getTargetBeanName() {
		return targetBeanName;
	}

	/**
	 * Sets the name of the bean managed by the Spring container, which will be
	 * exported as an OSGi service. This method is normally what most use-cases
	 * need, rather then {@link #setTarget(Object)}.
	 * 
	 * @param name target bean name
	 */
	public void setTargetBeanName(String name) {
		this.targetBeanName = name;
	}

	/**
	 * Sets the strategy used for automatically publishing classes. This allows
	 * the exporter to use the target class hierarchy and/or interfaces for
	 * registering the OSGi service. By default, autoExport is disabled
	 * {@link AutoExport#DISABLED}.
	 * 
	 * @param classExporter class exporter used for automatically publishing
	 *        service classes.
	 * 
	 * @see AutoExport
	 * 
	 */
	public void setAutoExport(AutoExport classExporter) {
		Assert.notNull(classExporter);
		this.autoExport = classExporter;
	}

	/**
	 * Returns the properties used when exporting the target as an OSGi service.
	 * 
	 * @return properties used for exporting the target
	 */
	public Map getServiceProperties() {
		return serviceProperties;
	}

	/**
	 * Sets the properties used when exposing the target as an OSGi service.
	 * 
	 * @param serviceProperties properties used for exporting the target as an
	 *        OSGi service
	 */
	public void setServiceProperties(Map serviceProperties) {
		this.serviceProperties = serviceProperties;
	}

	/**
	 * Returns the OSGi ranking used when publishing the service.
	 * 
	 * @return service ranking used when publishing the service
	 */
	public int getRanking() {
		return ranking;
	}

	/**
	 * Shortcut for setting the ranking property of the published service.
	 * 
	 * 
	 * @param ranking service ranking
	 * @see Constants#SERVICE_RANKING
	 */
	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	/**
	 * Controls whether the service actually gets published or not. This can be
	 * used by application context creators to control service creation without
	 * blocking the creation of the context
	 * 
	 * @param register whether to register the service or not. The default is
	 *        true.
	 */
	public void setRegisterService(boolean register) {
		registerService = register;
		// Use targetClass as a proxy for afterPropertiesSet() being called.
		if (registerService && targetClass != null) {
			registerService();
		}
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void setBundleContext(BundleContext context) {
		this.bundleContext = context;
	}

	/**
	 * Returns the property resolver used for publishing the service.
	 * 
	 * @return service property resolver
	 */
	public OsgiServicePropertiesResolver getResolver() {
		return this.propertiesResolver;
	}

	/**
	 * Sets the property resolver used when publishing the bean as an OSGi
	 * service.
	 * 
	 * @param resolver service property resolver
	 */
	public void setResolver(OsgiServicePropertiesResolver resolver) {
		this.propertiesResolver = resolver;
	}

	/**
	 * Returns the interfaces that will be considered when exporting the target
	 * as an OSGi service.
	 * 
	 * @return interfaces under which the target will be published as an OSGi
	 *         service
	 */
	public Class[] getInterfaces() {
		return interfaces;
	}

	/**
	 * Sets the interfaces advertised by the service.These will be advertised in
	 * the OSGi space and are considered when looking for a service.
	 * 
	 * @param interfaces array of classes to advertise
	 */
	public void setInterfaces(Class[] interfaces) {
		this.interfaces = interfaces;
	}

	public int getOrder() {
		return order;
	}

	/**
	 * Set the ordering which will apply to this class's implementation of
	 * Ordered, used when applying multiple BeanPostProcessors.
	 * <p>
	 * Default value is <code>Integer.MAX_VALUE</code>, meaning that it's
	 * non-ordered.
	 * 
	 * @param order ordering value
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Returns the bean name of this class when configured inside a Spring
	 * container.
	 * 
	 * @return the bean name for this class
	 */
	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}
}