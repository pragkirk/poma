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

package org.springframework.osgi.context.support;

import org.springframework.beans.factory.BeanClassLoaderAware;

/**
 * Default implementation of {@link ContextClassLoaderProvider} interface.
 * 
 * It returns the given application context class loader if it is set, falling
 * back to the current thread context class loader otherwise (in effect, leaving
 * the TCCL as it is).
 * 
 * @author Costin Leau
 */
public class DefaultContextClassLoaderProvider implements ContextClassLoaderProvider, BeanClassLoaderAware {

	private ClassLoader beanClassLoader;


	public ClassLoader getContextClassLoader() {
		return (beanClassLoader != null ? beanClassLoader : Thread.currentThread().getContextClassLoader());
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}
}
