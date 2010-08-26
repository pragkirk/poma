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

package org.springframework.osgi.service.importer.support.internal.aop;

import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.osgi.framework.Bundle;
import org.springframework.osgi.context.internal.classloader.ClassLoaderFactory;
import org.springframework.osgi.service.importer.ImportedOsgiServiceProxy;
import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;
import org.springframework.osgi.util.internal.PrivilegedUtils;
import org.springframework.util.ObjectUtils;

/**
 * Special Thread Context ClassLoading handling interceptor dealing with
 * "service-provided" case, in which the backing service reference can be
 * updated which requires update of the classloader used as TCCL.
 * 
 * This interceptor requires registration of a dedicated
 * {@link OsgiServiceLifecycleListener} which updates the classloader used.
 * 
 * @author Costin Leau
 * 
 */
public class ServiceProviderTCCLInterceptor implements MethodInterceptor {

	public class ServiceProviderTCCLListener implements OsgiServiceLifecycleListener {

		public void bind(Object service, Map properties) throws Exception {

			// check cast just to be sure (useful when doing testing for
			// example)
			if (service instanceof ImportedOsgiServiceProxy) {
				// get the service reference from object
				setServiceProvidedClassLoader(((ImportedOsgiServiceProxy) service).getServiceReference().getBundle());
			}
		}

		public void unbind(Object service, Map properties) throws Exception {
			// do nothing on unbind
		}
	}


	private static final int hashCode = ServiceProviderTCCLInterceptor.class.hashCode() * 13;

	/** internal lock used for synchronized access to the serviceBundle */
	private final Object lock = new Object();

	private Bundle serviceBundle;

	private ClassLoader serviceClassLoader;


	public Object invoke(MethodInvocation invocation) throws Throwable {

		if (System.getSecurityManager() != null) {
			return invokePrivileged(invocation);
		}
		else {
			return invokeUnprivileged(invocation);
		}
	}

	private Object invokePrivileged(final MethodInvocation invocation) throws Throwable {
		return PrivilegedUtils.executeWithCustomTCCL(getServiceProvidedClassLoader(),
			new PrivilegedUtils.UnprivilegedThrowableExecution() {

				public Object run() throws Throwable {
					return invocation.proceed();
				}
			});
	}

	private Object invokeUnprivileged(MethodInvocation invocation) throws Throwable {
		ClassLoader current = getServiceProvidedClassLoader();

		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(current);
			return invocation.proceed();
		}
		finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	private ClassLoader getServiceProvidedClassLoader() {
		synchronized (lock) {
			return serviceClassLoader;
		}
	}

	private void setServiceProvidedClassLoader(Bundle serviceBundle) {
		synchronized (lock) {
			this.serviceBundle = serviceBundle;
			if (serviceBundle != null) {
				serviceClassLoader = ClassLoaderFactory.getBundleClassLoaderFor(serviceBundle);
			}
			else
				serviceClassLoader = null;
		}
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof ServiceProviderTCCLInterceptor) {
			ServiceProviderTCCLInterceptor oth = (ServiceProviderTCCLInterceptor) other;
			return (ObjectUtils.nullSafeEquals(serviceBundle, oth.serviceBundle));
		}
		return false;
	}

	public int hashCode() {
		return hashCode;
	}

}
