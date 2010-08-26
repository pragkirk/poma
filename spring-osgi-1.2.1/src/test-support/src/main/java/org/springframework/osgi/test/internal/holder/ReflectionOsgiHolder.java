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

package org.springframework.osgi.test.internal.holder;

import java.lang.reflect.Method;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * OSGi adapter for the information holder. Overrides the methods used inside
 * OSGi to use reflection and avoid CCE.
 * 
 * @author Costin Leau
 * 
 */
class ReflectionOsgiHolder extends OsgiTestInfoHolder {

	private final Object instance;

	private final Method GET_TEST_BUNDLE_ID, GET_TEST_CLASS_NAME, GET_TEST_METHOD_NAME, ADD_TEST_ERROR,
			ADD_TEST_FAILURE;


	/**
	 * Constructs a new <code>OsgiTestInfoHolder</code> instance wrapping the
	 * given object and accessing it through reflection. This constructor is
	 * used for accessing the instance loaded outside OSGi, from within OSGi.
	 * 
	 * @param twinInstance
	 */
	ReflectionOsgiHolder(Object twinInstance) {
		Assert.notNull(twinInstance);
		this.instance = twinInstance;
		Class clazz = instance.getClass();
		GET_TEST_BUNDLE_ID = ReflectionUtils.findMethod(clazz, "getTestBundleId");
		GET_TEST_CLASS_NAME = ReflectionUtils.findMethod(clazz, "getTestClassName");
		GET_TEST_METHOD_NAME = ReflectionUtils.findMethod(clazz, "getTestMethodName");

		ADD_TEST_ERROR = ReflectionUtils.findMethod(clazz, "addTestError", new Class[] { Throwable.class });
		ADD_TEST_FAILURE = ReflectionUtils.findMethod(clazz, "addTestFailure", new Class[] { Throwable.class });

	}

	public Long getTestBundleId() {
		return (Long) ReflectionUtils.invokeMethod(GET_TEST_BUNDLE_ID, instance);
	}

	public String getTestClassName() {
		return (String) ReflectionUtils.invokeMethod(GET_TEST_CLASS_NAME, instance);
	}

	public String getTestMethodName() {
		return (String) ReflectionUtils.invokeMethod(GET_TEST_METHOD_NAME, instance);
	}

	public void addTestError(Throwable testProblem) {
		ReflectionUtils.invokeMethod(ADD_TEST_ERROR, instance, new Object[] { testProblem });
	}

	public void addTestFailure(Throwable testProblem) {
		ReflectionUtils.invokeMethod(ADD_TEST_FAILURE, instance, new Object[] { testProblem });
	}

}
