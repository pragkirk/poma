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

import java.beans.PropertyEditorSupport;

/**
 * Taken from the Spring reference documentation.
 * 
 * The name has been changed so the javabeans doesn't automatically pick the
 * editor (and thus the registration from the bean factory is checked).
 */
public class ExoticTypeCustomEditor extends PropertyEditorSupport {

	private String format;


	public void setFormat(String format) {
		this.format = format;
	}

	public void setAsText(String text) {
		if (format != null && format.equals("upperCase")) {
			text = text.toUpperCase();
		}
		ExoticType type = new ExoticType(text);
		setValue(type);
	}
}
