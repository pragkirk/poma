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

package org.springframework.osgi.service.importer.support;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.osgi.context.internal.classloader.ChainedClassLoader;
import org.springframework.osgi.context.internal.classloader.ClassLoaderFactory;

/**
 * Package protected class that provides the common aop infrastructure
 * functionality for OSGi service importers. Provides most of the constructs
 * required for assembling the service proxies, leaving subclasses to decide on
 * the service cardinality (one service or multiple) and proxy weaving.
 * 
 * 
 * @author Costin Leau
 * @author Adrian Colyer
 * @author Hal Hildebrand
 * 
 */
abstract class AbstractServiceImporterProxyFactoryBean extends AbstractOsgiServiceImportFactoryBean implements
		SmartFactoryBean {

	private boolean initialized = false;

	private Object proxy;

	/** aop classloader */
	private ChainedClassLoader aopClassLoader;


	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		Class[] intfs = getInterfaces();

		for (int i = 0; i < intfs.length; i++) {
			Class intf = intfs[i];
			aopClassLoader.addClassLoader(intf);
		}
		initialized = true;
	}

	public void destroy() throws Exception {
		Runnable callback = getProxyDestructionCallback();
		try {
			if (callback != null) {
				callback.run();
			}
		}
		finally {
			proxy = null;

		}
	}

	/**
	 * Returns a managed object for accessing OSGi service(s).
	 * 
	 * @return managed OSGi service(s)
	 */
	public Object getObject() {
		if (!initialized)
			throw new FactoryBeanNotInitializedException();

		if (proxy == null) {
			proxy = createProxy();
		}

		return proxy;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The object managed by this factory is a singleton.
	 * 
	 * @return true (i.e. the FactoryBean returns singletons)
	 */
	public boolean isSingleton() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The object created by this factory bean is eagerly initialized.
	 * 
	 * @return true (this factory bean should be eagerly initialized)
	 */
	public boolean isEagerInit() {
		return true;
	}

	/**
	 * {@inheritDoc} The object returned by this FactoryBean is a not a
	 * prototype.
	 * 
	 * @return false (the managed object is not a prototype)
	 */
	public boolean isPrototype() {
		return false;
	}

	/**
	 * Creates the proxy tracking the matching OSGi services. This method is
	 * guaranteed to be called only once, normally during initialization.
	 * 
	 * @return OSGi service tracking proxy.
	 * @see #getProxyDestructionCallback()
	 */
	abstract Object createProxy();

	/**
	 * Returns the destruction callback associated with the proxy created by
	 * this object. The callback is called once, during the destruction process
	 * of the {@link FactoryBean}.
	 * 
	 * @return destruction callback for the service proxy.
	 * @see #createProxy()
	 */
	abstract Runnable getProxyDestructionCallback();

	/**
	 * Returns the class loader used for AOP weaving
	 * 
	 * @return the classloader used for weaving
	 */
	ClassLoader getAopClassLoader() {
		return aopClassLoader;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The class will automatically chain this classloader with the AOP
	 * infrastructure classes (even if these are not visible to the user) so
	 * that the proxy creation can be completed successfully.
	 */
	public void setBeanClassLoader(final ClassLoader classLoader) {
		super.setBeanClassLoader(classLoader);
		AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				aopClassLoader = ClassLoaderFactory.getAopClassLoaderFor(classLoader);
				return null;
			}
		});
	}
}