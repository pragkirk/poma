/*
 * Copyright 2002-20067 the original author or authors.
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

package org.springframework.osgi.context.support;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.io.OsgiBundleResource;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.osgi.util.internal.BundleUtils;
import org.springframework.util.Assert;
import org.xml.sax.EntityResolver;

/**
 * Stand-alone XML application context, backed by an OSGi bundle.
 * 
 * <p> The configuration location defaults can be overridden via {@link #getDefaultConfigLocations()}. Note that
 * locations can either denote concrete files like <code>/myfiles/context.xml</code> or <em>Ant-style</em> patterns like
 * <code>/myfiles/*-context.xml</code> (see the {@link org.springframework.util.AntPathMatcher} javadoc for pattern
 * details). </p>
 * 
 * <p> <strong>Note:</strong> In case of multiple configuration locations, later bean definitions will override ones
 * defined in earlier loaded files. This can be leveraged to deliberately override certain bean definitions via an extra
 * XML file. </p>
 * 
 * <p/> <b>This is the main ApplicationContext class for OSGi environments.</b>
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 * @author Andy Piper
 * @author Hal Hildebrand
 */
public class OsgiBundleXmlApplicationContext extends AbstractDelegatedExecutionApplicationContext {

	/** Default config location for the root context(s) */
	public static final String DEFAULT_CONFIG_LOCATION = OsgiBundleResource.BUNDLE_URL_PREFIX
			+ "/META-INF/spring/*.xml";

	/**
	 * 
	 * Creates a new <code>OsgiBundleXmlApplicationContext</code> with no parent.
	 * 
	 */
	public OsgiBundleXmlApplicationContext() {
		this((String[]) null);
	}

	/**
	 * Creates a new <code>OsgiBundleXmlApplicationContext</code> with the given parent context.
	 * 
	 * @param parent the parent context
	 */
	public OsgiBundleXmlApplicationContext(ApplicationContext parent) {
		this(null, parent);
	}

	/**
	 * Creates a new <code>OsgiBundleXmlApplicationContext</code> with the given configLocations.
	 * 
	 * @param configLocations array of configuration resources
	 */
	public OsgiBundleXmlApplicationContext(String[] configLocations) {
		this(configLocations, null);
	}

