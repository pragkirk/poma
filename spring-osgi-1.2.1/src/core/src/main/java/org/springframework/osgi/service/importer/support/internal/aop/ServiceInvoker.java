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

package org.springframework.osgi.service.importer.support.internal.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.DisposableBean;

/**
 * Around interceptor for OSGi service invokers. Uses method invocation to
 * execute the call.
 * 
 * <p/> A {@link TargetSource} can be used though it doesn't offer localized
 * exceptions (unless information is passed around). The biggest difference as
 * opposed to a target source is that mixins call do not require a service
 * behind.
 * 
 * However, in the future, this interceptor might be replaced with a
 * TargetSource.
 * 
 * @author Costin Leau
 * 
 */
public abstract class ServiceInvoker implements MethodInterceptor, ServiceReferenceProvider, DisposableBean {

	protected transient final Log log = LogFactory.getLog(getClass());


	/**
	 * Actual invocation - the class is being executed on a different object
	 * then the one exposed in the invocation object.
	 * 
	 * @param service
	 * @param invocation
	 * @return
	 * @throws Throwable
	 */
	protected Object doInvoke(Object service, MethodInvocation invocation) throws Throwable {
		return AopUtils.invokeJoinpointUsingReflection(service, invocation.getMethod(), invocation.getArguments());
	}

	public final Object invoke(MethodInvocation invocation) throws Throwable {
		return doInvoke(getTarget(), invocation);
	}

	/**
	 * Determine the target object to execute the invocation upon.
	 * 
	 * @return
	 * @throws Throwable
	 */
	protected abstract Object getTarget();

	/**
	 * Convenience method exposing the target (OSGi service) reference so that
	 * subinterceptors can access it. By default, returns null.
	 * 
	 * @return
	 */
	public ServiceReference getServiceReference() {
		return null;
	}

	// override so no exception is thrown
	public abstract void destroy();
}