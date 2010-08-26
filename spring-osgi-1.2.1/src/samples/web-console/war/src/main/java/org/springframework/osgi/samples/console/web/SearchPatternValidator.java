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

package org.springframework.osgi.samples.console.web;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Basic validator for search pattern given by the user. Its main purpose is to
 * forbid the presence of a prefix.
 * 
 * @author Costin Leau
 */
public class SearchPatternValidator implements Validator {

	private static final String COLON = ":";


	public boolean supports(Class clazz) {
		return String.class.equals(clazz);
	}

	public void validate(Object target, Errors errors) {
		String pattern = (String) target;

		// validate only if there is some text
		if (StringUtils.hasText(pattern)) {
			if (pattern.contains(COLON)) {
				errors.rejectValue("searchPattern", "invalid.prefix", "No prefix (:) should be specified");
			}
		}
	}
}
