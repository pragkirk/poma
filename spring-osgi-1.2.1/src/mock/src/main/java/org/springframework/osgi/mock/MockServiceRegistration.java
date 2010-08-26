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

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * ServiceRegistration mock.
 * 
 * <p/> The mock allows the service properties modification (through
 * {@link #setProperties(Dictionary)}) as long as the underlying reference is
 * of type {@link MockServiceReference}.
 * 
 * @author Costin Leau
 * 
 */
public class MockServiceRegistration implements ServiceRegistration {

	private ServiceReference reference;


	/**
	 * Constructs a new <code>MockServiceRegistration</code> instance using
	 * defaults.
	 * 
	 */
	public MockServiceRegistration() {
		this(null);
	}

	/**
	 * Constructs a new <code>MockServiceRegistration</code> instance with the
	 * given properties.
	 * 
	 * @param props registration properties
	 */
	public MockServiceRegistration(Dictionary props) {
		this(null, props);
	}

	/**
	 * Constructs a new <code>MockServiceRegistration</code> instance using
	 * the given class names and properties.
	 * 
	 * @param clazz
	 * @param props
	 */
	public MockServiceRegistration(String[] clazz, Dictionary props) {
		reference = new MockServiceReference(null, props, this, clazz);
	}

	public ServiceReference getReference() {
		return reference;
	}

	/**
	 * Sets the service reference associated with this registration.
	 * 
	 * @param reference service reference
	 */
	public void setReference(ServiceReference reference) {
		this.reference = reference;
	}

	public void setProperties(Dictionary props) {
		if (reference instanceof MockServiceReference)
			((MockServiceReference) reference).setProperties(props);
		else
			throw new IllegalArgumentException("cannot update properties - service reference is not a "
					+ MockServiceReference.class.getName());
	}

	public void unregister() {
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof MockServiceRegistration)
			return this.reference.equals(((MockServiceRegistration) obj).reference);
		return false;
	}

	public int hashCode() {
		return MockServiceRegistration.class.hashCode() * 13 + reference.hashCode();
	}
}