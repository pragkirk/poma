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
package org.springframework.osgi.iandt.importer;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.util.OsgiServiceUtils;

/**
 * @author Costin Leau
 * 
 */
public class ServiceComparatorTest extends BaseIntegrationTest {

	private ServiceRegistration registration1, registration2, reg3;

	private Object service1, service2, service3;

	/** load context on each test run */
	private ConfigurableOsgiBundleApplicationContext context;

	public static interface MyInterface extends Comparable, Serializable, Cloneable {
		String value();
	}

	static class MyClass implements MyInterface {

		private String value;

		protected Object clone() throws CloneNotSupportedException {
			return null;
		}

		public int compareTo(Object o) {
			MyInterface other = (MyInterface) o;
			return value.compareTo(other.value());
		}

		public String value() {
			return value;
		}

		MyClass(String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}

		public boolean equals(Object obj) {
			if (obj instanceof MyInterface)
				return this.compareTo(obj) == 0;
			return false;
		}

	}

	protected void onSetUp() throws Exception {
		// publish service
		String[] intfs = new String[] { MyInterface.class.getName() };

		service1 = new MyClass("abc");
		service2 = new MyClass("xyz");
		service3 = new MyClass("mnp");

		// register the services
		registration1 = bundleContext.registerService(intfs, service1, null);
		registration2 = bundleContext.registerService(intfs, service2, null);
		reg3 = bundleContext.registerService(intfs, service3, null);


		context = loadAppContext();
	}

	protected void onTearDown() throws Exception {
		context.close();
		OsgiServiceUtils.unregisterService(registration1);
		OsgiServiceUtils.unregisterService(registration2);
		OsgiServiceUtils.unregisterService(reg3);
	}

	private ConfigurableOsgiBundleApplicationContext loadAppContext() {
		OsgiBundleXmlApplicationContext appContext = new OsgiBundleXmlApplicationContext(
				new String[] { "/org/springframework/osgi/iandt/importer/importer-ordering.xml" });
		appContext.setBundleContext(bundleContext);
		appContext.refresh();
		return appContext;
	}

	public void testComparableImportedObjects() throws Exception {
		assertNotNull(context);
		assertTrue("service 1 is greater then service2", ((Comparable) service1).compareTo(service2) < 0);

		Set set = (Set) context.getBean("setWithServiceOrder");
		assertEquals(3, set.size());
		Iterator iter = set.iterator();

		System.out.println(set);
		assertEquals(service1, iter.next());
		assertEquals(service3, iter.next());
		assertEquals(service2, iter.next());
	}

	public void testServiceReferenceOrderingOnImportedObjects() throws Exception {
		Set set = (Set) context.getBean("setWithServiceReference");
		assertNotNull(set);
		assertEquals(3, set.size());
		Iterator iter = set.iterator();

		assertEquals(service3, iter.next());
		assertEquals(service2, iter.next());
		assertEquals(service1, iter.next());
	}
}
