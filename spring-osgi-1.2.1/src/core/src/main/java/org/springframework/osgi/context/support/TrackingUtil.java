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

package org.springframework.osgi.context.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.util.OsgiFilterUtils;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;
import org.springframework.osgi.util.internal.ClassUtils;

/**
 * Utility class for easy, but reliable, tracking of OSGi services. It does
 * service tracking internally but wraps the logic into a proxy. Note that this
 * class uses raw JDK proxies and thus is usable only with interfaces. This
 * allows to create simple proxies with minimal class dependencies.
 * 
 * <p/> This class can be seen as a much shorter, less featured version of
 * {@link org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean}.
 * It is intended for the bootstrap areas of the project where no classloading
 * or listeners are required.
 * 
 * @author Costin Leau
 * 
 */
abstract class TrackingUtil {

	/**
	 * JDK Proxy invocation handler that delegates all calls to services found
	 * in the OSGi space at the time of the call, falling back to a given
	 * object.
	 * 
	 * @author Costin Leau
	 */
	private static class OsgiServiceHandler implements InvocationHandler {

		private final Object fallbackObject;
		private final BundleContext context;
		private final String filterClassName;
		private final String filter;
		private final boolean securityOn;
		private final Object lock = new Object();

		/** flag used to bypass the OSGi space if the context becomes unavailable */
		private boolean bundleContextInvalidated = false;


		public OsgiServiceHandler(Object fallbackObject, BundleContext bundleContext, String filterClass, String filter) {
			this.fallbackObject = fallbackObject;
			this.context = bundleContext;
			this.filterClassName = filterClass;
			this.filter = filter;
			this.securityOn = (System.getSecurityManager() != null);
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// fast dispatch
			if (method.getName().equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of Session proxy.
				return new Integer(System.identityHashCode(proxy));
			}

			Object target = null;

			boolean isBundleInvalid;

			synchronized (lock) {
				isBundleInvalid = bundleContextInvalidated;
			}

			if (!isBundleInvalid) {
				try {
					if (securityOn) {
						target = AccessController.doPrivileged(new PrivilegedAction() {

							public Object run() {
								return getTarget(context, filter);
							}
						});
					}
					else {
						target = getTarget(context, filter);
					}
				}
				catch (IllegalStateException ise) {
					// context has been invalidated
					synchronized (lock) {
						bundleContextInvalidated = true;
					}
				}
			}

			if (target == null) {
				target = fallbackObject;
			}

			// re-route call to the target
			try {
				Object result = method.invoke(target, args);
				return result;
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		private Object getTarget(BundleContext context, String filter) {
			ServiceReference ref = OsgiServiceReferenceUtils.getServiceReference(context, filterClassName, filter);
			return (ref != null ? context.getService(ref) : null);
		}
	}


	/**
	 * Returns a proxy that on each call seeks the relevant OSGi service and
	 * delegates the method invocation to it. In case no service is found, the
	 * fallback object is used.
	 * 
	 * <p/> Since JDK proxies are used to create services only interfaces are
	 * used.
	 * 
	 * @param classes array of classes used during proxy weaving
	 * @param filter OSGi filter (can be null)
	 * @param classLoader class loader to use - normally
	 *        classes.getClassLoader()
	 * @param context bundle context used for searching the services
	 * @param fallbackObject object to fall back onto if no OSGi service is
	 *        found.
	 * @return the proxy doing the lookup on each method invocation
	 */
	static Object getService(Class[] classes, String filter, ClassLoader classLoader, BundleContext context,
			Object fallbackObject) {
		// mold the proxy
		String flt = OsgiFilterUtils.unifyFilter(classes, filter);

		return Proxy.newProxyInstance(classLoader, classes, new OsgiServiceHandler(fallbackObject, context,
			ClassUtils.getParticularClass(classes).getName(), flt));
	}
}