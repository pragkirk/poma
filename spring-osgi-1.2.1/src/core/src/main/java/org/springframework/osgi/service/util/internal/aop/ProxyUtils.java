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

package org.springframework.osgi.service.util.internal.aop;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.osgi.framework.BundleContext;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.osgi.util.DebugUtils;
import org.springframework.osgi.util.internal.ClassUtils;

/**
 * Simple utility for creating Spring AOP proxies.
 * 
 * @author Costin Leau
 * 
 */
public abstract class ProxyUtils {

	public static Object createProxy(Class[] classes, Object target, ClassLoader classLoader,
			BundleContext bundleContext, List advices) {
		return createProxy(classes, target, classLoader, bundleContext,
			(advices != null ? (Advice[]) advices.toArray(new Advice[advices.size()]) : new Advice[0]));
	}

	public static Object createProxy(Class[] classes, Object target, final ClassLoader classLoader,
			BundleContext bundleContext, Advice[] advices) {
		final ProxyFactory factory = new ProxyFactory();

		ClassUtils.configureFactoryForClass(factory, classes);

		for (int i = 0; i < advices.length; i++) {
			factory.addAdvice(advices[i]);
		}

		if (target != null)
			factory.setTarget(target);

		// no need to add optimize since it means implicit usage of CGLib always
		// which is determined automatically anyway
		// factory.setOptimize(true);
		factory.setFrozen(true);
		factory.setOpaque(true);
		try {
			return AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					return factory.getProxy(classLoader);
				}
			});
		}
		catch (NoClassDefFoundError ncdfe) {
			DebugUtils.debugClassLoadingThrowable(ncdfe, bundleContext.getBundle(), classes);
			throw ncdfe;
		}
	}
}
