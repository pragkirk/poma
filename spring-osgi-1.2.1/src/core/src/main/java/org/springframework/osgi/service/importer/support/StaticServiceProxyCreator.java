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

package org.springframework.osgi.service.importer.support;

import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.internal.classloader.ClassLoaderFactory;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceInvoker;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceStaticInterceptor;
import org.springframework.osgi.service.util.internal.aop.ServiceTCCLInterceptor;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.osgi.util.internal.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author Costin Leau
 * 
 */
class StaticServiceProxyCreator extends AbstractServiceProxyCreator {

	private static final Log log = LogFactory.getLog(StaticServiceProxyCreator.class);
	/** greedy proxying mechanism */
	private final boolean greedyProxying;
	/** should greedy proxying consider just interfaces ? */
	private final boolean interfacesOnlyProxying;


	/**
	 * Constructs a new <code>StaticServiceProxyCreator</code> instance.
	 * 
	 * @param classes
	 * @param aopClassLoader
	 * @param bundleContext
	 * @param iccl
	 * @param greedyProxying
	 */
	StaticServiceProxyCreator(Class[] classes, ClassLoader aopClassLoader, ClassLoader bundleClassLoader,
			BundleContext bundleContext, ImportContextClassLoader iccl, boolean greedyProxying) {
		super(classes, aopClassLoader, bundleClassLoader, bundleContext, iccl);
		this.greedyProxying = greedyProxying;

		boolean onlyInterfaces = true;

		// see if cglib is used or not
		for (int i = 0; i < classes.length; i++) {
			if (!classes[i].isInterface())
				onlyInterfaces = false;
		}

		interfacesOnlyProxying = onlyInterfaces;

		String msg = (interfacesOnlyProxying ? "NOT" : "");

		if (log.isDebugEnabled())
			log.debug("Greedy proxying will " + msg + " consider exposed classes");
	}

	ServiceInvoker createDispatcherInterceptor(ServiceReference reference) {
		return new ServiceStaticInterceptor(bundleContext, reference);
	}

	Advice createServiceProviderTCCLAdvice(ServiceReference reference) {
		Bundle bundle = reference.getBundle();
		// if reference is dead already, it's impossible to provide the service
		// class loader
		if (bundle == null)
			return null;

		return new ServiceTCCLInterceptor(ClassLoaderFactory.getBundleClassLoaderFor(bundle));
	}

	/**
	 * Apply 'greedy' proxying by discovering the exposed classes.
	 * 
	 * @param ref
	 * @return
	 */
	Class[] discoverProxyClasses(ServiceReference ref) {
		boolean trace = log.isTraceEnabled();

		if (trace)
			log.trace("Generating greedy proxy for service " + OsgiStringUtils.nullSafeToString(ref));

		String[] classNames = OsgiServiceReferenceUtils.getServiceObjectClasses(ref);

		if (trace)
			log.trace("Discovered raw classes " + ObjectUtils.nullSafeToString(classNames));

		// try to get as many classes as possible
		Class[] classes = ClassUtils.loadClasses(classNames, classLoader);

		if (trace)
			log.trace("Visible classes are " + ObjectUtils.nullSafeToString(classes));

		// exclude final classes
		classes = ClassUtils.excludeClassesWithModifier(classes, Modifier.FINAL);

		if (trace)
			log.trace("Filtering out final classes; left out with " + ObjectUtils.nullSafeToString(classes));

		// remove classes if needed
		if (interfacesOnlyProxying) {
			Set clazzes = new LinkedHashSet(classes.length);
			for (int classIndex = 0; classIndex < classes.length; classIndex++) {
				Class clazz = classes[classIndex];
				if (clazz.isInterface())
					clazzes.add(clazz);
			}
			if (trace)
				log.trace("Filtering out concrete classes; left out with " + clazzes);

			classes = (Class[]) clazzes.toArray(new Class[clazzes.size()]);
		}

		// remove class duplicates/parents
		classes = ClassUtils.removeParents(classes);

		if (trace)
			log.trace("Filtering out parent classes; left out with " + classes);

		return classes;
	}

	Class[] getInterfaces(ServiceReference reference) {
		if (greedyProxying) {
			Class[] clazzes = discoverProxyClasses(reference);
			if (log.isTraceEnabled())
				log.trace("generating 'greedy' service proxy using classes " + ObjectUtils.nullSafeToString(clazzes)
						+ " over " + ObjectUtils.nullSafeToString(this.classes));
			return clazzes;
		}
		// no greedy proxy, return just the configured classes
		return classes;
	}
}
