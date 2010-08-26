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

package org.springframework.osgi.web.deployer;

import javax.servlet.ServletContext;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.web.deployer.internal.support.DefaultWarDeploymentContext;

/**
 * @author Costin Leau
 * 
 */
public class DefaultWarDeploymentContextTest extends TestCase {

	private WarDeploymentContext context;
	private Bundle bundle;
	private ServletContext servletContext;
	private String contextPath;


	protected void setUp() throws Exception {
		bundle = new MockBundle();
		servletContext = new MockServletContext();
		contextPath = "somePath";
	}

	protected void tearDown() throws Exception {
		context = null;
		bundle = null;
		servletContext = null;
		contextPath = null;
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.web.deployer.internal.support.DefaultWarDeploymentContext#getBundle()}.
	 */
	public void testGetBundle() {
		createWarDeployer();
		assertSame(bundle, context.getBundle());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.web.deployer.internal.support.DefaultWarDeploymentContext#getContextPath()}.
	 */
	public void testGetContextPath() {
		contextPath = "/foo";
		createWarDeployer();
		assertSame(contextPath, context.getContextPath());
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.web.deployer.internal.support.DefaultWarDeploymentContext#getServletContext()}.
	 */
	public void testGetServletContext() {
		createWarDeployer();
		assertSame(servletContext, context.getServletContext());
	}

	public void testBundleContextPresenceInServletContext() throws Exception {
		BundleContext bundleCtx = new MockBundleContext();
		bundle = new MockBundle(bundleCtx);

		createWarDeployer();
		ServletContext ctx = context.getServletContext();
		Object attribute = ctx.getAttribute(WarDeploymentContext.OSGI_BUNDLE_CONTEXT_ATTRIBUTE);
		assertNotNull("bundle context not saved", attribute);
		assertSame(bundleCtx, attribute);
	}

	private void createWarDeployer() {
		context = new DefaultWarDeploymentContext(bundle, contextPath, servletContext);
	}
}
