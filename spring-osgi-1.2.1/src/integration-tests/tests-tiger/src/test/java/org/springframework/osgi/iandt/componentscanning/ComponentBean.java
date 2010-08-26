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

package org.springframework.osgi.iandt.componentscanning;

import java.awt.Shape;
import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Just a Spring component that relies on Spring annotations for injection.
 * 
 * @author Costin Leau
 */
@Component
public class ComponentBean {

	private Shape constructorInjection;
	@Autowired
	private Shape fieldInjection;

	private Shape setterInjection;


	@Autowired
	public ComponentBean(Shape Shape) {
		this.constructorInjection = Shape;
	}

	public ComponentBean() {
		//this.constructorShape = Shape;
	}

	/**
	 * Returns the constructorInjection.
	 * 
	 * @return Returns the constructorInjection
	 */
	public Shape getConstructorInjection() {
		return constructorInjection;
	}

	/**
	 * Returns the fieldInjection.
	 * 
	 * @return Returns the fieldInjection
	 */
	public Shape getFieldInjection() {
		return fieldInjection;
	}

	/**
	 * Returns the setterInjection.
	 * 
	 * @return Returns the setterInjection
	 */
	public Shape getSetterInjection() {
		return setterInjection;
	}

	/**
	 * @param setterInjection The setterInjection to set.
	 */
	@Autowired
	public void setSetterInjection(Shape injection) {
		this.setterInjection = injection;
	}
}