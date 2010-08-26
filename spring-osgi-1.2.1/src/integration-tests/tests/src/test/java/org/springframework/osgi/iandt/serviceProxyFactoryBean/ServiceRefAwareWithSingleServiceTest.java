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

import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.ServiceRegistration;
import org.springframework.aop.SpringProxy;
import org.springframework.core.InfrastructureProxy;
import org.springframework.osgi.service.importer.ImportedOsgiServiceProxy;
import org.springframework.osgi.service.importer.support.Cardinality;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;

/**
 * @author Costin Leau
 * 
 */
public class ServiceRefAwareWithSingleServiceTest extends ServiceBaseTest {

	private OsgiServiceProxyFactoryBean fb;


	protected void onSetUp() throws Exception {
		fb = new OsgiServiceProxyFactoryBean();
		fb.setBundleContext(bundleContext);
		// execute retries fast
		fb.setTimeout(1);
		fb.setBeanClassLoader(getClass().getClassLoader());
	}

	protected void onTearDown() throws Exception {
		fb = null;
	}

	public void testProxyForUnaryCardinality() throws Exception {
		long time = 1234;
		Date date = new Date(time);
		ServiceRegistration reg = publishService(date);

		fb.setCardinality(Cardinality.C_1__1);

		fb.setInterfaces(new Class[] { Date.class });
		fb.afterPropertiesSet();

		ImportedOsgiServiceProxy refAware = null;
		try {
			Object result = fb.getObject();
			assertTrue(result instanceof Date);
			// check it's our object
			assertEquals(time, ((Date) result).getTime());
			assertTrue(result instanceof SpringProxy);
			assertTrue(result instanceof ImportedOsgiServiceProxy);
			assertTrue(result instanceof InfrastructureProxy);

			refAware = (ImportedOsgiServiceProxy) result;
			assertNotNull(refAware.getServiceReference());

			assertEquals("wrong target returned", date, ((InfrastructureProxy) result).getWrappedObject());
		}
		finally {
			if (reg != null)
				reg.unregister();
		}

		// test reference after the service went down
		assertNotNull(refAware.getServiceReference());
		assertNull(refAware.getServiceReference().getBundle());
	}

	public void testServiceReferenceProperties() throws Exception {
		long time = 1234;
		Date date = new Date(time);
		Dictionary dict = new Properties();
		dict.put("foo", "bar");
		dict.put("george", "michael");

		ServiceRegistration reg = publishService(date, dict);

		fb.setCardinality(Cardinality.C_1__1);
		fb.setFilter("(&(foo=bar)(george=michael))");
		fb.setInterfaces(new Class[] { Date.class });
		fb.afterPropertiesSet();

		try {
			Object result = fb.getObject();
			assertTrue(result instanceof Date);
			// check it's our object
			assertEquals(time, ((Date) result).getTime());

			ImportedOsgiServiceProxy refAware = (ImportedOsgiServiceProxy) result;

			assertTrue(doesMapContainsDictionary(dict,
				OsgiServiceReferenceUtils.getServicePropertiesAsMap(refAware.getServiceReference())));

			InfrastructureProxy targetAware = (InfrastructureProxy) result;
			assertEquals(date, targetAware.getWrappedObject());
		}
		finally {
			if (reg != null)
				reg.unregister();
		}
	}

	/**
	 * Check if the 'test' map contains the original Dictionary.
	 * 
	 * @param original
	 * @param test
	 * @return
	 */
	private boolean doesMapContainsDictionary(Dictionary original, Map test) {
		Enumeration enm = original.keys();
		while (enm.hasMoreElements()) {
			if (!test.containsKey(enm.nextElement()))
				return false;
		}

		return true;
	}

}
