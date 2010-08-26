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

package org.springframework.osgi.iandt.bridgemethods;

import java.awt.Shape;
import java.util.Map;

/**
 * Generified listener causing bridged methods to be created.
 * 
 * @author Costin Leau
 * 
 */
public class Listener implements GenerifiedListenerInterface<Shape> {

	public static int BIND_CALLS = 0;
	public static int UNBIND_CALLS = 0;


	public void bind(Shape service, Map<String, ?> properties) {
		System.out.println("calling bind");
		BIND_CALLS++;
	}

	public void unbind(Shape service, Map<String, ?> properties) {
		System.out.println("calling unbind");
		UNBIND_CALLS++;
	}
}
