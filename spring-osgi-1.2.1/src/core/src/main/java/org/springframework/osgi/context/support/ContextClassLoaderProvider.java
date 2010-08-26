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
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ResourceLoader;

/**
 * Strategy interface for plugging various thread context class loaders during
 * an OSGi application context critical life cycle events.
 * 
 * @see Thread#getContextClassLoader()
 * @see AbstractDelegatedExecutionApplicationContext
 * @see ResourceLoader#getClassLoader()
 * @see BeanClassLoaderAware
 * @see ApplicationContextAware
 * 
 * @author Costin Leau
 */
public interface ContextClassLoaderProvider {

	/**
	 * Returns the context class loader to be used by the OSGi application
	 * context during its life cycle events.
	 * 
	 * @return class loader used as a thread context class loader
	 */
	ClassLoader getContextClassLoader();
}
