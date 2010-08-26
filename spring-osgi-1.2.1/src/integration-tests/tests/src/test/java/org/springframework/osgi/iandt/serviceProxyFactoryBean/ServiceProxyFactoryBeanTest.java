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

import java.io.Serializable;
import java.util.Date;

import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.service.importer.support.Cardinality;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
import org.springframework.osgi.util.OsgiFilterUtils;

/**
 * @author Costin Leau
 * 
 */
public class ServiceProxyFactoryBeanTest extends ServiceBaseTest {

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

	public void testFactoryBeanForOneServiceAsClass() throws Exception {
		long time = 1234;
		Date date = new Date(time);
		ServiceRegistration reg = publishService(date);

		fb.setCardinality(Cardinality.C_1__1);
		fb.setInterfaces(new Class[] { Date.class });
		fb.afterPropertiesSet();

		try {
			Object result = fb.getObject();
			assertTrue(result instanceof Date);
			assertEquals(time, ((Date) result).getTime());
		}
		finally {
			if (reg != null)
				reg.unregister();
		}
	}

	public void testFactoryBeanForOneServiceAsInterface() throws Exception {
		long time = 1234;
		Date date = new Date(time);

		Class[] intfs = new Class[] { Comparable.class, Serializable.class, Cloneable.class };

		String[] classes = new String[] { Comparable.class.getName(), Serializable.class.getName(),
			Cloneable.class.getName(), Date.class.getName() };

		ServiceRegistration reg = publishService(date, classes);

		fb.setCardinality(Cardinality.C_1__1);
		fb.setInterfaces(intfs);
		fb.setFilter(OsgiFilterUtils.unifyFilter(Date.class, null));
		fb.afterPropertiesSet();

		try {
			Object result = fb.getObject();
			// the interfaces are implemented
			assertTrue(result instanceof Comparable);
			assertTrue(result instanceof Serializable);
			assertTrue(result instanceof Cloneable);
			// but not the class
			assertFalse(result instanceof Date);
			// compare the strings
			assertEquals(result.toString(), date.toString());
		}
		finally {
			if (reg != null)
				reg.unregister();
		}
	}

}
