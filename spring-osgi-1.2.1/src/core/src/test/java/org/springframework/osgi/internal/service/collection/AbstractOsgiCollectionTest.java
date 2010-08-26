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

package org.springframework.osgi.internal.service.collection;

import java.util.Date;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.service.importer.support.internal.aop.ServiceProxyCreator;
import org.springframework.osgi.service.importer.support.internal.collection.OsgiServiceCollection;
import org.springframework.util.ClassUtils;

/**
 * Base class for Osgi service dynamic collection tests.
 * 
 * @author Costin Leau
 * 
 */
public abstract class AbstractOsgiCollectionTest extends TestCase {

	protected MockBundleContext context;

	protected Map services;

	protected OsgiServiceCollection col;


	public static interface Wrapper {

		Object execute();
	}

	public static class DateWrapper implements Wrapper, Comparable {

		private Date date;


		public DateWrapper(long time) {
			date = new Date(time);
		}

		public Object execute() {
			return new Long(date.getTime());
		}

		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (other instanceof DateWrapper) {
				DateWrapper oth = (DateWrapper) other;
				return (date.equals(oth.date));
			}
			return false;
		}

		public int hashCode() {
			return DateWrapper.class.hashCode() * 13 + date.hashCode();
		}

		public int compareTo(Object o) {
			Wrapper wr = (Wrapper) o;
			Long time = (Long) wr.execute();
			return new Long(date.getTime()).compareTo(time);
		}

	};


	protected void setUp() throws Exception {
		services = new LinkedHashMap();

		context = new MockBundleContext() {

			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[0];
			}

			public Object getService(ServiceReference reference) {
				Object service = services.get(reference);
				return (service == null ? new Object() : service);
			}
		};

		col = createCollection();
		col.setRequiredAtStartup(false);
		col.afterPropertiesSet();
	}

	abstract OsgiServiceCollection createCollection();

	protected ServiceProxyCreator createProxyCreator(Class[] classes) {
		return new SimpleServiceJDKProxyCreator(context, classes, getClass().getClassLoader());
	}

	protected void tearDown() throws Exception {
		services = null;
		context = null;
	}

	protected void addService(Object service, Dictionary properties) {

		ServiceReference ref = null;
		ServiceEvent event = null;

		Set intfs = ClassUtils.getAllInterfacesAsSet(service);
		String[] clazzez = new String[intfs.size()];

		int i = 0;
		for (Iterator iter = intfs.iterator(); iter.hasNext();) {
			clazzez[i++] = ((Class) iter.next()).getName();
		}

		ref = new MockServiceReference(null, properties, null, clazzez);

		event = new ServiceEvent(ServiceEvent.REGISTERED, ref);

		services.put(ref, service);

		for (Iterator iter = context.getServiceListeners().iterator(); iter.hasNext();) {
			ServiceListener listener = (ServiceListener) iter.next();
			listener.serviceChanged(event);
		}
	}

	protected void addService(Object service) {
		addService(service, new Properties());
	}

	protected void removeService(Object service) {
		ServiceReference ref = new MockServiceReference();

		for (Iterator iter = services.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			if (entry.getValue().equals(service)) {
				ref = (ServiceReference) entry.getKey();
				continue;
			}
		}

		services.remove(ref);

		ServiceEvent event = new ServiceEvent(ServiceEvent.UNREGISTERING, ref);

		for (Iterator iter = context.getServiceListeners().iterator(); iter.hasNext();) {
			ServiceListener listener = (ServiceListener) iter.next();
			listener.serviceChanged(event);
		}

	}
}
