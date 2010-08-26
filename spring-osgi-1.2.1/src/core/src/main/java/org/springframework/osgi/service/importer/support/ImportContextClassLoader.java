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

package org.springframework.osgi.service.importer.support;

import org.springframework.core.enums.StaticLabeledEnum;

/**
 * Enum-like class containing the OSGi service importer thread context class
 * loader (TCCL) management options.
 * 
 * @author Costin Leau
 */
public class ImportContextClassLoader extends StaticLabeledEnum {

	private static final long serialVersionUID = -7054525261814306077L;

	/**
	 * The TCCL will not be managed upon service invocation.
	 */
	public static final ImportContextClassLoader UNMANAGED = new ImportContextClassLoader(0, "UNMANAGED");

	/**
	 * The TCCL will be set to the service provider upon service invocation.
	 */
	public static final ImportContextClassLoader SERVICE_PROVIDER = new ImportContextClassLoader(1, "SERVICE_PROVIDER");

	/**
	 * The TCCL will be set to the service provider upon service invocation.
	 */
	public static final ImportContextClassLoader CLIENT = new ImportContextClassLoader(2, "CLIENT");


	/**
	 * Constructs a new <code>ImportContextClassLoader</code> instance.
	 * 
	 * @param code
	 * @param label
	 */
	private ImportContextClassLoader(int code, String label) {
		super(code, label);
	}

}
