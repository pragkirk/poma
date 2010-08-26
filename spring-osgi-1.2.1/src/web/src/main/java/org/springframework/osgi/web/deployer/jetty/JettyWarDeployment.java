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

import org.mortbay.jetty.webapp.WebAppContext;
import org.osgi.framework.Bundle;
import org.springframework.osgi.web.deployer.OsgiWarDeploymentException;
import org.springframework.osgi.web.deployer.WarDeployment;
import org.springframework.osgi.web.deployer.WarDeploymentContext;
import org.springframework.osgi.web.deployer.internal.support.DefaultWarDeploymentContext;

/**
 * Jetty specific deployment class.
 * 
 * @author Costin Leau
 * 
 */
//do all logging in the deployer since that is a public class
class JettyWarDeployment implements WarDeployment {

	/** active flag */
	private boolean active = true;
	/** Jetty webapp context associated with this object */
	private final WebAppContext webAppCtx;
	/** undeployer entity */
	private final JettyContextUndeployer undeployer;
	/** context object */
	private final WarDeploymentContext deploymentContext;


	public JettyWarDeployment(JettyContextUndeployer jettyWarUndeployer, Bundle bundle, WebAppContext wac) {
		this.undeployer = jettyWarUndeployer;
		this.webAppCtx = wac;

		// create context
		this.deploymentContext = new DefaultWarDeploymentContext(bundle, wac.getContextPath(), wac.getServletContext());
	}

	public WarDeploymentContext getDeploymentContext() {
		return deploymentContext;
	}

	public boolean isActive() {
		return active;
	}

	public void undeploy() throws OsgiWarDeploymentException {
		if (!active)
			return;

		active = false;
		undeployer.undeploy(webAppCtx);
	}

	// package protected method
	WebAppContext getWebAppContext() {
		return webAppCtx;
	}
}
