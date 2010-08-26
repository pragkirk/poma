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

package org.springframework.osgi.web.deployer.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.osgi.web.deployer.OsgiWarDeploymentException;
import org.springframework.osgi.web.deployer.WarDeployer;
import org.springframework.osgi.web.deployer.WarDeployment;
import org.springframework.util.Assert;

/**
 * Convenient base class offering common functionality for war deployers such as
 * logging.
 * 
 * @author Costin Leau
 */
public abstract class AbstractWarDeployer implements WarDeployer, InitializingBean, BundleContextAware {

	/** logger */
	protected final Log log = LogFactory.getLog(getClass());

	private BundleContext bundleContext;


	public void afterPropertiesSet() throws Exception {
		Assert.notNull(bundleContext, "bundleContext is not set");
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	/**
	 * Returns the bundle context used by this deployer.
	 * 
	 * @return the OSGi bundle context used by this deployer.
	 */
	protected BundleContext getBundleContext() {
		return bundleContext;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Breaks down (and logs appropriately) the deployment process into:
	 * 
	 * <ol>
	 * <li>creation of the deployment</li>
	 * <li>start-up of the deployment</li>
	 * </ol>
	 * 
	 * Any exception thrown during each step, is wrapped into
	 * OsgiWarDeploymentException.
	 */
	public WarDeployment deploy(Bundle bundle, String contextPath) throws OsgiWarDeploymentException {
		String commonMessage = "bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "] at [" + contextPath
				+ "] on server " + getServerInfo();
		if (log.isDebugEnabled())
			log.debug("Creating deployment for " + commonMessage);
		WarDeployment deployment;

		try {
			deployment = createDeployment(bundle, contextPath);
		}
		catch (Exception ex) {
			throw new OsgiWarDeploymentException("Cannot create war deployment for " + commonMessage, ex);
		}

		if (log.isDebugEnabled())
			log.debug("About to deploy " + commonMessage);

		try {
			startDeployment(deployment);
			log.info("Successfully deployed " + commonMessage);
		}
		catch (Exception ex) {
			throw new OsgiWarDeploymentException("Cannot create war deployment for " + commonMessage, ex);
		}

		return deployment;
	}

	/**
	 * Creates and configures (but does not start) the web deployment for the
	 * given bundle. The returned object will be passed to
	 * {@link #startDeployment(WarDeployment)}.
	 * 
	 * @param bundle OSGi bundle deployed as war
	 * @param contextPath WAR context path
	 * @return web deployment artifact
	 * @throws Exception if something goes wrong.
	 */
	protected abstract WarDeployment createDeployment(Bundle bundle, String contextPath) throws Exception;

	/**
	 * Starts the deployment artifact using the object returned by
	 * {@link #createDeployment(Bundle, String)}.
	 * 
	 * @param deployment web deployment artifact
	 * @throws Exception if something goes wrong
	 */
	protected abstract void startDeployment(WarDeployment deployment) throws Exception;

	/**
	 * Returns a nice String representation of the underlying server for logging
	 * messages.
	 * 
	 * @return toString for the running environment
	 */
	protected abstract String getServerInfo();
}
