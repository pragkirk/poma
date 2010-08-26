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

package org.springframework.osgi.service.importer;

import org.osgi.framework.Filter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Default, immutable implementation for {@link OsgiServiceDependency}.
 * 
 * @author Costin Leau
 * 
 */
public class DefaultOsgiServiceDependency implements OsgiServiceDependency {

	private final String beanName;
	private final Filter filter;
	private final boolean mandatoryService;
	private final String toString;
	private final int hashCode;


	/**
	 * Constructs a new <code>DefaultOsgiServiceDependency</code> instance.
	 * 
	 * @param beanName dependency bean name (can be null)
	 * @param filter dependency OSGi filter (can be null)
	 * @param mandatoryService flag indicating whether the dependency is
	 * mandatory or not
	 */
	public DefaultOsgiServiceDependency(String beanName, Filter filter, boolean mandatoryService) {
		this.beanName = beanName;
		this.filter = filter;
		this.mandatoryService = mandatoryService;

		// calculate internal fields
		toString = "DependencyService[Name=" + (beanName != null ? beanName : "null") + "][Filter=" + filter
				+ "][Mandatory=" + mandatoryService + "]";

		int result = 17;
		result = 37 * result + DefaultOsgiServiceDependency.class.hashCode();
		result = 37 * result + (filter == null ? 0 : filter.hashCode());
		result = 37 * result + (beanName == null ? 0 : beanName.hashCode());
		result = 37 * result + (mandatoryService ? 0 : 1);
		hashCode = result;
	}

	public String getBeanName() {
		return beanName;
	}

	public Filter getServiceFilter() {
		return filter;
	}

	public boolean isMandatory() {
		return mandatoryService;
	}

	public String toString() {
		return toString;
	}

	public boolean equals(Object obj) {
		if (obj instanceof OsgiServiceDependency) {
			OsgiServiceDependency other = (OsgiServiceDependency) obj;
			return (other.isMandatory() == mandatoryService && filter.equals(other.getServiceFilter()) && ObjectUtils.nullSafeEquals(
				beanName, other.getBeanName()));
		}
		return false;
	}

	public int hashCode() {
		return hashCode;
	}
}
