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

package org.springframework.osgi.extensions.annotation;

import org.springframework.osgi.service.importer.support.Cardinality;

/**
 * Spring-DM managed service cardinality.
 * 
 * @author Andy Piper
 */
public enum ServiceReferenceCardinality {
	/** @see Cardinality#C_0__1 */
	C0__1(Cardinality.C_0__1),
	/** @see Cardinality#C_0__N */
	C0__N(Cardinality.C_0__N),
	/** @see Cardinality#C_1__1 */
	C1__1(Cardinality.C_1__1),
	/** @see Cardinality#C_1__N */
	C1__N(Cardinality.C_1__N);

	private Cardinality cardValue;


	private ServiceReferenceCardinality(Cardinality c) {
		cardValue = c;
	}

	public String toString() {
		return cardValue.getLabel();
	}

	public Cardinality toCardinality() {
		return cardValue;
	}

}
