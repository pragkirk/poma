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

package org.springframework.osgi.config.internal.util;

import java.lang.reflect.Method;

import org.springframework.core.JdkVersion;

/**
 * Internal class that deals with Method handling. The main intent for this
 * class is to support bridge methods without requiring a JDK 5 to compile
 * (since maven will use the same VM for tests which is not what we want as we
 * do integration testing).
 * 
 * @author Costin Leau
 * 
 */
public abstract class MethodUtils {

	private static final int BRIDGE = 0x00000040;

	/** flag used for skipping bridged methods */
	private static final boolean isJDK5 = JdkVersion.isAtLeastJava15();


	public static boolean isBridge(Method method) {
		if (isJDK5) {
			return (method.getModifiers() & BRIDGE) != 0;
		}
		return false;
	}
}
