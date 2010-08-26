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

package org.springframework.osgi.web.context.support;

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.osgi.io.OsgiBundleResourceLoader;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.UiApplicationContextUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextAwareProcessor;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * OSGi variant for {@link XmlWebApplicationContext}. The implementation
 * mandates that the OSGi bundle context is either set ({@link #setBundleContext(BundleContext)}
 * before setting the ServletContext or that the given ServletContext contains
 * the BundleContext as an attribute under {@link #BUNDLE_CONTEXT_ATTRIBUTE} (<code>org.springframework.osgi.web.org.osgi.framework.BundleContext</code>).
 * 
 * <p/> Additionally, this implementation replaces the {@link ServletContext}
 * resource loading with an OSGi specific loader which provides equivalent
 * functionality.
 * 
 * <p/>The OSGi service published for this application context contains the
 * namespace property (if non-null) under
 * <code>org.springframework.web.context.namespace</code> property.
 * 
 * @see XmlWebApplicationContext
 * @see OsgiBundleResourceLoader
 * @see OsgiBundleXmlApplicationContext
 * 
 * @author Costin Leau
 */
public class OsgiBundleXmlWebApplicationContext extends OsgiBundleXmlApplicationContext implements
		ConfigurableWebApplicationContext, ThemeSource {

	/** service entry used for storing the namespace associated with this context */
	private static final String APPLICATION_CONTEXT_SERVICE_NAMESPACE_PROPERTY = "org.springframework.web.context.namespace";

	/** ServletContext attribute for retrieving the bundle context */
	public static final String BUNDLE_CONTEXT_ATTRIBUTE = "org.springframework.osgi.web."
			+ BundleContext.class.getName();

	/**
	 * Suffix for WebApplicationContext namespaces.
	 */
	private static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";

	/** Servlet context that this context runs in */
	private ServletContext servletContext;

	/** Servlet config that this context runs in, if any */
	private ServletConfig servletConfig;

	/** Namespace of this context, or <code>null</code> if root */
	private String namespace;

	/** the ThemeSource for this ApplicationContext */
	private ThemeSource themeSource;


	public OsgiBundleXmlWebApplicationContext() {
		setDisplayName("Root OsgiWebApplicationContext");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Additionally, if the {@link BundleContext} is not set, it is looked up
	 * under {@link #BUNDLE_CONTEXT_ATTRIBUTE}.
	 */
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;

		// look for the attribute only if there is no BundleContext available
		if (getBundleContext() == null) {

			// try to locate the bundleContext in the ServletContext
			if (servletContext != null) {
				Object context = servletContext.getAttribute(BUNDLE_CONTEXT_ATTRIBUTE);

				if (context != null) {
					Assert.isInstanceOf(BundleContext.class, context);
					logger.debug("Using the bundle context located in the servlet context at "
							+ BUNDLE_CONTEXT_ATTRIBUTE);
					setBundleContext((BundleContext) context);
				}
			}

			// fall back to the parent
			ApplicationContext parent = getParent();

			if (parent instanceof ConfigurableOsgiBundleApplicationContext) {
				logger.debug("Using the application context parent's bundle context");
				setBundleContext(((ConfigurableOsgiBundleApplicationContext) parent).getBundleContext());
			}
		}
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Additionally, it also sets the context namespace if it's not initialized
	 * (null).
	 */
	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
		if (servletConfig != null) {

			if (this.servletContext == null) {
				setServletContext(servletConfig.getServletContext());
			}

			if (getNamespace() == null) {
				setNamespace(this.servletConfig.getServletName() + DEFAULT_NAMESPACE_SUFFIX);
			}
		}
	}

	public ServletConfig getServletConfig() {
		return this.servletConfig;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
		if (namespace != null) {
			setDisplayName("WebApplicationContext for namespace '" + namespace + "'");
		}
	}

	public String getNamespace() {
		return this.namespace;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Registers request/session scopes, a {@link ServletContextAwareProcessor},
	 * etc.
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		super.postProcessBeanFactory(beanFactory);

		beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext, this.servletConfig));
		beanFactory.ignoreDependencyInterface(ServletContextAware.class);
		beanFactory.ignoreDependencyInterface(ServletConfigAware.class);
		beanFactory.registerResolvableDependency(ServletContext.class, this.servletContext);
		beanFactory.registerResolvableDependency(ServletConfig.class, this.servletConfig);

		WebApplicationContextUtils.registerWebApplicationScopes(beanFactory);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Additionally, this implementation published the context namespace under
	 * <code>org.springframework.context.web.namespace</code> property.
	 */
	protected void customizeApplicationContextServiceProperties(Map serviceProperties) {
		super.customizeApplicationContextServiceProperties(serviceProperties);
		String ns = getNamespace();
		if (ns != null) {
			serviceProperties.put(APPLICATION_CONTEXT_SERVICE_NAMESPACE_PROPERTY, ns);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Initializes the theme capability.
	 */
	protected void onRefresh() {
		super.onRefresh();
		this.themeSource = UiApplicationContextUtils.initThemeSource(this);
	}

	public Theme getTheme(String themeName) {
		return this.themeSource.getTheme(themeName);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Returns the default location for the root context. Default values are
	 * "/WEB-INF/applicationContext.xml", and "/WEB-INF/test-servlet.xml" for a
	 * context with the namespace "test-servlet" (like for a DispatcherServlet
	 * instance with the servlet-name "test").
	 * 
	 * @see XmlWebApplicationContext#getDefaultConfigLocations()
	 * @see XmlWebApplicationContext#DEFAULT_CONFIG_LOCATION
	 */
	protected String[] getDefaultConfigLocations() {
		String ns = getNamespace();
		if (ns != null) {
			return new String[] { XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION_PREFIX + ns
					+ XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION_SUFFIX };
		}
		else {
			return new String[] { XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION };
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Sets the config locations for this application context in init-param
	 * style, i.e. with distinct locations separated by commas, semicolons or
	 * whitespace.
	 * <p>
	 * If not set, the implementation may use a default as appropriate.
	 * 
	 * @see ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS
	 */
	public void setConfigLocation(String location) {
		setConfigLocations(StringUtils.tokenizeToStringArray(location, CONFIG_LOCATION_DELIMITERS));
	}
}
