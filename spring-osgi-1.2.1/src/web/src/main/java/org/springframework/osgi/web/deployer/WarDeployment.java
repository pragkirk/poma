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


/**
 * An OSGi war deployment.
 * 
 * <p/>Provides access to its context and control over its lifecycle.
 * 
 * @see WarDeployer
 * 
 * @author Costin Leau
 * 
 */
public interface WarDeployment {

	/**
	 * Returns the context associated with this deployer. Clients should use
	 * this interface for interacting with a war deployment as it gives access
	 * to its context without jeopardizing its lifecycle.
	 * 
	 * @return context associated with this deployer.
	 */
	WarDeploymentContext getDeploymentContext();

	/**
	 * Indicates if the current deployment is active (still deployed) or not.
	 * 
	 * @return false if the deployment has been undeployed, true otherwise.
	 */
	boolean isActive();

	/**
	 * Undeploys the current deployment. If the deployment has been already
	 * undeployed, this method simply returns.
	 * 
	 * @throws OsgiWarDeploymentException if something went wrong during
	 * undeployment
	 */
	void undeploy() throws OsgiWarDeploymentException;

}
