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

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceInvoker;

/**
 * @author Costin Leau
 * 
 */
public class OsgiServiceInvokerTest extends TestCase {

	private ServiceInvoker invoker;

	private Object target;


	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		target = new Object();
		invoker = new ServiceInvoker() {

			protected Object getTarget() {
				return target;
			}

			public void destroy() {
			}
		};
	}

	protected void tearDown() throws Exception {
		target = null;
		invoker = null;
	}

	/**
	 * Test method for
	 * {@link org.springframework.osgi.service.interceptor.ServiceInvoker#invoke(org.aopalliance.intercept.MethodInvocation)}.
	 */
	public void testInvoke() throws Throwable {
		MethodInvocation invocation = new MockMethodInvocation(Object.class.getMethod("hashCode", null));
		Object result = invoker.invoke(invocation);
		assertEquals("different target invoked", new Integer(target.hashCode()), result);
	}

	public void testExceptionUnwrapping() throws Throwable {
		MethodInvocation invocation = new MockMethodInvocation(Integer.class.getMethod("parseInt",
			new Class[] { String.class }), new Object[] { "invalid number" });

		try {
			invoker.invoke(invocation);
			fail("should have thrown exception" + NumberFormatException.class);
		}
		catch (NumberFormatException nfe) {
			// expected
		}
	}
}
