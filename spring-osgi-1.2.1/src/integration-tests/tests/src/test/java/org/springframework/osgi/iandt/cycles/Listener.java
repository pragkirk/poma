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

package org.springframework.osgi.iandt.cycles;

import java.util.Map;

/**
 * Simple custom listener used for testing cyclic injection.
 * 
 * @author Costin Leau
 */
public abstract class Listener {

	private Object target;


	public void bind(Object service, Map properties) {
	}

	/**
	 * Returns the target.
	 * 
	 * @return Returns the target
	 */
	public Object getTarget() {
		return target;
	}

	/**
	 * @param target The target to set.
	 */
	public void setTarget(Object target) {
		this.target = target;
	}
}
