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

package org.springframework.osgi.service.importer.support.internal.aop;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.osgi.service.ServiceUnavailableException;
import org.springframework.osgi.service.importer.DefaultOsgiServiceDependency;
import org.springframework.osgi.service.importer.OsgiServiceDependency;
import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;
import org.springframework.osgi.service.importer.ServiceProxyDestroyedException;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyWaitEndedEvent;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyWaitStartingEvent;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyWaitTimedOutEvent;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
import org.springframework.osgi.service.importer.support.internal.dependency.ImporterStateListener;
import org.springframework.osgi.service.importer.support.internal.support.DefaultRetryCallback;
import org.springframework.osgi.service.importer.support.internal.support.RetryCallback;
import org.springframework.osgi.service.importer.support.internal.support.RetryTemplate;
import org.springframework.osgi.service.importer.support.internal.support.ServiceWrapper;
import org.springframework.osgi.service.importer.support.internal.util.OsgiServiceBindingUtils;
import org.springframework.osgi.util.OsgiListenerUtils;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Interceptor adding dynamic behaviour for unary service (..1 cardinality). It
 * will look for a service using the given filter, retrying if the service is
 * down or unavailable. Will dynamically rebound a new service, if one is
 * available with a higher service ranking. <p/> <p/> In case no service is
 * available, it will throw an exception.
 * 
 * <p/> <strong>Note</strong>: this is a stateful interceptor and should not be
 * shared.
 * 
 * @author Costin Leau
 */
