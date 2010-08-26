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

package org.springframework.osgi.internal.service.collection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.osgi.service.importer.ImportedOsgiServiceProxy;
import org.springframework.osgi.service.importer.support.internal.aop.ProxyPlusCallback;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceProxyCreator;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Simple, JDK based proxy creator useful for testing only.
 * 
 * @author Costin Leau
 */
public class SimpleServiceJDKProxyCreator implements ServiceProxyCreator {

	private Class[] classes;

	private ClassLoader loader;

	private BundleContext context;


	private class JDKHandler implements InvocationHandler {

		private final ServiceReference reference;


		public JDKHandler(ServiceReference reference) {
			this.reference = reference;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object service = context.getService(reference);

			if (AopUtils.isEqualsMethod(method)) {
				return (equals(args[0]) ? Boolean.TRUE : Boolean.FALSE);
			}

			return ReflectionUtils.invokeMethod(method, service, args);
		}

		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (other == null) {
				return false;
			}

			if (Proxy.isProxyClass(other.getClass())) {
				InvocationHandler ih = Proxy.getInvocationHandler(other);
				if (ih instanceof JDKHandler) {
					return reference.equals(((JDKHandler) ih).reference);
				}
			}
			return false;
		}
	}


	public SimpleServiceJDKProxyCreator(BundleContext context, Class[] classes, ClassLoader loader) {
		// add Spring-DM proxies
		Object[] obj = ObjectUtils.addObjectToArray(classes, ImportedOsgiServiceProxy.class);
		this.classes = (Class[]) ObjectUtils.addObjectToArray(obj, SpringProxy.class);
		System.out.println("given classes " + ObjectUtils.nullSafeToString(classes) + " | resulting classes "
				+ ObjectUtils.nullSafeToString(this.classes));
		this.loader = loader;
		this.context = context;
	}

	public SimpleServiceJDKProxyCreator(BundleContext context, Class[] classes) {
		this(context, classes, SimpleServiceJDKProxyCreator.class.getClassLoader());
	}

	public ProxyPlusCallback createServiceProxy(final ServiceReference reference) {
		return new ProxyPlusCallback(Proxy.newProxyInstance(loader, classes, new JDKHandler(reference)), null);
	}
}
