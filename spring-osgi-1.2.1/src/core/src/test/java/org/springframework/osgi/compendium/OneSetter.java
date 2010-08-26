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

package org.springframework.osgi.compendium;

/**
 * @author Costin Leau
 * 
 */
public class OneSetter {

	private Long prop;
	private Class clz;

	public Long getProp() {
		return prop;
	}

	public void setProp(Long prop) {
		this.prop = prop;
	}

	/**
	 * Returns the clz.
	 *
	 * @return Returns the clz
	 */
	public Class getClz() {
		return clz;
	}

	/**
	 * @param clz The clz to set.
	 */
	public void setClz(Class clz) {
		this.clz = clz;
	}
}
