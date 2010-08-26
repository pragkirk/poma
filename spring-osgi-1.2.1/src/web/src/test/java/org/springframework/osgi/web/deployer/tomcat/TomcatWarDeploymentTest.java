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

import javax.servlet.ServletContext;

import junit.framework.TestCase;

import org.apache.catalina.Context;
import org.easymock.MockControl;
import org.osgi.framework.Bundle;
import org.springframework.mock.web.MockServletContext;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.web.deployer.OsgiWarDeploymentException;

/**
 * 
 * @author Costin Leau
 * 
 */
public class TomcatWarDeploymentTest extends TestCase {

	private TomcatWarDeployment deployment;

	private boolean undeployed = false;


	private class Undeployer implements TomcatContextUndeployer {

		public void undeploy(Context catalinaContext) throws OsgiWarDeploymentException {
			undeployed = true;
		}
	}


	private Context context;


	protected void setUp() throws Exception {
		undeployed = false;
		String path = "/path";
		ServletContext sc = new MockServletContext();
		MockControl mc = MockControl.createNiceControl(Context.class);
		context = (Context) mc.getMock();
		mc.expectAndReturn(context.getPath(), path);
		mc.expectAndReturn(context.getServletContext(), sc);
		mc.replay();
		Bundle bundle = new MockBundle();
		deployment = new TomcatWarDeployment(new Undeployer(), bundle, context);
	}

	protected void tearDown() throws Exception {
		deployment = null;
	}

	public void testGetDeploymentContext() throws Exception {
		assertNotNull(deployment.getDeploymentContext());
	}

	public void testIsActive() throws Exception {
		assertTrue(deployment.isActive());
		deployment.undeploy();
		assertFalse(deployment.isActive());
	}

	public void testUndeploy() throws Exception {
		assertTrue(deployment.isActive());
		assertFalse(undeployed);
		deployment.undeploy();
		assertFalse(deployment.isActive());
		assertTrue(undeployed);
	}

	public void testAlreadyUndeployed() throws Exception {
		deployment.undeploy();
		undeployed = false;
		deployment.undeploy();
		assertFalse(undeployed);
	}

	public void testGetCatalinaContext() throws Exception {
		assertSame(context, deployment.getCatalinaContext());
	}
}
