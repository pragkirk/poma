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

package org.springframework.osgi.iandt.tcclManagement;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.osgi.framework.AdminPermission;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.iandt.tccl.TCCLService;

/**
 * Test for TCCL handling only on the client side. That is the service doesn't
 * provide any handling.
 * 
 * @author Costin Leau
 * 
 */
public class ClientOnlyTcclTest extends BaseIntegrationTest {

	private static final String CLIENT_RESOURCE = "/org/springframework/osgi/iandt/tcclManagement/client-resource.properties";

	private static final String SERVICE_RESOURCE = "/org/springframework/osgi/iandt/tccl/internal/internal-resource.file";

	private static final String SERVICE_PUBLIC_RESOURCE = "/org/springframework/osgi/iandt/tccl/service-resource.file";

	private static final String CLIENT_CLASS = "org.springframework.osgi.iandt.tcclManagement.ClientOnlyTcclTest";

	private static final String SERVICE_CLASS = "org.springframework.osgi.iandt.tccl.internal.PrivateTCCLServiceImplementation";

	private static final String SERVICE_PUBLIC_CLASS = "org.springframework.osgi.iandt.tccl.TCCLService";


	protected String[] getConfigLocations() {
		return new String[] { "/org/springframework/osgi/iandt/tcclManagement/client-context.xml" };
	}

	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt,tccl," + getSpringDMVersion() };
	}

	public void testTCCLUnmanaged() throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		TCCLService tccl = getUnmanagedTCCL();
		assertSame(loader, tccl.getTCCL());
	}

	public void testTCCLUnmanagedWithNullClassLoader() throws Exception {
		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(null);
			ClassLoader cl = getUnmanagedTCCL().getTCCL();
			assertNull(cl);
		}
		finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	public void testTCCLUnmanagedWithPredefinedClassLoader() throws Exception {
		URLClassLoader dummyCL = new URLClassLoader(new URL[0]);

		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(dummyCL);
			ClassLoader cl = getUnmanagedTCCL().getTCCL();
			assertSame(dummyCL, cl);
		}
		finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	public void testClientTCCLOnClasses() throws Exception {
		ClassLoader clientCL = getClientTCCL().getTCCL();
		assertNotNull(clientCL);
		assertNotNull(clientCL.loadClass(CLIENT_CLASS));
	}

	public void testClientTCCLOnResources() throws Exception {
		ClassLoader clientCL = getClientTCCL().getTCCL();
		assertNotNull(clientCL);
		assertNotNull(clientCL.getResource(CLIENT_RESOURCE));
	}

	public void testClientTCCLWithServiceClasses() throws Exception {
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		ClassLoader cl = getClientTCCL().getTCCL();
		System.out.println("current :" + current);
		System.out.println("cl : " + cl);
		cl.loadClass(SERVICE_PUBLIC_CLASS);
		failToLoadClass(cl, SERVICE_CLASS);
	}

	public void testClientTCCLWithServiceResource() throws Exception {
		assertNotNull(getClientTCCL().getTCCL().getResource(SERVICE_PUBLIC_RESOURCE));
		assertNull(getClientTCCL().getTCCL().getResource(SERVICE_RESOURCE));
	}

	public void testServiceProvidedTCCLOnClasses() throws Exception {
		ClassLoader cl = getServiceProviderTCCL().getTCCL();

		cl.loadClass(SERVICE_PUBLIC_CLASS);
		cl.loadClass(SERVICE_CLASS);
	}

	public void testServiceProvidedTCCLOnResources() throws Exception {
		assertNotNull(getServiceProviderTCCL().getTCCL().getResource(SERVICE_RESOURCE));
	}

	public void testServiceProviderTCCLOnClientClasses() throws Exception {
		failToLoadClass(getServiceProviderTCCL().getTCCL(), CLIENT_CLASS);
	}

	public void testServiceProviderTCCLOnClientResources() throws Exception {
		assertNull(getServiceProviderTCCL().getTCCL().getResource(CLIENT_RESOURCE));
	}

	private void failToLoadClass(ClassLoader cl, String className) {
		try {
			cl.loadClass(className);
			fail("shouldn't be able to load class " + className);
		}
		catch (ClassNotFoundException cnfe) {
			// expected
		}
	}

	private TCCLService getUnmanagedTCCL() {
		return (TCCLService) applicationContext.getBean("unmanaged");
	}

	private TCCLService getServiceProviderTCCL() {
		return (TCCLService) applicationContext.getBean("service-provider");
	}

	private TCCLService getClientTCCL() {
		return (TCCLService) applicationContext.getBean("client");
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		perms.add(new AdminPermission("(name=org.springframework.osgi.iandt.tccl)", AdminPermission.RESOURCE));
		perms.add(new AdminPermission("(name=org.springframework.osgi.iandt.tccl)", AdminPermission.CLASS));
		return perms;
	}
}
