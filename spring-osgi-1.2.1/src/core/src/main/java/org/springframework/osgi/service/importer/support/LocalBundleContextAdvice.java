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
 *
 */

package org.springframework.osgi.service.importer.support;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.util.OsgiBundleUtils;

/**
 * Simple interceptor for temporarily pushing the invoker BundleContext to a
 * threadLocal.
 * 
 * This class has package visibility to be able to access the
 * {@link LocalBundleContextAdvice} setter method.
 * 
 * <strong>Note</strong>: This class is state-less so the same instance can be
 * used by several proxies at the same time.
 * 
 * @author Andy Piper
 * @author Costin Leau
 */
class LocalBundleContextAdvice implements MethodInterceptor {

	private static final int hashCode = LocalBundleContextAdvice.class.hashCode() * 13;

	private final BundleContext context;


	LocalBundleContextAdvice(Bundle bundle) {
		this(OsgiBundleUtils.getBundleContext(bundle));
	}

	LocalBundleContextAdvice(BundleContext bundle) {
		this.context = bundle;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		// save the old context
		BundleContext oldContext = LocalBundleContext.setInvokerBundleContext(context);

		try {
			return invocation.proceed();
		}
		finally {
			// restore old context
			LocalBundleContext.setInvokerBundleContext(oldContext);
		}
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof LocalBundleContextAdvice) {
			LocalBundleContextAdvice oth = (LocalBundleContextAdvice) other;
			return context.equals(oth.context);
		}
		return false;
	}

	public int hashCode() {
		return hashCode;
	}
}
