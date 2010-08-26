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

package org.springframework.osgi.web.deployer.tomcat;

import java.io.File;
import java.io.IOException;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Loader;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.ExpandWar;
import org.osgi.framework.Bundle;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.osgi.web.deployer.OsgiWarDeploymentException;
import org.springframework.osgi.web.deployer.WarDeployment;
import org.springframework.osgi.web.deployer.internal.util.Utils;
import org.springframework.osgi.web.deployer.support.AbstractWarDeployer;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Apache <a href="http://tomcat.apache.org">Tomcat</a> 5.5.x/6.0.x specific
 * war deployer. Unpacks the given bundle into a temporary folder which is then
 * used for deploying the war into the web container.
 * 
 * <p/>The deployer works with a {@link Service} instance which should contain
 * the {@link Host} and at least one
 * {@link org.apache.catalina.connector.Connector}. The service can be either
 * configured by the user ({@link #setService(Object)} or detected
 * automatically by the deployer.
 * 
 * <p/>Note: It is up to the Catalina Service provider to decide whether an
 * {@link org.apache.catalina.startup.Embedded} instance is used or whether a
 * {@link org.apache.catalina.startup.Catalina} object will be used instead.
 * 
 * @see Context
 * @see Container
 * @see Loader
 * 
 * @author Costin Leau
 */
public class TomcatWarDeployer extends AbstractWarDeployer {

	/** Catalina OSGi service */
	private Service service;


	/**
	 * Sets the Tomcat Service used by this deployer. If none is set (the
	 * default), the deployer will look for an OSGi service, matching the
	 * {@link Service} interface (using a timeout of 5 seconds).
	 * 
	 * <p/> To avoid the dependencies on Tomcat classes in its signature, this
	 * setter accepts a plain Object that is checked and casted internally.
	 * 
	 * @param service Tomcat service (normally a Spring-DM OSGi service
	 * reference)
	 */
	public void setService(Object service) {
		if (service != null) {
			Assert.isInstanceOf(Service.class, service, "Invalid Catalina Service given:");
			this.service = (Service) service;
		}
	}

	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if (service == null) {
			log.info("No Catalina Service set; looking for one in the OSGi service registry...");
			try {
				service = (Service) Utils.createServerServiceProxy(getBundleContext(), Service.class, null);
				log.info("Found service " + service.getName());
			}
			catch (RuntimeException ex) {
				log.error("No Catalina Service found, bailing out", ex);
				throw ex;
			}
		}
	}

	protected WarDeployment createDeployment(Bundle bundle, String contextPath) throws Exception {
		String docBase = createDocBase(bundle, contextPath);

		Context catalinaContext = createDefaultContext(contextPath, docBase);
		catalinaContext.setLoader(createCatalinaLoader(bundle));
		catalinaContext.setPrivileged(false);
		catalinaContext.setReloadable(false);

		// create Tomcat specific deployment
		TomcatWarDeployment deployment = new TomcatWarDeployment(new TomcatContextUndeployer() {

			public void undeploy(Context catalinaContext) throws OsgiWarDeploymentException {
				stopCatalinaContext(catalinaContext);
			}
		}, bundle, catalinaContext);

		return deployment;
	}

	private Context createDefaultContext(String contextPath, String docBase) {
		StandardContext context = new StandardContext();

		context.setDocBase(docBase);
		context.setPath(contextPath);

		ContextConfig config = new ContextConfig();
		context.addLifecycleListener(config);
		return context;
	}

	protected void startDeployment(WarDeployment deployment) throws Exception {
		Assert.isInstanceOf(TomcatWarDeployment.class, deployment, "Wrong type of deployment used");
		// start web context
		startCatalinaContext(((TomcatWarDeployment) deployment).getCatalinaContext());
	}

	private void startCatalinaContext(Context context) {
		Thread currentThread = Thread.currentThread();

		ClassLoader old = currentThread.getContextClassLoader();
		try {
			// TODO: this seemed to be ignored and another TCCL used instead
			//			ClassLoader jasperTCCLLoader = createJasperClassLoader(context.getLoader().getClassLoader());
			currentThread.setContextClassLoader(null);
			getHost().addChild(context);
		}
		finally {
			currentThread.setContextClassLoader(old);
		}
	}

	/**
	 * Stops the given context.
	 * 
	 * @param catalinaContext
	 * @throws OsgiWarDeploymentException
	 */
	private void stopCatalinaContext(Context catalinaContext) throws OsgiWarDeploymentException {
		String docBase = catalinaContext.getDocBase();
		String contextPath = catalinaContext.getPath();
		String messageEnding = "context [" + contextPath + "] from server " + getServerInfo();

		log.info("About to undeploy " + messageEnding);

		// remove context
		try {
			removeContext(catalinaContext);
			log.info("Context [" + contextPath + "] undeployed successfully from server " + getServerInfo());
		}
		catch (Exception ex) {
			throw new OsgiWarDeploymentException("Cannot undeploy " + messageEnding, ex);
		}
		// try to clean up anyway
		finally {
			if (log.isDebugEnabled())
				log.debug("Cleaning unpacked folder " + docBase);
			// clean unpacked folder
			ExpandWar.delete(new File(docBase));
		}
	}

	private void removeContext(Context context) {
		context.getParent().removeChild(context);
	}

	/**
	 * Creates a dedicated Catalina Loader plus a special, chained, OSGi
	 * classloader.
	 * 
	 * @param bundle
	 * @return
	 */
	private Loader createCatalinaLoader(Bundle bundle) {
		OsgiCatalinaLoader loader = new OsgiCatalinaLoader();
		// create special class loader
		loader.setClassLoader(Utils.createWebAppClassLoader(bundle, StandardService.class));
		return loader;
	}

	private String createDocBase(Bundle bundle, String contextPath) throws IOException {
		File tmpFile = File.createTempFile("tomcat-" + contextPath.substring(1), ".osgi");
		tmpFile.delete();
		tmpFile.mkdir();

		String path = tmpFile.getCanonicalPath();
		if (log.isDebugEnabled())
			log.debug("Unpacking bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "] to folder [" + path
					+ "]...");

		Utils.unpackBundle(bundle, tmpFile);

		return path;
	}

	private Container getHost() {
		// get engine
		Container container = service.getContainer();

		if (container == null)
			throw new IllegalStateException("The Tomcat server doesn't have any Engines defined");
		// now get host
		Container[] children = container.findChildren();
		if (ObjectUtils.isEmpty(children))
			throw new IllegalStateException("The Tomcat server doesn't have any Hosts defined");

		// pick the first one and associate the context with it
		return children[0];
	}

	protected String getServerInfo() {
		return service.getInfo();
	}
}
