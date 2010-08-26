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

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.springframework.osgi.internal.service.interceptor.MockMethodInvocation;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.util.ReflectionUtils;

public class LocalBundleContextAdviceTest extends TestCase {

	private MockMethodInvocation invocation;

	private LocalBundleContextAdvice interceptor;

	private BundleContext context;

	protected void setUp() throws Exception {
		Method m = ReflectionUtils.findMethod(Object.class, "hashCode");
		context = new MockBundleContext();
		interceptor = new LocalBundleContextAdvice(context);
		
		invocation = new MockMethodInvocation(m) {
			public Object proceed() throws Throwable {
				assertSame("bundle context not set", context, LocalBundleContext.getInvokerBundleContext());
				return null;
			}
		};

	}

	protected void tearDown() throws Exception {
		invocation = null;
		interceptor = null;
		context = null;
	}

	public void testInvoke() throws Throwable {
		assertNull(LocalBundleContext.getInvokerBundleContext());
		interceptor.invoke(invocation);
	}

}
