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

package org.springframework.osgi.extender.internal.support;

import java.util.List;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEventMulticasterAdapter;
import org.springframework.osgi.extender.internal.dependencies.startup.MandatoryImporterDependencyFactory;
import org.springframework.osgi.extender.support.DefaultOsgiApplicationContextCreator;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.util.BundleDelegatingClassLoader;
import org.springframework.scheduling.timer.TimerTaskExecutor;

/**
 * @author Costin Leau
 */
public class ExtenderConfigurationDefaultSettingsTest extends TestCase {

	private ExtenderConfiguration config;
	private BundleContext bundleContext;


	protected void setUp() throws Exception {
		bundleContext = new MockBundleContext();
		config = new ExtenderConfiguration(bundleContext);
	}

	protected void tearDown() throws Exception {
		config.destroy();
		config = null;
	}

	public void testTaskExecutor() throws Exception {
		assertTrue(config.getTaskExecutor() instanceof SimpleAsyncTaskExecutor);
	}

	public void testShutdownTaskExecutor() throws Exception {
		TaskExecutor executor = config.getShutdownTaskExecutor();
		assertTrue(executor instanceof TimerTaskExecutor);
	}

	public void testEventMulticaster() throws Exception {
		assertTrue(config.getEventMulticaster() instanceof OsgiBundleApplicationContextEventMulticasterAdapter);
	}

	public void testApplicationContextCreator() throws Exception {
		assertTrue(config.getContextCreator() instanceof DefaultOsgiApplicationContextCreator);
	}

	public void testShutdownWaitTime() throws Exception {
		// 10 seconds in ms
		assertEquals(10 * 1000, config.getShutdownWaitTime());
	}

	public void testShouldProcessAnnotation() throws Exception {
		assertFalse(config.shouldProcessAnnotation());
	}

	public void testDependencyWaitTime() throws Exception {
		// 5 minutes in ms
		assertEquals(5 * 60 * 1000, config.getDependencyWaitTime());
	}

	public void testPostProcessors() throws Exception {
		List postProcessors = config.getPostProcessors();
		assertTrue(postProcessors.isEmpty());
	}

	public void testDependencyFactories() throws Exception {
		List factories = config.getDependencyFactories();
		assertEquals("wrong number of dependencies factories registered by default", 1, factories.size());
		assertTrue(factories.get(0) instanceof MandatoryImporterDependencyFactory);
	}
}