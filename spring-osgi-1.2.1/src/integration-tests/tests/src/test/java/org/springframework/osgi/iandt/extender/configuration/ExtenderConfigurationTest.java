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

package org.springframework.osgi.iandt.extender.configuration;

import java.util.List;
import java.util.Properties;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.scheduling.timer.TimerTaskExecutor;

/**
 * Extender configuration fragment.
 * 
 * @author Costin Leau
 * 
 */
public class ExtenderConfigurationTest extends BaseIntegrationTest {

	private ApplicationContext context;


	protected void onSetUp() throws Exception {
		context = (ApplicationContext) applicationContext.getBean("appCtx");
	}

	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt,extender-fragment-bundle," + getSpringDMVersion() };
	}

	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/osgi/iandt/extender/configuration/config.xml" };
	}

	public void testExtenderConfigAppCtxPublished() throws Exception {
		ServiceReference[] refs = bundleContext.getAllServiceReferences(
			"org.springframework.context.ApplicationContext", null);
		for (int i = 0; i < refs.length; i++) {
			System.out.println(OsgiStringUtils.nullSafeToString(refs[i]));
		}
		assertNotNull(context);
	}

	public void tstPackageAdminReferenceBean() throws Exception {
		logger.info("Calling package admin bean");
		assertNotNull(context.getBean("packageAdmin"));
	}

	public void testShutdownTaskExecutor() throws Exception {
		assertTrue(context.containsBean("shutdownTaskExecutor"));
		Object bean = context.getBean("shutdownTaskExecutor");
		assertTrue("unexpected type", bean instanceof TimerTaskExecutor);
	}

	public void testTaskExecutor() throws Exception {
		assertTrue(context.containsBean("taskExecutor"));
		Object bean = context.getBean("shutdownTaskExecutor");
		assertTrue("unexpected type", bean instanceof TaskExecutor);
	}

	public void testCustomProperties() throws Exception {
		assertTrue(context.containsBean("extenderProperties"));
		Object bean = context.getBean("extenderProperties");
		assertTrue("unexpected type", bean instanceof Properties);
	}

	// felix doesn't support fragments, so disable this test
	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return getPlatformName().indexOf("elix") > -1;
	}

	protected List getTestPermissions() {
		List list = super.getTestPermissions();
		list.add(new AdminPermission("*", AdminPermission.METADATA));
		return list;
	}
}