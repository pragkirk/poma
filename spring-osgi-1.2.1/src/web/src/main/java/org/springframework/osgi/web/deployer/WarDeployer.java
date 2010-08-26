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

import org.osgi.framework.Bundle;

/**
 * OSGi WAR bundle deployer. Handles the installing and uninstalling of the OSGi
 * bundle as an web application and deals with any container specific
 * integration.
 * 
 * <p/> Implementations are free to use specific environments for the actual
 * deployment process, such as Apache Tomcat, OSGi HttpService, Jetty or other
 * web containers. It is up to implementations to start the servers embedded or
 * to require their presence (though the latter is preferred).
 * 
 * <p/>As the war listener handles the detection, start up and shutdown of the
 * bundles, the deployer should be concerned only with the deployment process of
 * the bundle. It is recommended to thrown exceptions (if any) unwrapped as the
 * extender will take care of logging and wrapping.
 * 
 * @see ContextPathStrategy
 * 
 * @author Costin Leau
 */
public interface WarDeployer {

	/**
	 * Deploys the given bundle as a WAR using the given context path.
	 * Traditionally, a WAR means the given bundle contains a
	 * <code>WEB-INF/web.xml</code> file in its bundle space. However,
	 * implementations can chose to create a <em>synthetic</em> WAR or use
	 * other configuration files instead.
	 * 
	 * @param bundle war bundle
	 * @param contextPath the war context path
	 * @return a war deployment for the given bundle
	 * @throws OsgiWarDeploymentException if something went wrong during
	 * deployment
	 */
	WarDeployment deploy(Bundle bundle, String contextPath) throws OsgiWarDeploymentException;

}
