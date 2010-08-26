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

package org.springframework.osgi.service.importer.support.internal.aop;

import java.util.Comparator;

import org.springframework.beans.BeanUtils;
import org.springframework.osgi.service.importer.ServiceReferenceProxy;
import org.springframework.osgi.service.importer.support.internal.collection.comparator.ServiceReferenceComparator;

/**
 * Base {@link ServiceReferenceProxy} used for instantiating the OSGi service
 * comparator.
 * 
 * In DM 2.0, this class is going to be removed since the Services will
 * automatically implement Comparable.
 * 
 * @author Costin Leau
 */
// TODO: remove in DM 2.0
abstract class BaseServiceReferenceProxy implements ServiceReferenceProxy {

	protected static final Comparator COMPARATOR;

	static {
		Class comparatorClass = ServiceReferenceComparator.class;
		COMPARATOR = (Comparator) BeanUtils.instantiateClass(comparatorClass);
	}
}