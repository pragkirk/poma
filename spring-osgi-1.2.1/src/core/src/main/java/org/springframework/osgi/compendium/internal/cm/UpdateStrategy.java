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

package org.springframework.osgi.compendium.internal.cm;

import org.springframework.core.enums.StaticLabeledEnum;

/**
 * Enum-like class providing the possible update strategies for managed-service
 * beans.
 * 
 * @author Costin Leau
 * 
 */
public class UpdateStrategy extends StaticLabeledEnum {

	public static final UpdateStrategy NONE = new UpdateStrategy(1, "none");

	public static final UpdateStrategy BEAN_MANAGED = new UpdateStrategy(2, "bean-managed");

	public static final UpdateStrategy CONTAINER_MANAGED = new UpdateStrategy(3, "container-managed");


	/**
	 * Constructs a new <code>UpdateStrategy</code> instance.
	 * 
	 * @param code
	 * @param label
	 */
	private UpdateStrategy(int code, String label) {
		super(code, label);
	}
}
