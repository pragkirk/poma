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

package org.springframework.osgi.context.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.osgi.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.springframework.osgi.context.DependencyAwareBeanFactoryPostProcessor;
import org.springframework.osgi.context.DependencyInitializationAwareBeanPostProcessor;
import org.springframework.osgi.context.OsgiBundleApplicationContextExecutor;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEventMulticaster;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEventMulticasterAdapter;
import org.springframework.osgi.context.event.OsgiBundleContextClosedEvent;
import org.springframework.osgi.context.event.OsgiBundleContextFailedEvent;
import org.springframework.osgi.context.event.OsgiBundleContextRefreshedEvent;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.osgi.util.internal.ClassUtils;
import org.springframework.osgi.util.internal.PrivilegedUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * OSGi-specific application context that delegates the execution of its life
 * cycle methods to a different class. The main reason behind this is to
 * <em>break</em> the startup of the application context in steps that can be
 * executed asynchronously.
 * <p/>
 * <p/> <p/> The {@link #refresh()} and {@link #close()} methods delegate their
 * execution to an {@link OsgiBundleApplicationContextExecutor} class that
 * chooses how to call the lifecycle methods.
 * <p/>
 * <p/> <p/> One can still call the 'traditional' lifecycle methods through
 * {@link #normalRefresh()} and {@link #normalClose()}.
 *
 * @author Costin Leau
 * @see DelegatedExecutionOsgiBundleApplicationContext
 */
public abstract class AbstractDelegatedExecutionApplicationContext extends AbstractOsgiBundleApplicationContext
	implements DelegatedExecutionOsgiBundleApplicationContext {

	/**
	 * Executor that offers the traditional way of <code>refreshing</code>/<code>closing</code>
	 * of an ApplicationContext (no conditions have to be met and the refresh
	 * happens in only one step).
	 *
	 * @author Costin Leau
	 */
	private static class NoDependenciesWaitRefreshExecutor implements OsgiBundleApplicationContextExecutor {

		private final DelegatedExecutionOsgiBundleApplicationContext context;


		private NoDependenciesWaitRefreshExecutor(DelegatedExecutionOsgiBundleApplicationContext ctx) {
			context = ctx;
		}

		public void refresh() throws BeansException, IllegalStateException {
			context.normalRefresh();
		}

		public void close() {
			context.normalClose();
		}
	}

	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private class BeanPostProcessorChecker implements BeanPostProcessor {

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;


		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor)
				&& this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' is not eligible for getting processed by all "
						+ "BeanPostProcessors (for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}
	}


	/**
	 * Default executor
	 */
	private OsgiBundleApplicationContextExecutor executor = new NoDependenciesWaitRefreshExecutor(this);

	/**
	 * monitor used during refresh/close
	 */
	private final Object startupShutdownMonitor = new Object();

	/**
	 * Delegated multicaster
	 */
	private OsgiBundleApplicationContextEventMulticaster delegatedMulticaster;

	private ContextClassLoaderProvider cclProvider;


	/**
	 * Constructs a new
	 * <code>AbstractDelegatedExecutionApplicationContext</code> instance.
	 */
	public AbstractDelegatedExecutionApplicationContext() {
		super();
	}

	/**
	 * Constructs a new
	 * <code>AbstractDelegatedExecutionApplicationContext</code> instance.
	 *
	 * @param parent parent application context
	 */
	public AbstractDelegatedExecutionApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	/**
	 * Delegate execution of refresh method to a third party. This allows
	 * breaking the refresh process into several small pieces providing
	 * continuation-like behavior or completion of the refresh method on several
	 * threads, in a asynch manner. <p/> By default, the refresh method in
	 * executed in <em>one go</em> (normal behavior). <p/> {@inheritDoc}
	 */
	public void refresh() throws BeansException, IllegalStateException {
		executor.refresh();
	}

	public void normalRefresh() {
		Assert.notNull(getBundleContext(), "bundle context should be set before refreshing the application context");

		try {
			PrivilegedUtils.executeWithCustomTCCL(contextClassLoaderProvider().getContextClassLoader(),
				new PrivilegedUtils.UnprivilegedExecution() {

					public Object run() {
						AbstractDelegatedExecutionApplicationContext.super.refresh();
						sendRefreshedEvent();
						return null;
					}
				});
		}
		catch (Throwable th) {
			if (logger.isDebugEnabled()) {
				logger.debug("Refresh error", th);
			}
			sendFailedEvent(th);
			// propagate exception to the caller
			// rethrow the problem w/o rewrapping
			if (th instanceof RuntimeException) {
				throw (RuntimeException) th;
			}
			else {
				throw (Error) th;
			}
		}
	}

	public void normalClose() {
		try {
			PrivilegedUtils.executeWithCustomTCCL(contextClassLoaderProvider().getContextClassLoader(),
				new PrivilegedUtils.UnprivilegedExecution() {

					public Object run() {
						AbstractDelegatedExecutionApplicationContext.super.doClose();
						sendClosedEvent();
						return null;
					}
				});
		}
		catch (Throwable th) {
			// send failure event
			sendClosedEvent(th);
			// rethrow the problem w/o rewrapping
			if (th instanceof RuntimeException) {
				throw (RuntimeException) th;
			}
			else {
				throw (Error) th;
			}
		}

	}

	// Adds behaviour for isAvailable flag.
	protected void doClose() {
		executor.close();
	}

	public void startRefresh() {

		// check concurrent collection (which are mandatory)
		if (!ClassUtils.concurrentLibAvailable())
			throw new IllegalStateException(
				"JVM 5+ or backport-concurrent library (for JVM 1.4) required; see the FAQ for more details");

		try {
			PrivilegedUtils.executeWithCustomTCCL(contextClassLoaderProvider().getContextClassLoader(),
				new PrivilegedUtils.UnprivilegedExecution() {

					public Object run() {

						synchronized (startupShutdownMonitor) {

							if (ObjectUtils.isEmpty(getConfigLocations())) {
								setConfigLocations(getDefaultConfigLocations());
							}
							if (!OsgiBundleUtils.isBundleActive(getBundle())) {
								throw new ApplicationContextException(
									"Unable to refresh application context: bundle is "
										+ OsgiStringUtils.bundleStateAsString(getBundle()));
							}

							ConfigurableListableBeanFactory beanFactory = null;
							// Prepare this context for refreshing.
							prepareRefresh();

							// Tell the subclass to refresh the internal bean
							// factory.
							beanFactory = obtainFreshBeanFactory();

							// Prepare the bean factory for use in this context.
							prepareBeanFactory(beanFactory);

							try {
								// Allows post-processing of the bean factory in
								// context subclasses.
								postProcessBeanFactory(beanFactory);

								// Invoke factory processors registered as beans
								// in the context.
								invokeBeanFactoryPostProcessors(beanFactory);

								// Register bean processors that intercept bean
								// creation.
								registerBeanPostProcessors(beanFactory,
									DependencyInitializationAwareBeanPostProcessor.class, null, false);

								return null;
							}
							catch (BeansException ex) {
								// Destroy already created singletons to avoid
								// dangling resources.
								beanFactory.destroySingletons();
								cancelRefresh(ex);
								// propagate exception to the caller
								throw ex;
							}
						}
					}
				});
		}
		catch (Throwable th) {
			if (logger.isDebugEnabled()) {
				logger.debug("Pre refresh error", th);
			}
			// send failure event
			sendFailedEvent(th);
			// rethrow the problem w/o rewrapping
			if (th instanceof RuntimeException) {
				throw (RuntimeException) th;
			}
			else {
				throw (Error) th;
			}
		}
	}

	public void completeRefresh() {
		try {
			PrivilegedUtils.executeWithCustomTCCL(contextClassLoaderProvider().getContextClassLoader(),
				new PrivilegedUtils.UnprivilegedExecution() {

					public Object run() {

						synchronized (startupShutdownMonitor) {
							try {
								ConfigurableListableBeanFactory beanFactory = getBeanFactory();

								// Invoke factory processors registered as beans
								// in the context.
								invokeBeanFactoryPostProcessors(beanFactory,
									DependencyAwareBeanFactoryPostProcessor.class, null);

								// Register bean processors that intercept bean
								// creation.
								registerBeanPostProcessors(beanFactory);

								// Initialize message source for this context.
								initMessageSource();

								// Initialize event multicaster for this
								// context.
								initApplicationEventMulticaster();

								// Initialize other special beans in specific
								// context
								// subclasses.
								onRefresh();

								// Check for listener beans and register them.
								registerListeners();

								// Instantiate all remaining (non-lazy-init)
								// singletons.
								finishBeanFactoryInitialization(beanFactory);

								// Last step: publish corresponding event.
								finishRefresh();

								// everything went okay, post notification
								sendRefreshedEvent();
								return null;
							}
							catch (BeansException ex) {
								// Destroy already created singletons to avoid
								// dangling
								// resources.
								getBeanFactory().destroySingletons();
								cancelRefresh(ex);
								// propagate exception to the caller
								throw ex;
							}
						}
					}
				});
		}
		catch (Throwable th) {
			if (logger.isDebugEnabled()) {
				logger.debug("Post refresh error", th);
			}
			// post notification
			sendFailedEvent(th);
			// rethrow the problem w/o rewrapping
			if (th instanceof RuntimeException) {
				throw (RuntimeException) th;
			}
			else {
				throw (Error) th;
			}
		}
	}

	// customized to handle DependencyAwareBeanFactoryPostProcessor classes
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		invokeBeanFactoryPostProcessors(beanFactory, BeanFactoryPostProcessor.class,
			DependencyAwareBeanFactoryPostProcessor.class);
	}

	/**
	 * Instantiate and invoke all registered BeanFactoryPostProcessor beans,
	 * respecting explicit order if given.
	 * <p/>
	 * Must be called before singleton instantiation. Very similar to
	 * {@link AbstractApplicationContext#invokeBeanFactoryPostProcessors} but
	 * allowing exclusion of a certain type.
	 *
	 * @param beanFactory
	 * @param type
	 * @param exclude
	 */
	private void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory, Class type, Class exclude) {
		// Invoke factory processors registered with the context instance.
		for (Iterator it = getBeanFactoryPostProcessors().iterator(); it.hasNext();) {
			BeanFactoryPostProcessor factoryProcessor = (BeanFactoryPostProcessor) it.next();
			// check the exclude type
			if (type.isInstance(factoryProcessor) && (exclude == null || !exclude.isInstance(factoryProcessor))) {
				factoryProcessor.postProcessBeanFactory(beanFactory);
			}
		}

		// Do not initialize FactoryBeans here: We need to leave all regular
		// beans uninitialized to let the bean factory post-processors apply to
		// them!
		String[] postProcessorNames = beanFactory.getBeanNamesForType(type, true, false);

		// Separate between BeanFactoryPostProcessors that implement
		// PriorityOrdered,
		// Ordered, and the rest.
		List priorityOrderedPostProcessors = new ArrayList();
		List orderedPostProcessorNames = new ArrayList();
		List nonOrderedPostProcessorNames = new ArrayList();
		for (int i = 0; i < postProcessorNames.length; i++) {
			// first check the excluded type
			if (exclude == null || !isTypeMatch(postProcessorNames[i], exclude)) {
				if (isTypeMatch(postProcessorNames[i], PriorityOrdered.class)) {
					priorityOrderedPostProcessors.add(beanFactory.getBean(postProcessorNames[i]));
				}
				else if (isTypeMatch(postProcessorNames[i], Ordered.class)) {
					orderedPostProcessorNames.add(postProcessorNames[i]);
				}
				else {
					nonOrderedPostProcessorNames.add(postProcessorNames[i]);
				}
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement
		// PriorityOrdered.
		Collections.sort(priorityOrderedPostProcessors, new OrderComparator());
		invokeBeanFactoryPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List orderedPostProcessors = new ArrayList();
		for (Iterator it = orderedPostProcessorNames.iterator(); it.hasNext();) {
			String postProcessorName = (String) it.next();
			orderedPostProcessors.add(getBean(postProcessorName));
		}
		Collections.sort(orderedPostProcessors, new OrderComparator());
		invokeBeanFactoryPostProcessors(beanFactory, orderedPostProcessors);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List nonOrderedPostProcessors = new ArrayList();
		for (Iterator it = nonOrderedPostProcessorNames.iterator(); it.hasNext();) {
			String postProcessorName = (String) it.next();
			nonOrderedPostProcessors.add(getBean(postProcessorName));
		}
		invokeBeanFactoryPostProcessors(beanFactory, nonOrderedPostProcessors);
	}

	/**
	 * Invoke given post processors.
	 *
	 * @param beanFactory
	 * @param postProcessors
	 */
	private void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory, List postProcessors) {
		for (Iterator it = postProcessors.iterator(); it.hasNext();) {
			BeanFactoryPostProcessor postProcessor = (BeanFactoryPostProcessor) it.next();
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	// customized to handle DependencyInitializationAwareBeanPostProcessor
	// classes
	protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		registerBeanPostProcessors(beanFactory, BeanPostProcessor.class,
			DependencyInitializationAwareBeanPostProcessor.class, true);
	}

	/**
	 * Instantiate and invoke all registered BeanPostProcessor beans, respecting
	 * explicit order if given.
	 * <p/>
	 * Must be called before any instantiation of application beans. Very
	 * similar to
	 * {@link AbstractApplicationContext#invokeBeanFactoryPostProcessors} but
	 * allowing exclusion of a certain type.
	 */
	protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory, Class type, Class exclude,
	                                          boolean check) {
		String[] postProcessorNames = beanFactory.getBeanNamesForType(type, true, false);

		if (check) {
			// Register BeanPostProcessorChecker that logs an info message when
			// a bean is created during BeanPostProcessor instantiation, i.e.
			// when
			// a bean is not eligible for getting processed by all
			// BeanPostProcessors.
			int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
			beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));
		}

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List priorityOrderedPostProcessors = new ArrayList();
		List orderedPostProcessorNames = new ArrayList();
		List nonOrderedPostProcessorNames = new ArrayList();
		for (int i = 0; i < postProcessorNames.length; i++) {
			// check exclude type first
			if (exclude == null || !isTypeMatch(postProcessorNames[i], exclude)) {
				if (isTypeMatch(postProcessorNames[i], PriorityOrdered.class)) {
					priorityOrderedPostProcessors.add(beanFactory.getBean(postProcessorNames[i]));
				}
				else if (isTypeMatch(postProcessorNames[i], Ordered.class)) {
					orderedPostProcessorNames.add(postProcessorNames[i]);
				}
				else {
					nonOrderedPostProcessorNames.add(postProcessorNames[i]);
				}
			}
		}

		// First, register the BeanPostProcessors that implement
		// PriorityOrdered.
		Collections.sort(priorityOrderedPostProcessors, new OrderComparator());
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		List orderedPostProcessors = new ArrayList();
		for (Iterator it = orderedPostProcessorNames.iterator(); it.hasNext();) {
			String postProcessorName = (String) it.next();
			orderedPostProcessors.add(getBean(postProcessorName));
		}
		Collections.sort(orderedPostProcessors, new OrderComparator());
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Finally, register all other BeanPostProcessors.
		List nonOrderedPostProcessors = new ArrayList();
		for (Iterator it = nonOrderedPostProcessorNames.iterator(); it.hasNext();) {
			String postProcessorName = (String) it.next();
			nonOrderedPostProcessors.add(getBean(postProcessorName));
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory, List postProcessors) {
		for (Iterator it = postProcessors.iterator(); it.hasNext();) {
			BeanPostProcessor postProcessor = (BeanPostProcessor) it.next();
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}

	public void setExecutor(OsgiBundleApplicationContextExecutor executor) {
		this.executor = executor;
	}

	public Object getMonitor() {
		return startupShutdownMonitor;
	}

	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
	}

	public void setDelegatedEventMulticaster(OsgiBundleApplicationContextEventMulticaster multicaster) {
		this.delegatedMulticaster = multicaster;
	}

	/**
	 * Sets the OSGi multicaster by using a Spring
	 * {@link ApplicationEventMulticaster}. This method is added as a
	 * covenience.
	 *
	 * @param multicaster Spring multi-caster used for propagating OSGi specific
	 *                    events
	 * @see OsgiBundleApplicationContextEventMulticasterAdapter
	 */
	public void setDelegatedEventMulticaster(ApplicationEventMulticaster multicaster) {
		this.delegatedMulticaster = new OsgiBundleApplicationContextEventMulticasterAdapter(multicaster);
	}

	public OsgiBundleApplicationContextEventMulticaster getDelegatedEventMulticaster() {
		return this.delegatedMulticaster;
	}

	private void sendFailedEvent(Throwable cause) {
		if (delegatedMulticaster != null)
			delegatedMulticaster.multicastEvent(new OsgiBundleContextFailedEvent(this, this.getBundle(), cause));
	}

	private void sendRefreshedEvent() {
		if (delegatedMulticaster != null)
			delegatedMulticaster.multicastEvent(new OsgiBundleContextRefreshedEvent(this, this.getBundle()));
	}

	private void sendClosedEvent() {
		if (delegatedMulticaster != null)
			delegatedMulticaster.multicastEvent(new OsgiBundleContextClosedEvent(this, this.getBundle()));
	}

	private void sendClosedEvent(Throwable cause) {
		if (delegatedMulticaster != null)
			delegatedMulticaster.multicastEvent(new OsgiBundleContextClosedEvent(this, this.getBundle(), cause));
	}

	/**
	 * private method used for doing lazy-init-if-not-set for cclProvider
	 */
	private ContextClassLoaderProvider contextClassLoaderProvider() {
		if (cclProvider == null) {
			DefaultContextClassLoaderProvider defaultProvider = new DefaultContextClassLoaderProvider();
			defaultProvider.setBeanClassLoader(getClassLoader());
			cclProvider = defaultProvider;
		}
		return cclProvider;
	}

	/**
	 * Sets the {@link ContextClassLoaderProvider} used by this OSGi application
	 * context instance. By default, {@link DefaultContextClassLoaderProvider}
	 * is used.
	 *
	 * @param contextClassLoaderProvider context class loader provider to use
	 * @see ContextClassLoaderProvider
	 * @see DefaultContextClassLoaderProvider
	 */
	public void setContextClassLoaderProvider(ContextClassLoaderProvider contextClassLoaderProvider) {
		this.cclProvider = contextClassLoaderProvider;
	}
}