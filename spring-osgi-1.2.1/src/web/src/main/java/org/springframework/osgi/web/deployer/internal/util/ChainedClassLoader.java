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

package org.springframework.osgi.web.deployer.internal.util;

import java.net.URL;

import org.springframework.util.Assert;

/**
 * Chaining classloader implementation that delegates the resource and class
 * loading to a number of class loaders passed in.
 * 
 * @author Costin Leau
 * 
 */
public class ChainedClassLoader extends ClassLoader {

	private final ClassLoader[] loaders;


	public ChainedClassLoader(ClassLoader[] loaders) {
		Assert.notEmpty(loaders);
		for (int i = 0; i < loaders.length; i++) {
			ClassLoader classLoader = loaders[i];
			Assert.notNull(classLoader, "null classloaders not allowed");
		}
		this.loaders = (ClassLoader[]) loaders.clone();
	}

	public URL getResource(String name) {
		URL url = null;
		for (int i = 0; i < loaders.length; i++) {
			ClassLoader loader = loaders[i];
			url = loader.getResource(name);
			if (url != null)
				return url;
		}
		return url;
	}

	public Class loadClass(String name) throws ClassNotFoundException {
		Class clazz = null;
		for (int i = 0; i < loaders.length; i++) {
			ClassLoader loader = loaders[i];
			try {
				clazz = loader.loadClass(name);
				return clazz;
			}
			catch (ClassNotFoundException e) {
				// keep moving through the classloaders
			}
		}
		throw new ClassNotFoundException(name);
	}
}
