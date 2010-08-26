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

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.osgi.service.importer.ImportedOsgiServiceProxy;
import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;
import org.springframework.osgi.service.importer.support.internal.aop.ProxyPlusCallback;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceDynamicInterceptor;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceInvoker;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceProviderTCCLInterceptor;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceProxyCreator;
import org.springframework.osgi.service.importer.support.internal.controller.ImporterController;
import org.springframework.osgi.service.importer.support.internal.controller.ImporterInternalActions;
import org.springframework.osgi.service.importer.support.internal.dependency.ImporterStateListener;
import org.springframework.osgi.service.importer.support.internal.support.RetryTemplate;
import org.springframework.osgi.util.internal.ClassUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * OSGi (single) service importer. This implementation creates a managed OSGi
 * service proxy that handles the OSGi service dynamics. The returned proxy will
 * select only the best matching OSGi service for the configuration criteria. If
 * the select service goes away (at any point in time), the proxy will
 * automatically search for a replacement without the user intervention.
 * 
 * <p/> Note that the proxy instance remains the same and only the backing OSGi
 * service changes. Due to the dynamic nature of OSGi, the backing object can
 * change during method invocations.
 * 
 * @author Costin Leau
 * @author Adrian Colyer
 * @author Hal Hildebrand
 * 
 */
