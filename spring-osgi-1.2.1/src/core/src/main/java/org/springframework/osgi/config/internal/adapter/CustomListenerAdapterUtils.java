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

package org.springframework.osgi.config.internal.adapter;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.osgi.config.internal.util.MethodUtils;
import org.springframework.osgi.util.internal.ReflectionUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Local utility class used by adapters. Handles things such as method
 * discovery.
 * 
 * 
 * @author Costin Leau
 * 
 */
abstract class CustomListenerAdapterUtils {

	private static final Log log = LogFactory.getLog(CustomListenerAdapterUtils.class);


	/**
	 * Specialized reflection utility that determines all methods that accept
	 * two parameters such:
	 * 
	 * <pre>
	 * methodName(Type serviceType, Type1 arg)
	 * methodName(Type serviceType, Type2 arg)
	 * methodName(AnotherType serviceType, Type1 arg).
	 * </pre>
	 * 
	 * It will return a map which has the serviceType (first argument) as type
	 * and contains as list the variants of methods using the second argument.
	 * This method is normally used by listeners when determining custom
	 * methods.
	 * 
	 * @param methodName
	 * @param possibleArgumentTypes
	 * @return
	 */
	static Map determineCustomMethods(final Class target, final String methodName, final Class[] possibleArgumentTypes) {

		if (!StringUtils.hasText(methodName)) {
			return Collections.EMPTY_MAP;
		}

		Assert.notEmpty(possibleArgumentTypes);

		final Map methods = new LinkedHashMap(3);

		final boolean trace = log.isTraceEnabled();

		org.springframework.util.ReflectionUtils.doWithMethods(target,
			new org.springframework.util.ReflectionUtils.MethodCallback() {

				public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
					if (!MethodUtils.isBridge(method) && methodName.equals(method.getName())) {
						// take a look at the variables
						Class[] args = method.getParameterTypes();

						// Properties can be passed as Map or Dictionary
						if (args != null && args.length == 2) {
							Class propType = args[1];

							for (int i = 0; i < possibleArgumentTypes.length; i++) {
								Class clazz = possibleArgumentTypes[i];
								if (clazz.isAssignableFrom(propType)) {

									if (trace)
										log.trace("discovered custom method [" + method.toString() + "] on " + target);
									// see if there was a method already found
									Method m = (Method) methods.get(args[0]);

									if (m != null) {
										if (trace)
											log.trace("type " + args[0] + " already has an associated method ["
													+ m.toString() + "];ignoring " + method);
									}
									else {
										org.springframework.util.ReflectionUtils.makeAccessible(method);
										methods.put(args[0], method);
									}
								}
							}
						}
					}
				}
			});
		return methods;
	}

	/**
	 * Shortcut method that uses as possible argument types, Dictionary.class or
	 * Map.class.
	 * 
	 * @param target
	 * @param methodName
	 * @return
	 */
	static Map determineCustomMethods(Class target, final String methodName) {
		return determineCustomMethods(target, methodName, new Class[] { Dictionary.class, Map.class });
	}

	/**
	 * Invoke the custom listener method. Takes care of iterating through the
	 * method map (normally acquired through
	 * {@link #determineCustomMethods(Class, String, Class[])} and invoking the
	 * method using the arguments.
	 * 
	 * @param target
	 * @param methods
	 * @param service
	 * @param properties
	 */
	// the properties field is Dictionary implementing a Map interface
	static void invokeCustomMethods(Object target, Map methods, Object service, Map properties) {
		if (methods != null && !methods.isEmpty()) {
			boolean trace = log.isTraceEnabled();

			Object[] args = new Object[] { service, properties };
			for (Iterator iter = methods.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				Class key = (Class) entry.getKey();
				Method method = (Method) entry.getValue();
				// find the compatible types (accept null service)
				if (service == null || key.isInstance(service)) {
					if (trace)
						log.trace("Invoking listener custom method " + method);

					try {
						ReflectionUtils.invokeMethod(method, target, args);
					}
					// make sure to log exceptions and continue with the
					// rest of the methods
					catch (Exception ex) {
						Exception cause = ReflectionUtils.getInvocationException(ex);
						log.warn("Custom method [" + method + "] threw exception when passing service type ["
								+ (service != null ? service.getClass().getName() : null) + "]", cause);
					}
				}
			}
		}
	}
}
