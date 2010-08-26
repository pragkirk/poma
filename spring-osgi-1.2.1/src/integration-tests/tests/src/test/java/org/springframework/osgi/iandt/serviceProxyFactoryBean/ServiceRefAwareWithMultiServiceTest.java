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

package org.springframework.osgi.iandt.serviceProxyFactoryBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.ServiceRegistration;
import org.springframework.core.InfrastructureProxy;
import org.springframework.osgi.service.importer.ImportedOsgiServiceProxy;
import org.springframework.osgi.service.importer.support.Cardinality;
import org.springframework.osgi.service.importer.support.OsgiServiceCollectionProxyFactoryBean;

/**
 * @author Costin Leau
 * 
 */
public class ServiceRefAwareWithMultiServiceTest extends ServiceBaseTest {

	private OsgiServiceCollectionProxyFactoryBean fb;


	protected void onSetUp() throws Exception {
		fb = new OsgiServiceCollectionProxyFactoryBean();
		fb.setBundleContext(bundleContext);
		fb.setBeanClassLoader(getClass().getClassLoader());
	}

	protected void onTearDown() throws Exception {
		fb = null;
	}

	public void testProxyForMultipleCardinality() throws Exception {
		fb.setCardinality(Cardinality.C_0__N);
		fb.setInterfaces(new Class[] { Date.class });
		fb.afterPropertiesSet();

		List registrations = new ArrayList(3);

		long time = 321;
		Date dateA = new Date(time);

		try {
			Object result = fb.getObject();
			assertTrue(result instanceof Collection);
			Collection col = (Collection) result;

			assertTrue(col.isEmpty());
			Iterator iter = col.iterator();

			assertFalse(iter.hasNext());
			registrations.add(publishService(dateA));
			assertTrue(iter.hasNext());
			Object service = iter.next();
			assertTrue(service instanceof Date);
			assertEquals(time, ((Date) service).getTime());

			assertTrue(service instanceof ImportedOsgiServiceProxy);
			assertNotNull(((ImportedOsgiServiceProxy) service).getServiceReference());
			assertSame(dateA, ((InfrastructureProxy) service).getWrappedObject());

			assertFalse(iter.hasNext());
			time = 111;
			Date dateB = new Date(time);
			registrations.add(publishService(dateB));
			assertTrue(iter.hasNext());
			service = iter.next();
			assertTrue(service instanceof Date);
			assertEquals(time, ((Date) service).getTime());
			assertTrue(service instanceof ImportedOsgiServiceProxy);
			assertNotNull(((ImportedOsgiServiceProxy) service).getServiceReference());

			assertTrue(service instanceof InfrastructureProxy);
			assertSame(dateB, ((InfrastructureProxy) service).getWrappedObject());
		}
		finally {
			for (int i = 0; i < registrations.size(); i++) {
				((ServiceRegistration) registrations.get(i)).unregister();
			}
		}
	}
}
