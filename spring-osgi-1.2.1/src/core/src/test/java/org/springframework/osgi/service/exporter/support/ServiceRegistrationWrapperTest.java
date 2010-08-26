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
package org.springframework.osgi.service.exporter.support;

import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.service.exporter.OsgiServiceRegistrationListener;
import org.springframework.osgi.service.exporter.SimpleOsgiServiceRegistrationListener;
import org.springframework.osgi.service.exporter.support.internal.support.ServiceRegistrationDecorator;

public class ServiceRegistrationWrapperTest extends TestCase {

	private ServiceRegistration registration;

	private ServiceRegistration actualRegistration;

	private MockControl mc;

	protected void setUp() throws Exception {
		mc = MockControl.createControl(ServiceRegistration.class);
		actualRegistration = (ServiceRegistration) mc.getMock();

		registration = new ServiceRegistrationDecorator(new Object(), actualRegistration,
				new OsgiServiceRegistrationListener[] { new SimpleOsgiServiceRegistrationListener() });
		SimpleOsgiServiceRegistrationListener.REGISTERED = 0;
		SimpleOsgiServiceRegistrationListener.UNREGISTERED = 0;
	}

	protected void tearDown() throws Exception {
		mc.verify();
		registration = null;
	}

	public void testGetReference() {
		ServiceReference reference = new MockServiceReference();
		mc.expectAndReturn(actualRegistration.getReference(), reference);
		mc.replay();

		assertSame(reference, registration.getReference());
	}

	public void testSetProperties() {
		Dictionary props = new Hashtable();
		actualRegistration.setProperties(props);
		mc.replay();

		registration.setProperties(props);
	}

	public void testUnregister() {
		ServiceReference reference = new MockServiceReference();
		mc.expectAndReturn(actualRegistration.getReference(), reference);
		actualRegistration.unregister();
		mc.replay();

		registration.unregister();
	}

	public void testUnregistrationNotified() {
		assertEquals(0, SimpleOsgiServiceRegistrationListener.UNREGISTERED);

		ServiceReference reference = new MockServiceReference();
		mc.expectAndReturn(actualRegistration.getReference(), reference);
		actualRegistration.unregister();
		mc.replay();

		registration.unregister();

		assertEquals(1, SimpleOsgiServiceRegistrationListener.UNREGISTERED);
	}

	public void testExceptionProperlyPropagates() {
		assertEquals(0, SimpleOsgiServiceRegistrationListener.UNREGISTERED);
		IllegalStateException excep = new IllegalStateException();
		mc.expectAndThrow(actualRegistration.getReference(), excep);

		mc.replay();
		try {
			registration.unregister();
		}
		catch (IllegalStateException ise) {
			assertSame(excep, ise);
		}
		// check listener hasn't been called
		assertEquals(0, SimpleOsgiServiceRegistrationListener.UNREGISTERED);
	}

}