public final class OsgiServiceProxyFactoryBean extends AbstractServiceImporterProxyFactoryBean implements
		ApplicationEventPublisherAware {

	/**
	 * Wrapper around internal commands.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private class Executor implements ImporterInternalActions {

		public void addStateListener(ImporterStateListener stateListener) {
			stateListeners.add(stateListener);
		}

		public void removeStateListener(ImporterStateListener stateListener) {
			stateListeners.remove(stateListener);
		}

		public boolean isSatisfied() {
			if (!mandatory)
				return true;
			else
				return (proxy == null ? true : proxy.getServiceReference().getBundle() != null);
		}
	};


	private static final Log log = LogFactory.getLog(OsgiServiceProxyFactoryBean.class);

	private long retryTimeout;
	private RetryTemplate retryTemplate;

	/** proxy cast to a specific interface to allow specific method calls */
	private ImportedOsgiServiceProxy proxy;

	/** proxy infrastructure hook exposed to allow clean up */
	private Runnable destructionCallback;

	/** application publisher */
	private ApplicationEventPublisher applicationEventPublisher;

	/** internal listeners */
	private final List stateListeners = Collections.synchronizedList(new ArrayList(4));

	private final ImporterInternalActions controller;
	/** convenience field * */
	private boolean mandatory;

	private final Object monitor = new Object();


	public OsgiServiceProxyFactoryBean() {
		controller = new ImporterController(new Executor());
	}

	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		// add default cardinality
		if (getCardinality() == null)
			setCardinality(Cardinality.C_1__1);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Returns the managed proxy type. If the proxy is not created when this
	 * method is invoked, the method will try to create a composite interface
	 * (if only interfaces are specified) or null otherwise.
	 */
	public Class getObjectType() {
		synchronized (monitor) {
			if (proxy != null) {
				return proxy.getClass();
			}
			// no proxy defined, try to create a composite interface
			Class[] intfs = getInterfaces();
			if (!ObjectUtils.isEmpty(intfs)) {
				for (int index = 0; index < intfs.length; index++) {
					// concrete class found, need to create an actual proxy
					if (!intfs[index].isInterface()) {
						return null;
					}
				}
				Class[] cls = (Class[]) ObjectUtils.addObjectToArray(intfs, ImportedOsgiServiceProxy.class);
				return Proxy.getProxyClass(getAopClassLoader(), cls);
			}
		}

		// unable to determine the type, returning null
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Returns a managed proxy to the best matching OSGi service.
	 */
	public Object getObject() {
		return super.getObject();
	}

	Object createProxy() {
		if (log.isDebugEnabled())
			log.debug("Creating a single service proxy ...");

		// first create the TCCL interceptor to register its listener with the
		// dynamic interceptor
		final ServiceProviderTCCLInterceptor tcclAdvice = new ServiceProviderTCCLInterceptor();
		final OsgiServiceLifecycleListener tcclListener = tcclAdvice.new ServiceProviderTCCLListener();

		final ServiceDynamicInterceptor lookupAdvice = new ServiceDynamicInterceptor(getBundleContext(),
			ClassUtils.getParticularClass(getInterfaces()).getName(), getUnifiedFilter(), getAopClassLoader());

		lookupAdvice.setRequiredAtStartup(getCardinality().isMandatory());

		OsgiServiceLifecycleListener[] listeners = addListener(getListeners(), tcclListener);

		lookupAdvice.setListeners(listeners);
		synchronized (monitor) {
			lookupAdvice.setRetryTimeout(retryTimeout);
			retryTemplate = lookupAdvice.getRetryTemplate();
		}
		lookupAdvice.setApplicationEventPublisher(applicationEventPublisher);

		// add the listeners as a list since it might be updated after the proxy
		// has been created
		lookupAdvice.setStateListeners(stateListeners);
		lookupAdvice.setServiceImporter(this);
		lookupAdvice.setServiceImporterName(getBeanName());

		// create a proxy creator using the existing context
		ServiceProxyCreator creator = new AbstractServiceProxyCreator(getInterfaces(), getAopClassLoader(),
			getBeanClassLoader(), getBundleContext(), getContextClassLoader()) {

			ServiceInvoker createDispatcherInterceptor(ServiceReference reference) {
				return lookupAdvice;
			}

			Advice createServiceProviderTCCLAdvice(ServiceReference reference) {
				return tcclAdvice;
			}
		};

		ProxyPlusCallback proxyPlusCallback = creator.createServiceProxy(lookupAdvice.getServiceReference());

		synchronized (monitor) {
			proxy = proxyPlusCallback.proxy;
			destructionCallback = new DisposableBeanRunnableAdapter(proxyPlusCallback.destructionCallback);
		}

		lookupAdvice.setProxy(proxy);
		// start the lookup only after the proxy has been assembled
		lookupAdvice.afterPropertiesSet();

		return proxy;
	}

	Runnable getProxyDestructionCallback() {
		synchronized (monitor) {
			return destructionCallback;
		}
	}

	/**
	 * Add the given listener to the array but in the first position.
	 * 
	 * @param listeners
	 * @param listener
	 * @return
	 */
	private OsgiServiceLifecycleListener[] addListener(OsgiServiceLifecycleListener[] listeners,
			OsgiServiceLifecycleListener listener) {

		int size = (listeners == null ? 1 : listeners.length + 1);
		OsgiServiceLifecycleListener[] list = new OsgiServiceLifecycleListener[size];
		list[0] = listener;
		if (listeners != null)
			System.arraycopy(listeners, 0, list, 1, listeners.length);
		return list;
	}

	/**
	 * Sets how long (in milliseconds) should this importer wait between failed
	 * attempts at rebinding to a service that has been unregistered.
	 * 
	 * <p/> It is possible to change this value after initialization (while the
	 * proxy is in place). The new values will be used immediately by the proxy.
	 * Any in-flight waiting will be restarted using the new values. Note that
	 * if both values are the same, no restart will be applied.
	 * 
	 * @param timeoutInMillis Timeout to set, in milliseconds
	 */
	public void setTimeout(long timeoutInMillis) {
		RetryTemplate rt;

		synchronized (monitor) {
			this.retryTimeout = timeoutInMillis;
			rt = retryTemplate;
		}

		if (rt != null) {
			rt.reset(timeoutInMillis);
		}
	}

	/**
	 * Returns the timeout (in milliseconds) this importer waits while trying to
	 * find a backing service.
	 * 
	 * @return timeout in milliseconds
	 */
	public long getTimeout() {
		synchronized (monitor) {
			return retryTimeout;
		}
	}

	/* override to check proper cardinality - x..1 */
	/**
	 * {@inheritDoc}
	 * 
	 * <p/>Since this implementation creates a managed proxy, only
	 * <em>single</em> cardinalities are accepted.
	 */
	public void setCardinality(Cardinality cardinality) {
		Assert.notNull(cardinality);
		Assert.isTrue(cardinality.isSingle(), "only singular cardinality ('X..1') accepted");
		super.setCardinality(cardinality);
		synchronized (monitor) {
			this.mandatory = cardinality.isMandatory();
		}
	}

	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		synchronized (monitor) {
			this.applicationEventPublisher = applicationEventPublisher;
		}
	}
}