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

package org.springframework.osgi.iandt.proxycreator;

/**
 * Default implementation.
 * 
 * @author Costin Leau
 * 
 */
public class SomeInterfaceImplementation implements SomeInterface {

	public static int INVOCATION = 0;


	public String doSmth() {
		INVOCATION++;
		throw new UnsupportedOperationException("the original target should not be invoked");
	}
}
