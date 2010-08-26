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

package org.springframework.osgi.web.deployer.jetty;

import junit.framework.TestCase;

import org.mortbay.jetty.webapp.WebAppContext;
import org.osgi.framework.Bundle;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.web.deployer.OsgiWarDeploymentException;
import org.springframework.osgi.web.deployer.WarDeploymentContext;

/**
 * 
 * @author Costin Leau
 * 
 */
public class JettyWarDeploymentTest extends TestCase {

	private JettyWarDeployment deployment;

	private JettyContextUndeployer undeployer;
	private WebAppContext wac;
	private Bundle bundle;
	private String contextPath = "/context";
	private String webApp = "webapp";
	private boolean undeployed;


	protected void setUp() throws Exception {
		undeployed = false;

		undeployer = new JettyContextUndeployer() {

			public void undeploy(WebAppContext webAppCtx) throws OsgiWarDeploymentException {
				undeployed = true;
			}
		};
		wac = new WebAppContext(webApp, contextPath);

		bundle = new MockBundle();
		deployment = new JettyWarDeployment(undeployer, bundle, wac);

	}

	protected void tearDown() throws Exception {
		deployment = null;
		undeployer = null;
		wac = null;
		bundle = null;
	}

	public void testGetDeploymentContext() {
		WarDeploymentContext warCtx = deployment.getDeploymentContext();
		assertNotNull(warCtx);
		assertSame(bundle, warCtx.getBundle());
		assertEquals(contextPath, warCtx.getContextPath());
		assertNotNull(warCtx.getServletContext());
	}

	public void testIsActive() {
		assertTrue(deployment.isActive());
		deployment.undeploy();
		assertFalse(deployment.isActive());
	}

	public void testUndeploy() {
		assertFalse(undeployed);
		deployment.undeploy();
		assertTrue(undeployed);
	}

	public void testMultipleUndeployCalls() throws Exception {
		deployment.undeploy();
		assertTrue(undeployed);
		assertFalse(deployment.isActive());
		undeployed = false;
		deployment.undeploy();
		assertFalse(undeployed);
		assertFalse(deployment.isActive());
	}

	public void testGetWebAppContext() {
		assertSame(wac, deployment.getWebAppContext());
	}
}
