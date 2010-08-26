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

package org.springframework.osgi.mock;

import java.util.Dictionary;

import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

/**
 * Filter mock.
 * 
 * <p/> Just a no-op interface implementation.
 * 
 * @author Costin Leau
 */
public class MockFilter implements Filter {

	private String filter;


	/**
	 * Constructs a new <code>MockFilter</code> instance.
	 * 
	 */
	public MockFilter() {
		this("<no filter>");
	}

	/**
	 * Constructs a new <code>MockFilter</code> instance.
	 * 
	 * @param filter OSGi filter
	 */
	public MockFilter(String filter) {
		this.filter = filter;
	}

	public boolean match(ServiceReference reference) {
		return false;
	}

	public boolean match(Dictionary dictionary) {
		return false;
	}

	public boolean matchCase(Dictionary dictionary) {
		return false;
	}

	public String toString() {
		return filter;
	}
}