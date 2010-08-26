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

import org.easymock.MockControl;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.web.deployer.OsgiWarDeploymentException;
import org.springframework.osgi.web.deployer.WarDeployment;
import org.springframework.osgi.web.deployer.WarDeploymentContext;

/**
 * 
 * @author Costin Leau
 * 
 */
public class JettyWarDeployerTest extends TestCase {

	private JettyWarDeployer deployer;
	private Server server;
	private BundleContext context;


	protected void setUp() throws Exception {
		deployer = new JettyWarDeployer();
		server = new Server();
		context = new MockBundleContext();
		deployer.setServer(server);
		deployer.setBundleContext(context);

	}

	protected void tearDown() throws Exception {
		deployer = null;
		server = null;
		context = null;
	}

	public void testAfterPropertiesSet() throws Exception {
		deployer = new JettyWarDeployer();
		try {
			deployer.afterPropertiesSet();
			fail("not having a server should trigger a validation exception");
		}
		catch (RuntimeException ex) {
			// expected
		}
	}

	public void testValidAfterPropertiesSet() throws Exception {
		deployer.afterPropertiesSet();
	}

	public void testCreateDeployment() throws Exception {
		deployer.afterPropertiesSet();
		BundleContext bundleCtx = new MockBundleContext();
		String contextPath = "/path";
		WarDeployment deployment = deployer.createDeployment(bundleCtx.getBundle(), contextPath);
		assertNotNull(deployment);
		JettyWarDeployment jettyDeployment = (JettyWarDeployment) deployment;
		assertTrue(jettyDeployment.isActive());
		WebAppContext ctx = jettyDeployment.getWebAppContext();
		assertSame(server, ctx.getServer());
		assertSame(bundleCtx, ctx.getAttribute(WarDeploymentContext.OSGI_BUNDLE_CONTEXT_ATTRIBUTE));
		assertSame(contextPath, ctx.getContextPath());
		assertFalse(ctx.isParentLoaderPriority());
	}

	public void testStartDeploymentWithInvalidObject() throws Exception {
		deployer.afterPropertiesSet();
		try {
			deployer.startDeployment((WarDeployment) MockControl.createNiceControl(WarDeployment.class).getMock());
			fail("should have thrown exception when given an non-jetty war deployment");
		}
		catch (RuntimeException ex) {
			// expected
		}
	}

	public void testStartDeployment() throws Exception {
		Bundle bundle = new MockBundle();
		final boolean[] started = new boolean[1];
		started[0] = false;

		final WebAppContext tempCtx = new WebAppContext() {

			protected void doStart() throws Exception {
				// check TCCL assignment
				assertSame(Thread.currentThread().getContextClassLoader(), getClassLoader());
				started[0] = true;
			}
		};

		tempCtx.setContextPath("/path");

		JettyWarDeployment tempDeployment = new JettyWarDeployment(new JettyContextUndeployer() {

			public void undeploy(WebAppContext webAppCtx) throws OsgiWarDeploymentException {
			}
		}, bundle, tempCtx);

		final HandlerCollection handlerCollection = new HandlerCollection() {

			protected void doStart() throws Exception {
				// check TCCL assignment
				assertSame(Thread.currentThread().getContextClassLoader(), tempCtx.getClassLoader());
			}

			public void addHandler(Handler handler) {
				assertSame(handler, tempCtx);
			}
		};

		Server tempServer = new Server() {

			public Handler getChildHandlerByClass(Class byclass) {
				if (ContextHandlerCollection.class.equals(byclass))
					return null;
				assertEquals(HandlerCollection.class, byclass);
				return handlerCollection;
			}
		};
		deployer.setServer(tempServer);
		deployer.afterPropertiesSet();
		deployer.startDeployment(tempDeployment);
		assertTrue(started[0]);
	}

	public void testGetServerInfo() {
		assertTrue(deployer.getServerInfo().indexOf(Server.getVersion()) > -1);
	}

	public void testStopWebAppCtx() throws Exception {
		final boolean[] stopped = new boolean[1];

		Server tempServer = new Server() {

			public Handler getChildHandlerByClass(Class byclass) {
				if (ContextHandlerCollection.class.equals(byclass))
					return null;
				assertEquals(HandlerCollection.class, byclass);
				return new HandlerCollection();
			}
		};

		BundleContext bundleCtx = new MockBundleContext();
		String contextPath = "/path";
		deployer.setServer(tempServer);
		deployer.afterPropertiesSet();
		WarDeployment deployment = deployer.createDeployment(bundleCtx.getBundle(), contextPath);
		deployment.undeploy();
	}
}
