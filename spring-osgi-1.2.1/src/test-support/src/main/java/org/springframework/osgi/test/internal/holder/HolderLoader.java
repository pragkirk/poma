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

package org.springframework.osgi.test.internal.holder;

import java.lang.reflect.Field;

import org.osgi.framework.Bundle;
import org.springframework.util.ReflectionUtils;

/**
 * Specific OSGi loader for OsgiTestHolder. It's main usage is to load the
 * holder using a class-loader outside the OSGi world and to store results
 * there.
 * 
 * <p/> Boot delegation should work here but each platform has its own approach.
 * Notably, Equinox uses by default the boot (not the app) classloader which
 * means the classpath used for starting OSGi is not seen. To not interfere with
 * the default configuration which might change in the future (it has been
 * changed between 3.2 and 3.3) and to not impose restrictions on the test
 * usage, the loader manually discovers the proper classloader and uses it to
 * load the holder class. Inside OSGi, special care must be taken to make sure
 * no CCE are generated.
 * 
 * @author Costin Leau
 * 
 */
public class HolderLoader {

	private static final String INSTANCE_FIELD = "INSTANCE";

	private static final String HOLDER_CLASS_NAME = "org.springframework.osgi.test.internal.holder.OsgiTestInfoHolder";

	public static final HolderLoader INSTANCE = new HolderLoader();

	private final OsgiTestInfoHolder holder;


	public HolderLoader() {
		// try to load the holder using the app ClassLoader
		ClassLoader appCL = Bundle.class.getClassLoader();
		Class clazz;
		try {
			clazz = appCL.loadClass(HOLDER_CLASS_NAME);
		}
		catch (Exception ex) {
			// if it's not found, then the class path is incorrectly constructed
			throw (RuntimeException) new IllegalStateException(
				"spring-osgi-test.jar is not available on the boot class path; are you deploying the test framework"
						+ "as a bundle by any chance? ").initCause(ex);
		}
		// get the static instance
		Field field = ReflectionUtils.findField(clazz, INSTANCE_FIELD, clazz);
		Object instance;
		try {
			instance = field.get(null);
		}
		catch (Exception ex) {
			throw (RuntimeException) new IllegalStateException("Cannot read property " + INSTANCE_FIELD).initCause(ex);
		}
		// once the class is loaded return it wrapped through it's OSGi instance
		holder = new ReflectionOsgiHolder(instance);
	}

	public OsgiTestInfoHolder getHolder() {
		return holder;
	}

}
