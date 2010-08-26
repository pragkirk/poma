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
package org.springframework.osgi.internal.service.interceptor;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Dummy MethodInvocation.
 * 
 * @author Costin Leau
 * 
 */
public class MockMethodInvocation implements MethodInvocation {

	private final Method m;

	private final Object[] args;

	public MockMethodInvocation(Method m) {
		this(m, null);
	}

	public MockMethodInvocation(Method m, Object[] args) {
		this.m = m;
		this.args = (args == null ? new Object[0] : args);
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInvocation#getMethod()
	 */
	public Method getMethod() {
		return m;
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.Invocation#getArguments()
	 */
	public Object[] getArguments() {
		return args;
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.Joinpoint#getStaticPart()
	 */
	public AccessibleObject getStaticPart() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.Joinpoint#getThis()
	 */
	public Object getThis() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.Joinpoint#proceed()
	 */
	public Object proceed() throws Throwable {
		return null;
	}

}
