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
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.iandt.tccl.TCCLService;

/**
 * Test for TCCL handling from the server side. This test checks that the
 * service provider has always priority no matter the client setting.
 * 
 * @author Costin Leau
 * 
 */
public class ServiceTcclTest extends BaseIntegrationTest {

	private static final String CLIENT_RESOURCE = "/org/springframework/osgi/iandt/tcclManagement/client-resource.properties";

	private static final String SERVICE_RESOURCE = "/org/springframework/osgi/iandt/tccl/internal/internal-resource.file";

	private static final String SERVICE_PUBLIC_RESOURCE = "/org/springframework/osgi/iandt/tccl/service-resource.file";

	private static final String CLIENT_CLASS = "org.springframework.osgi.iandt.tcclManagement.ServiceTcclTest";

	private static final String SERVICE_CLASS = "org.springframework.osgi.iandt.tccl.internal.PrivateTCCLServiceImplementation";

	private static final String SERVICE_PUBLIC_CLASS = "org.springframework.osgi.iandt.tccl.TCCLService";


	protected String[] getConfigLocations() {
		return new String[] { "/org/springframework/osgi/iandt/tcclManagement/service-context.xml" };
	}

	protected String[] getTestBundlesNames() {
		return new String[] { "org.springframework.osgi.iandt,tccl," + getSpringDMVersion() };
	}

	public void testSanity() throws Exception {
		ServiceReference[] refs = bundleContext.getServiceReferences("org.springframework.osgi.iandt.tccl.TCCLService",
			"(tccl=service-provider)");
		System.out.println(bundleContext.getService(refs[0]));
	}

	public void testServiceProviderTCCLAndUnmanagedClient() throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		TCCLService tccl = getUnmanagedTCCL();
		assertNotSame("service provide CL hasn't been set", loader, tccl.getTCCL());
	}

	public void testServiceProviderTCCLWithUnmanagedClientWithNullClassLoader() throws Exception {
		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(null);
			ClassLoader cl = getUnmanagedTCCL().getTCCL();
			assertNotNull("service provide CL hasn't been set", cl);
		}
		finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	public void testServiceProviderTCCLAndUnmanagedClientWithPredefinedClassLoader() throws Exception {
		URLClassLoader dummyCL = new URLClassLoader(new URL[0]);

		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(dummyCL);
			ClassLoader cl = getUnmanagedTCCL().getTCCL();
			assertNotSame(dummyCL, cl);
		}
		finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	public void testServiceProviderTCCLWithClientTCCLOnClasses() throws Exception {
		failToLoadClass(getClientTCCL().getTCCL(), CLIENT_CLASS);
	}

	public void testServiceProviderTCCLWithClientTCCLOnResources() throws Exception {
		assertNull(getClientTCCL().getTCCL().getResource(CLIENT_RESOURCE));
	}

	public void testServiceProviderTCCLWithClientTCCLWithServiceClasses() throws Exception {
		ClassLoader cl = getClientTCCL().getTCCL();
		cl.loadClass(SERVICE_PUBLIC_CLASS);
		cl.loadClass(SERVICE_CLASS);
	}

	public void testServiceProviderTCCLWithClientTCCLWithServiceResource() throws Exception {
		assertNotNull(getClientTCCL().getTCCL().getResource(SERVICE_PUBLIC_RESOURCE));
		assertNotNull(getClientTCCL().getTCCL().getResource(SERVICE_RESOURCE));
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

	// provide permission for loading class using the service bundle
	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		perms.add(new AdminPermission("(name=org.springframework.osgi.iandt.tccl)", AdminPermission.CLASS));
		perms.add(new AdminPermission("(name=org.springframework.osgi.iandt.tccl)", AdminPermission.RESOURCE));
		return perms;
	}

}