public class ServiceDynamicInterceptor extends ServiceInvoker implements InitializingBean,
		ApplicationEventPublisherAware {

	/**
	 * Override the default implementation to plug in event notification.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private class EventSenderRetryTemplate extends RetryTemplate {

		public EventSenderRetryTemplate(long waitTime) {
			super(waitTime, lock);
		}

		public EventSenderRetryTemplate() {
			super(lock);
		}

		protected void callbackFailed(long stop) {
			publishEvent(new OsgiServiceDependencyWaitTimedOutEvent(eventSource, dependency, stop));
		}

		protected void callbackSucceeded(long stop) {
			publishEvent(new OsgiServiceDependencyWaitEndedEvent(eventSource, dependency, stop));
		}

		protected void onMissingTarget() {
			//send event
			publishEvent(new OsgiServiceDependencyWaitStartingEvent(eventSource, dependency, this.getWaitTime()));
		}
	}

	private class ServiceLookUpCallback extends DefaultRetryCallback {

		public Object doWithRetry() {
			// before checking for a service, check whether the proxy is still valid
			if (destroyed && !isDuringDestruction)
				throw new ServiceProxyDestroyedException();

			return (wrapper != null) ? wrapper.getService() : null;
		}
	}

	/**
	 * Listener tracking the OSGi services which form the dynamic reference.
	 */
	// NOTE: while the listener here seems to share the same functionality as
	// the one in ServiceCollection in reality there are a big number of
	// differences in them - for example this one supports rebind
	// while the collection does not.
	//
	// the only common part is the TCCL handling before calling the listeners.
	private class Listener implements ServiceListener {

		public void serviceChanged(ServiceEvent event) {
			ClassLoader tccl = Thread.currentThread().getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(classLoader);
				ServiceReference ref = event.getServiceReference();

				// service id
				long serviceId = ((Long) ref.getProperty(Constants.SERVICE_ID)).longValue();
				// service ranking
				Integer rank = (Integer) ref.getProperty(Constants.SERVICE_RANKING);
				int ranking = (rank == null ? 0 : rank.intValue());

				boolean debug = log.isDebugEnabled();
				boolean publicDebug = PUBLIC_LOGGER.isDebugEnabled();

				switch (event.getType()) {

					case (ServiceEvent.REGISTERED):
						// same as ServiceEvent.REGISTERED
					case (ServiceEvent.MODIFIED): {
						// flag indicating if the service is bound or rebound
						boolean servicePresent = false;

						synchronized (lock) {
							servicePresent = (wrapper != null && wrapper.isServiceAlive());
						}

						if (updateWrapperIfNecessary(ref, serviceId, ranking)) {
							// inform listeners
							OsgiServiceBindingUtils.callListenersBind(bundleContext, proxy, ref, listeners);

							if (!servicePresent) {
								notifySatisfiedStateListeners();
							}
						}

						break;
					}
					case (ServiceEvent.UNREGISTERING): {

						boolean serviceRemoved = false;
						/**
						 * used if the service goes down and there is no
						 * replacement
						 */
						/**
						 * since the listeners will require a valid proxy, the
						 * invalidation has to happen *after* calling the
						 * listeners
						 */
						ServiceWrapper oldWrapper = wrapper;

						synchronized (lock) {
							// remove service
							if (wrapper != null) {
								if (serviceId == wrapper.getServiceId()) {
									serviceRemoved = true;
									wrapper = null;

								}
							}
						}

						ServiceReference newReference = null;

						boolean isDestroyed = false;

						synchronized (lock) {
							isDestroyed = destroyed;
						}

						// discover a new reference only if we are still running
						if (!isDestroyed) {
							newReference = OsgiServiceReferenceUtils.getServiceReference(bundleContext,
								filterClassName, (filter == null ? null : filter.toString()));

							// we have a rebind (a new service was bound)
							// so another candidate has to be searched from the existing candidates
							// - as they are alive already, we have to send an event for them ourselves
							// MODIFIED will be used for clarity
							if (newReference != null) {
								// update the listeners (through a MODIFIED event
								serviceChanged(new ServiceEvent(ServiceEvent.MODIFIED, newReference));
							}
						}

						// if no new reference was found and the service was indeed removed (it was bound to the interceptor)
						// then do an unbind
						if (newReference == null && serviceRemoved) {

							// reuse the old service for the time being
							synchronized (lock) {
								wrapper = oldWrapper;
							}

							// inform listeners
							OsgiServiceBindingUtils.callListenersUnbind(bundleContext, proxy, ref, listeners);

							// clean up wrapper
							synchronized (lock) {
								wrapper = null;
							}

							if (debug || publicDebug) {
								String message = "Service reference [" + ref + "] was unregistered";
								if (serviceRemoved) {
									message += " and unbound from the service proxy";
								}
								else {
									message += " but did not affect the service proxy";
								}
								if (debug)
									log.debug(message);
								if (publicDebug)
									PUBLIC_LOGGER.debug(message);
							}

							// update internal state listeners (unsatisfied event)
							notifyUnsatisfiedStateListeners();
						}

						break;
					}
					default:
						throw new IllegalArgumentException("unsupported event type");
				}
			}
			catch (Throwable e) {
				// The framework will swallow these exceptions without logging,
				// so log them here
				log.fatal("Exception during service event handling", e);
			}
			finally {
				Thread.currentThread().setContextClassLoader(tccl);
			}
		}

		private void notifySatisfiedStateListeners() {
			synchronized (stateListeners) {
				for (Iterator iterator = stateListeners.iterator(); iterator.hasNext();) {
					ImporterStateListener stateListener = (ImporterStateListener) iterator.next();
					stateListener.importerSatisfied(eventSource, dependency);
				}
			}
		}

		private void notifyUnsatisfiedStateListeners() {
			synchronized (stateListeners) {
				for (Iterator iterator = stateListeners.iterator(); iterator.hasNext();) {
					ImporterStateListener stateListener = (ImporterStateListener) iterator.next();
					stateListener.importerUnsatisfied(eventSource, dependency);
				}
			}
		}

		private boolean updateWrapperIfNecessary(ServiceReference ref, long serviceId, int serviceRanking) {
			boolean updated = false;
			try {
				synchronized (lock) {
					if (wrapper != null && wrapper.isServiceAlive()) {
						// if there is a higher rank service
						if (serviceRanking > wrapper.getServiceRanking()) {
							updated = true;
							updateReferenceHolders(ref);
						}
						// if equality, use the service id
						if (serviceRanking == wrapper.getServiceRanking()) {
							if (serviceId < wrapper.getServiceId()) {
								updated = true;
								updateReferenceHolders(ref);
							}
						}
					}
					// we don't have any valid services bounded yet so just bind
					// the new
					// one
					else {
						updated = true;
						updateReferenceHolders(ref);
					}
					lock.notifyAll();
					return updated;
				}
			}
			finally {
				boolean debug = log.isDebugEnabled();
				boolean publicDebug = PUBLIC_LOGGER.isDebugEnabled();

				if (debug || publicDebug) {
					String message = "Service reference [" + ref + "]";
					if (updated)
						message += " bound to proxy";
					else
						message += " not bound to proxy";
					if (debug)
						log.debug(message);
					if (publicDebug)
						PUBLIC_LOGGER.debug(message);
				}
			}
		}

		/**
		 * Update internal holders for the backing ServiceReference.
		 * 
		 * @param ref
		 */
		private void updateReferenceHolders(ServiceReference ref) {
			// no need for a lock since this method is called from a synchronized block
			wrapper = new ServiceWrapper(ref, bundleContext);
			referenceDelegate.swapDelegates(ref);
		}
	}


	private static final int hashCode = ServiceDynamicInterceptor.class.hashCode() * 13;

	/** public logger */
	private static final Log PUBLIC_LOGGER = LogFactory.getLog(OsgiServiceProxyFactoryBean.class);

	private final BundleContext bundleContext;

	private final String filterClassName;

	private final Filter filter;

	/** TCCL to set when calling listeners */
	private final ClassLoader classLoader;

	private final SwappingServiceReferenceProxy referenceDelegate;

	/** event listener */
	private final ServiceListener listener;

	/** mandatory flag */
	private boolean serviceRequiredAtStartup = true;

	/** flag indicating whether the destruction has started or not */
	private boolean isDuringDestruction = false;

	/** flag indicating whether the proxy is already destroyed or not */
	private boolean destroyed = false;

	/** private lock */
	/**
	 * used for reading/setting property and sending notifications between the
	 * event listener and any threads waiting for an OSGi service to appear
	 */
	private final Object lock = new Object();

	/** utility service wrapper */
	private ServiceWrapper wrapper;

	/** retry template */
	private final RetryTemplate retryTemplate = new EventSenderRetryTemplate();

	/** retry callback */
	private final RetryCallback retryCallback = new ServiceLookUpCallback();

	/** dependable service importer */
	private Object eventSource;

	/** event source (importer) name */
	private String sourceName;

	/** listener that need to be informed of bind/rebind/unbind */
	private OsgiServiceLifecycleListener[] listeners = new OsgiServiceLifecycleListener[0];

	/** reference to the created proxy passed to the listeners */
	private Object proxy;

	/** event publisher */
	private ApplicationEventPublisher applicationEventPublisher;

	/** dependency object */
	private OsgiServiceDependency dependency;

	/** internal state listeners */
	private List stateListeners = Collections.EMPTY_LIST;


	public ServiceDynamicInterceptor(BundleContext context, String filterClassName, Filter filter,
			ClassLoader classLoader) {
		this.bundleContext = context;
		this.filterClassName = filterClassName;
		this.filter = filter;
		this.classLoader = classLoader;

		referenceDelegate = new SwappingServiceReferenceProxy();
		listener = new Listener();
	}

	public Object getTarget() {
		Object target = lookupService();

		// nothing found
		if (target == null) {
			throw new ServiceUnavailableException(filter);
		}
		return target;
	}

	/**
	 * Look the service by waiting the service to appear. Note this method
	 * should use the same lock as the listener handling the service reference.
	 */
	private Object lookupService() {
		synchronized (lock) {
			return (Object) retryTemplate.execute(retryCallback);
		}
	}

	private void publishEvent(ApplicationEvent event) {
		if (applicationEventPublisher != null) {
			if (log.isTraceEnabled())
				log.trace("Publishing event through publisher " + applicationEventPublisher);
			try {
				applicationEventPublisher.publishEvent(event);
			}
			catch (IllegalStateException ise) {
				log.debug(
					"Event "
							+ event
							+ " not published as the publisher is not initialized - usually this is caused by eager initialization of the importers by post processing",
					ise);
			}

		}
		else if (log.isTraceEnabled())
			log.trace("No application event publisher set; no events will be published");
	}

	public void afterPropertiesSet() {
		Assert.notNull(proxy);
		Assert.notNull(eventSource);

		boolean debug = log.isDebugEnabled();

		dependency = new DefaultOsgiServiceDependency(sourceName, filter, serviceRequiredAtStartup);

		if (debug)
			log.debug("Adding OSGi mandatoryListeners for services matching [" + filter + "]");
		OsgiListenerUtils.addSingleServiceListener(bundleContext, listener, filter);

		if (serviceRequiredAtStartup) {
			if (debug)
				log.debug("1..x cardinality - looking for service [" + filter + "] at startup...");

			PUBLIC_LOGGER.info("Looking for mandatory OSGi service dependency for bean [" + sourceName
					+ "] matching filter " + filter);
			Object target = getTarget();
			if (debug)
				log.debug("Service retrieved " + target);

			PUBLIC_LOGGER.info("Found mandatory OSGi service for bean [" + sourceName + "]");
		}
	}

	public void destroy() {
		OsgiListenerUtils.removeServiceListener(bundleContext, listener);
		synchronized (lock) {
			// set this flag first to make sure no rebind is done
			destroyed = true;
			isDuringDestruction = true;
			if (wrapper != null) {
				ServiceReference ref = wrapper.getReference();
				if (ref != null) {
					// send unregistration event to the listener
					listener.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, ref));
				}
			}
			/** destruction process has ended */
			isDuringDestruction = false;
			// notify also any proxies that still wait on the service
			lock.notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This particular interceptor returns a delegated service reference so that
	 * callers can keep the reference even if the underlying target service
	 * reference changes in time.
	 */
	public ServiceReference getServiceReference() {
		return referenceDelegate;
	}

	public void setRetryTimeout(long timeout) {
		retryTemplate.reset(timeout);
	}

	public RetryTemplate getRetryTemplate() {
		return retryTemplate;
	}

	public OsgiServiceLifecycleListener[] getListeners() {
		return listeners;
	}

	public void setListeners(OsgiServiceLifecycleListener[] listeners) {
		this.listeners = listeners;
	}

	public void setServiceImporter(Object importer) {
		this.eventSource = importer;
	}

	public void setServiceImporterName(String name) {
		this.sourceName = name;
	}

	public void setRequiredAtStartup(boolean requiredAtStartup) {
		this.serviceRequiredAtStartup = requiredAtStartup;
	}

	public void setProxy(Object proxy) {
		this.proxy = proxy;
	}

	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	/** Internal state listeners */
	public void setStateListeners(List stateListeners) {
		this.stateListeners = stateListeners;
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof ServiceDynamicInterceptor) {
			ServiceDynamicInterceptor oth = (ServiceDynamicInterceptor) other;
			return (serviceRequiredAtStartup == oth.serviceRequiredAtStartup
					&& ObjectUtils.nullSafeEquals(wrapper, oth.wrapper)
					&& ObjectUtils.nullSafeEquals(filter, oth.filter) && ObjectUtils.nullSafeEquals(retryTemplate,
				oth.retryTemplate));
		}
		else
			return false;
	}

	public int hashCode() {
		return hashCode;
	}
}