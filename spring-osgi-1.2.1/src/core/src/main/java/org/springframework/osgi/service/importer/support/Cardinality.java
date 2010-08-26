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
 * Enum-like class containing the OSGi importer services cardinality. Indicates
 * the number of expected matching services and whether the presence is
 * mandatory or not.
 * 
 * @author Costin Leau
 */
public class Cardinality extends StaticLabeledEnum {

	private static final long serialVersionUID = 6377096464873348405L;

	/**
	 * Optional, single cardinality. At most one OSGi service is expected. This
	 * cardinality indicates an OSGi service reference proxy.
	 */
	public static final Cardinality C_0__1 = new Cardinality(0, "0..1");

	/**
	 * Optional, multiple cardinality. Zero, one or multiple OSGi services are
	 * expected. This cardinality indicates an OSGi service managed collection.
	 */
	public static final Cardinality C_0__N = new Cardinality(1, "0..N");

	/**
	 * Mandatory, single cardinality. Exactly one OSGi service is expected. This
	 * cardinality indicates an OSGi service reference proxy.
	 */
	public static final Cardinality C_1__1 = new Cardinality(2, "1..1");

	/**
	 * Mandatory, multiple cardinality. At least one OSGi service is expected.
	 * This cardinality indicates an OSGi service managed collection.
	 */
	public static final Cardinality C_1__N = new Cardinality(3, "1..N");


	/**
	 * Indicates if this cardinality implies that at most one service is
	 * expected.
	 * 
	 * @return true if the given cardinality is single, false otherwise
	 */
	public boolean isSingle() {
		return Cardinality.C_0__1.equals(this) || Cardinality.C_1__1.equals(this);
	}

	/**
	 * Indicates if this cardinality implies that multiple services are
	 * expected.
	 * 
	 * @return true if this cardinality is multiple, false otherwise
	 */
	public boolean isMultiple() {
		return Cardinality.C_0__N.equals(this) || Cardinality.C_1__N.equals(this);
	}

	/**
	 * Indicates if this cardinality implies that at least one service is
	 * expected (mandatory cardinality).
	 * 
	 * @return true if this cardinality is mandatory, false otherwise
	 */
	public boolean isMandatory() {
		return Cardinality.C_1__1.equals(this) || Cardinality.C_1__N.equals(this);
	}

	/**
	 * Indicates if this cardinality implies that is acceptable for no matching
	 * services to be found.
	 * 
	 * @return true if this cardinality is optional, false otherwise
	 */
	public boolean isOptional() {
		return Cardinality.C_0__N.equals(this) || Cardinality.C_0__1.equals(this);
	}

	/**
	 * Constructs a new <code>Cardinality</code> instance.
	 * 
	 * @param code
	 * @param label
	 */
	private Cardinality(int code, String label) {
		super(code, label);
	}
}
