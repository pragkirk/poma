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

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.web.deployer.OsgiWarDeploymentException;
import org.springframework.osgi.web.deployer.WarDeployment;
import org.springframework.osgi.web.deployer.WarDeploymentContext;
import org.springframework.osgi.web.deployer.internal.support.DefaultWarDeploymentContext;

/**
 * 
 * @author Costin Leau
 * 
 */
public class AbstractWarDeployerTest extends TestCase {

	private AbstractWarDeployer deployer;
	private WarDeployment deployment;


	private class ParentWarDeployer extends AbstractWarDeployer {

		protected WarDeployment createDeployment(Bundle bundle, String contextPath) throws Exception {
			return deployment;
		}

		protected String getServerInfo() {
			return "server info";
		}

		protected void startDeployment(WarDeployment deployment) throws Exception {

		}
	}


	protected void setUp() throws Exception {
		deployment = new WarDeployment() {

			public WarDeploymentContext getDeploymentContext() {
				return new DefaultWarDeploymentContext(new MockBundle(), "/context", new MockServletContext());
			}

			public boolean isActive() {
				return false;
			}

			public void undeploy() throws OsgiWarDeploymentException {
			}
		};

		deployer = new ParentWarDeployer();
	}

	protected void tearDown() throws Exception {
		deployment = null;
		deployer = null;
	}

	public void testBundleContextSet() throws Exception {
		BundleContext bc = new MockBundleContext();
		deployer.setBundleContext(bc);
		deployer.afterPropertiesSet();
		assertSame(bc, deployer.getBundleContext());
	}

	public void testNoBundleContext() throws Exception {
		try {
			deployer.afterPropertiesSet();
			fail("expected validation exception");
		}
		catch (IllegalArgumentException iae) {
		}
	}

	public void testDeploySuccesful() throws Exception {
		BundleContext bc = new MockBundleContext();

		final Bundle bnd = new MockBundle();
		final String ctxPath = "/context";
		final Boolean[] started = new Boolean[1];

		deployer = new ParentWarDeployer() {

			protected WarDeployment createDeployment(Bundle bundle, String contextPath) throws Exception {
				assertSame(bnd, bundle);
				assertSame(ctxPath, contextPath);
				return super.createDeployment(bundle, contextPath);
			}

			protected void startDeployment(WarDeployment deployment) throws Exception {
				assertSame(AbstractWarDeployerTest.this.deployment, deployment);
				started[0] = Boolean.TRUE;
			}
		};

		deployer.setBundleContext(bc);
		deployer.afterPropertiesSet();

		deployer.deploy(bnd, ctxPath);
		assertEquals(Boolean.TRUE, started[0]);
	}

	public void testDeploymentCreationFailure() throws Exception {
		final RuntimeException creationException = new RuntimeException();

		deployer = new ParentWarDeployer() {

			protected WarDeployment createDeployment(Bundle bundle, String contextPath) throws Exception {
				throw creationException;
			}
		};

		deployer.setBundleContext(new MockBundleContext());
		deployer.afterPropertiesSet();
		try {
			deployer.deploy(null, null);
			fail("should have thrown exception");
		}
		catch (OsgiWarDeploymentException ex) {
			assertSame(creationException, ex.getCause());
		}
	}

	public void testDeploymentStatupFailure() throws Exception {
		final RuntimeException creationException = new RuntimeException();

		deployer = new ParentWarDeployer() {

			protected void startDeployment(WarDeployment deployment) throws Exception {
				throw creationException;
			}
		};
		deployer.setBundleContext(new MockBundleContext());
		deployer.afterPropertiesSet();
		try {
			deployer.deploy(null, null);
			fail("should have thrown exception");
		}
		catch (OsgiWarDeploymentException ex) {
			assertSame(creationException, ex.getCause());
		}
	}
}