	/**
	 * Creates a new <code>OsgiBundleXmlApplicationContext</code> with the given configLocations and parent context.
	 * 
	 * @param configLocations array of configuration resources
	 * @param parent the parent context
	 */
	public OsgiBundleXmlApplicationContext(String[] configLocations, ApplicationContext parent) {
		super(parent);
		setConfigLocations(configLocations);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p/> Loads the bean definitions via an <code>XmlBeanDefinitionReader</code>.
	 */
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		beanDefinitionReader.setResourceLoader(this);

		final Object[] resolvers = new Object[2];

		AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				BundleContext ctx = getBundleContext();
				String filter = BundleUtils.createNamespaceFilter(ctx);
				resolvers[0] = createNamespaceHandlerResolver(ctx, filter, getClassLoader());
				resolvers[1] = createEntityResolver(ctx, filter, getClassLoader());
				return null;
			}
		});

		beanDefinitionReader.setNamespaceHandlerResolver((NamespaceHandlerResolver) resolvers[0]);
		beanDefinitionReader.setEntityResolver((EntityResolver) resolvers[1]);

		// Allow a subclass to provide custom initialisation of the reader,
		// then proceed with actually loading the bean definitions.
		initBeanDefinitionReader(beanDefinitionReader);
		loadBeanDefinitions(beanDefinitionReader);
	}

	/**
	 * Allows subclasses to do custom initialisation here.
	 * 
	 * @param beanDefinitionReader
	 */
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
	}

	/**
	 * Loads the bean definitions with the given <code>XmlBeanDefinitionReader</code>. <p> The lifecycle of the bean
	 * factory is handled by the refreshBeanFactory method; therefore this method is just supposed to load and/or
	 * register bean definitions. <p> Delegates to a ResourcePatternResolver for resolving location patterns into
	 * Resource instances.
	 * 
	 * @throws org.springframework.beans.BeansException in case of bean registration errors
	 * @throws java.io.IOException if the required XML document isn't found
	 * @see #refreshBeanFactory
	 * @see #getConfigLocations
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 */
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			for (int i = 0; i < configLocations.length; i++) {
				reader.loadBeanDefinitions(configLocations[i]);
			}
		}
	}

	/**
	 * Provide default locations for XML files. This implementation returns <code>META-INF/spring/*.xml</code> relying
	 * on the default resource environment for actual localisation. By default, the bundle space will be used for
	 * locating the resources.
	 * 
	 * <p/> <strong>Note:</strong> Instead of overriding this method, consider using the Spring-DM specific header
	 * inside your manifest bundle.
	 * 
	 * @return default XML configuration locations
	 */
	protected String[] getDefaultConfigLocations() {
		return new String[] { DEFAULT_CONFIG_LOCATION };
	}

	/**
	 * Creates a special OSGi namespace handler resolver that first searches the bundle class path falling back to the
	 * namespace service published by Spring-DM. This allows embedded libraries that provide namespace handlers take
	 * priority over namespace provided by other bundles.
	 * 
	 * @param bundleContext the OSGi context of which the resolver should be aware of
	 * @param filter
	 * @param bundleClassLoader classloader for creating the OSGi namespace resolver proxy
	 * @return a OSGi aware namespace handler resolver
	 */
	private NamespaceHandlerResolver createNamespaceHandlerResolver(BundleContext bundleContext, String filter,
			ClassLoader bundleClassLoader) {
		Assert.notNull(bundleContext, "bundleContext is required");
		// create local namespace resolver
		// we'll use the default resolver which uses the bundle local class-loader
		NamespaceHandlerResolver localNamespaceResolver = new DefaultNamespaceHandlerResolver(bundleClassLoader);

		// hook in OSGi namespace resolver
		NamespaceHandlerResolver osgiServiceNamespaceResolver = lookupNamespaceHandlerResolver(bundleContext, filter,
				localNamespaceResolver);

		DelegatedNamespaceHandlerResolver delegate = new DelegatedNamespaceHandlerResolver();
		delegate.addNamespaceHandler(localNamespaceResolver, "LocalNamespaceResolver for bundle "
				+ OsgiStringUtils.nullSafeNameAndSymName(bundleContext.getBundle()));
		delegate.addNamespaceHandler(osgiServiceNamespaceResolver, "OSGi Service resolver");

		return delegate;
	}

	/**
	 * Similar to {@link #createNamespaceHandlerResolver(BundleContext, ClassLoader, ClassLoader)}, this method creates
	 * a special OSGi entity resolver that considers the bundle class path first, falling back to the entity resolver
	 * service provided by the Spring DM extender.
	 * 
	 * @param bundleContext the OSGi context of which the resolver should be aware of
	 * @param filter
	 * @param bundleClassLoader classloader for creating the OSGi namespace resolver proxy
	 * @return a OSGi aware entity resolver
	 */
	private EntityResolver createEntityResolver(BundleContext bundleContext, String filter,
			ClassLoader bundleClassLoader) {
		Assert.notNull(bundleContext, "bundleContext is required");
		// create local namespace resolver
		EntityResolver localEntityResolver = new DelegatingEntityResolver(bundleClassLoader);

		// hook in OSGi namespace resolver
		EntityResolver osgiServiceEntityResolver = lookupEntityResolver(bundleContext, filter, localEntityResolver);

		DelegatedEntityResolver delegate = new DelegatedEntityResolver();
		delegate.addEntityResolver(localEntityResolver, "LocalEntityResolver for bundle "
				+ OsgiStringUtils.nullSafeNameAndSymName(bundleContext.getBundle()));

		// hook in OSGi namespace resolver
		delegate.addEntityResolver(osgiServiceEntityResolver, "OSGi Service resolver");

		return delegate;
	}

	private NamespaceHandlerResolver lookupNamespaceHandlerResolver(final BundleContext bundleContext, String filter,
			final Object fallbackObject) {
		return (NamespaceHandlerResolver) TrackingUtil.getService(new Class[] { NamespaceHandlerResolver.class },
				filter, NamespaceHandlerResolver.class.getClassLoader(), bundleContext, fallbackObject);
	}

	private EntityResolver lookupEntityResolver(final BundleContext bundleContext, String filter,
			final Object fallbackObject) {
		return (EntityResolver) TrackingUtil.getService(new Class[] { EntityResolver.class }, filter,
				EntityResolver.class.getClassLoader(), bundleContext, fallbackObject);
	}

	public String[] getConfigLocations() {
		return super.getConfigLocations();
	}
}