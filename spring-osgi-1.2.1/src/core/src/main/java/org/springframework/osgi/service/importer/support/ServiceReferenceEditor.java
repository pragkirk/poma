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

package org.springframework.osgi.service.importer.support;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.osgi.framework.ServiceReference;
import org.springframework.osgi.service.importer.ImportedOsgiServiceProxy;

/**
 * {@link PropertyEditor} that converts an &lt;osgi:reference&gt; element into a
 * {@link ServiceReference}. That is, it allows conversion between a
 * Spring-managed OSGi service to a Spring-managed ServiceReference.
 * 
 * <p/> Automatically registered by
 * {@link org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext}
 * implementations.
 * 
 * @author Costin Leau
 * @see ImportedOsgiServiceProxy
 */
public class ServiceReferenceEditor extends PropertyEditorSupport {

	/**
	 * {@inheritDoc}
	 * 
	 * <p/> Converts the given text value to a ServiceReference.
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		throw new IllegalArgumentException("this property editor works only with "
				+ ImportedOsgiServiceProxy.class.getName());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p/> Converts the given value to a ServiceReference.
	 */
	public void setValue(Object value) {
		// nulls allowed
		if (value == null) {
			super.setValue(null);
			return;
		}

		if (value instanceof ImportedOsgiServiceProxy) {
			ImportedOsgiServiceProxy serviceProxy = (ImportedOsgiServiceProxy) value;
			super.setValue(serviceProxy.getServiceReference());
			return;
		}

		if (value instanceof ServiceReference) {
			super.setValue(value);
			return;
		}

		throw new IllegalArgumentException("Expected a service of type " + ImportedOsgiServiceProxy.class.getName()
				+ " but received type " + value.getClass());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p/> This implementation returns <code>null</code> to indicate that
	 * there is no appropriate text representation.
	 */
	public String getAsText() {
		return null;
	}
}