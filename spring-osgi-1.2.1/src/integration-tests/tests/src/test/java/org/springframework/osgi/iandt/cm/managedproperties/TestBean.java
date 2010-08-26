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

package org.springframework.osgi.iandt.cm.managedproperties;

import java.util.Map;

/**
 * 
 * @author Costin Leau
 * 
 */
public class TestBean {

	private String string;
	private Integer integer;
	private Class clazz;
	private Map props;
	private ExoticType exoticType;


	/**
	 * Returns the exoticType.
	 * 
	 * @return Returns the exoticType
	 */
	public ExoticType getExoticType() {
		return exoticType;
	}

	/**
	 * @param exoticType The exoticType to set.
	 */
	public void setExoticType(ExoticType exoticType) {
		this.exoticType = exoticType;
	}

	/**
	 * Returns the string.
	 * 
	 * @return Returns the string
	 */
	public String getString() {
		return string;
	}

	/**
	 * @param string The string to set.
	 */
	public void setString(String string) {
		this.string = string;
	}

	/**
	 * Returns the integer.
	 * 
	 * @return Returns the integer
	 */
	public Integer getInteger() {
		return integer;
	}

	/**
	 * @param integer The integer to set.
	 */
	public void setInteger(Integer integer) {
		this.integer = integer;
	}

	/**
	 * Returns the clazz.
	 * 
	 * @return Returns the clazz
	 */
	public Class getClazz() {
		return clazz;
	}

	/**
	 * @param clazz The clazz to set.
	 */
	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	public Map getProps() {
		return props;
	}

	public void update(Map properties) {
		System.out.println("Received properties " + properties);
		this.props = properties;
	}

	public void init() {
		if (this.props == null && string == null)
			throw new IllegalStateException("Arguments not initialized before init() method called");
	}

}
