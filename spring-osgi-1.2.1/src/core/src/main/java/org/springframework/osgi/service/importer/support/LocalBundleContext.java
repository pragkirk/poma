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

import org.osgi.framework.BundleContext;

/**
 * Class containing static methods used to obtain information about the current
 * OSGi service invocation.
 * 
 * <p>
 * The <code>getInvokerBundleContext()</code> method offers access to the
 * {@link BundleContext} of the entity accessing an OSGi service. The invoked
 * entity can thus discover information about the caller context.
 * 
 * <p>
 * The functionality in this class might be used by a target object that needed
 * access to resources on the invocation. However, this approach should not be
 * used when there is a reasonable alternative, as it makes application code
 * dependent on usage under AOP and the Spring Dynamic Modules and AOP framework
 * in particular.
 * 
 * @author Andy Piper
 * @author Costin Leau
 */
public abstract class LocalBundleContext {

	/**
	 * ThreadLocal holder for the invoker context.
	 */
	private final static ThreadLocal invokerBundleContext = new InheritableThreadLocal();

	/**
	 * Try to get the current invoker bundle context. Note that this can be
	 * <code>null</code> if the caller is not a Spring-DM importer.
	 * 
	 * @return the invoker bundle context (can be null)
	 */
	public static BundleContext getInvokerBundleContext() {
		return (BundleContext) invokerBundleContext.get();
	}

	/**
	 * Set the invoker bundle context. Note that callers should take care in
	 * cleaning up the thread-local when the invocation ends.
	 * 
	 * @param bundleContext invoker bundle context
	 * @return the old context in case there was one; maybe <code>null</code>
	 * is none is set
	 */
	static BundleContext setInvokerBundleContext(BundleContext bundleContext) {
		BundleContext old = (BundleContext) invokerBundleContext.get();
		invokerBundleContext.set(bundleContext);
		return old;
	}

}
