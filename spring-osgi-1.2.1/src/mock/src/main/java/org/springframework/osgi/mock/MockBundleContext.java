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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * BundleContext mock.
 * 
 * <p/>
 * Can be configured to use a predefined Bundle or/and configuration. By
 * default, will create an internal MockBundle. Most of the operations are no-op
 * (as anonymous classes with specific functionality can be created per use
 * basis).
 * 
 * @author Costin Leau
 * 
 */
public class MockBundleContext implements BundleContext {

	public static final Properties DEFAULT_PROPERTIES = new DefaultBundleContextProperties();

	private Bundle bundle;

	private Properties properties;

	protected Set serviceListeners, bundleListeners;


	/**
	 * Constructs a new <code>MockBundleContext</code> instance. The associated
	 * bundle will be created automatically.
	 */
	public MockBundleContext() {
		this(null, null);
	}

	/**
	 * Constructs a new <code>MockBundleContext</code> instance.
	 * 
	 * @param bundle associated bundle
	 */
	public MockBundleContext(Bundle bundle) {
		this(bundle, null);
	}

	/**
	 * Constructs a new <code>MockBundleContext</code> instance allowing both
	 * the bundle and the context properties to be specified.
	 * 
	 * @param bundle associated bundle
	 * @param props context properties
	 */
	public MockBundleContext(Bundle bundle, Properties props) {
		this.bundle = (bundle == null ? new MockBundle(this) : bundle);
		properties = new Properties(DEFAULT_PROPERTIES);
		if (props != null)
			properties.putAll(props);

		// make sure the order is preserved
		this.serviceListeners = new LinkedHashSet(2);
		this.bundleListeners = new LinkedHashSet(2);
	}

	public void addBundleListener(BundleListener listener) {
		bundleListeners.add(listener);
	}

	public void addFrameworkListener(FrameworkListener listener) {
	}

	public void addServiceListener(ServiceListener listener) {
		try {
			addServiceListener(listener, null);
		}
		catch (InvalidSyntaxException ex) {
			throw new IllegalStateException("exception should not occur");
		}
	}

	public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
		if (listener == null)
			throw new IllegalArgumentException("non-null listener required");
		this.serviceListeners.add(listener);
	}

	public Filter createFilter(String filter) throws InvalidSyntaxException {
		return new MockFilter(filter);
	}

	public ServiceReference[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
		return new ServiceReference[] {};
	}

	public Bundle getBundle() {
		return bundle;
	}

	public Bundle getBundle(long id) {
		return bundle;
	}

	public Bundle[] getBundles() {
		return new Bundle[] { bundle };
	}

	public File getDataFile(String filename) {
		return null;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public Object getService(ServiceReference reference) {
		return new Object();
	}

	public ServiceReference getServiceReference(String clazz) {
		return new MockServiceReference(getBundle(), new String[] { clazz });
	}

	public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
		// Some jiggery-pokery to get round the fact that we don't ever use the
		// clazz
		if (clazz == null)
			if (filter != null) {
				// flatten filter since the constants might be case insensitive
				String flattenFilter = filter.toLowerCase();
				int i = flattenFilter.indexOf(Constants.OBJECTCLASS.toLowerCase() + "=");
				if (i > 0) {
					clazz = filter.substring(i + Constants.OBJECTCLASS.length() + 1);
					clazz = clazz.substring(0, clazz.indexOf(")"));
				}
			}
			else
				clazz = Object.class.getName();
		return new ServiceReference[] { new MockServiceReference(getBundle(), new String[] { clazz }) };
	}

	public Bundle installBundle(String location) throws BundleException {
		MockBundle bundle = new MockBundle();
		bundle.setLocation(location);
		return bundle;
	}

	public Bundle installBundle(String location, InputStream input) throws BundleException {
		try {
			input.close();
		}
		catch (IOException ex) {
			throw new BundleException("cannot close stream", ex);
		}
		return installBundle(location);
	}

	public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
		MockServiceRegistration reg = new MockServiceRegistration(properties);

		// disabled for now
		// MockServiceReference ref = new MockServiceReference(this.bundle,
		// properties, reg, clazzes);
		// ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, ref);
		//
		// for (Iterator iter = serviceListeners.iterator(); iter.hasNext();) {
		// ServiceListener listener = (ServiceListener) iter.next();
		// listener.serviceChanged(event);
		// }

		return reg;
	}

	public ServiceRegistration registerService(String clazz, Object service, Dictionary properties) {
		return registerService(new String[] { clazz }, service, properties);
	}

	public void removeBundleListener(BundleListener listener) {
		bundleListeners.remove(listener);
	}

	public void removeFrameworkListener(FrameworkListener listener) {
	}

	public void removeServiceListener(ServiceListener listener) {
		serviceListeners.remove(listener);
	}

	public boolean ungetService(ServiceReference reference) {
		return false;
	}

	/**
	 * Sets the bundle associated with this context.
	 * 
	 * @param bundle associated bundle
	 */
	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}

	// hooks
	/**
	 * Returns a set of registered service listeners. Handy method when mocking
	 * with listeners is required.
	 * 
	 * @return set of registered service listeners
	 */
	public Set getServiceListeners() {
		return serviceListeners;
	}

	/**
	 * Returns a set of registered bundle listeners.
	 * 
	 * @return set of registered bundle listeners
	 */
	public Set getBundleListeners() {
		return bundleListeners;
	}
}