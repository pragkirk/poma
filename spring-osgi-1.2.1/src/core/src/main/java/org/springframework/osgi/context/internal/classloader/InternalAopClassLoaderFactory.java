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

package org.springframework.osgi.context.internal.classloader;

/**
 * Internal contract for creating standard AOP class loaders. Implementations
 * can differ by using caching or returning new objects on each call.
 * 
 * <p/> Implementations <b>must</b> be thread-safe since the proxy generation
 * can occur at any time.
 * 
 * @author Costin Leau
 */
interface InternalAopClassLoaderFactory {

	/**
	 * Return the AOP class loader for the given bundle.
	 * 
	 * @param classLoader OSGi bundle
	 * @return AOP class loader for it
	 */
	ChainedClassLoader createClassLoader(ClassLoader classLoader);
}
