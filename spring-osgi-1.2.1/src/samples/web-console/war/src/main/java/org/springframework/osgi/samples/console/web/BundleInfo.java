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

package org.springframework.osgi.samples.console.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.samples.console.service.BundleIdentifier;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * Simple POJO used for passing information to the view.
 * 
 * @author Costin Leau
 */
public class BundleInfo {

	/**
	 * Description POJO for an OSGi service reference. Extracts the relevant
	 * information in a suitable manner for expression languages (EL).
	 * 
	 * @author Costin Leau
	 */
	public static class OsgiService {

		private final Collection<String> usingBundles;
		private final Map<String, Object> properties;
		private final String bundle;


		/**
		 * Constructs a new <code>OsgiService</code> instance.
		 * 
		 * @param reference OSGi service reference
		 * @param displayOption bundle to String convertor
		 */
		public OsgiService(ServiceReference reference, BundleIdentifier displayOption) {
			Hashtable<String, Object> props = new Hashtable<String, Object>();
			for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) OsgiServiceReferenceUtils.getServicePropertiesSnapshotAsMap(
				reference).entrySet()) {
				props.put(entry.getKey(), ObjectUtils.nullSafeToString(entry.getValue()));
			}
			properties = Collections.unmodifiableMap(props);

			bundle = displayOption.toString(reference.getBundle());
			Collection<String> usingBundlesString = new ArrayList<String>();
			Bundle[] usingBndls = reference.getUsingBundles();
			if (usingBndls != null)
				for (Bundle usingBundle : usingBndls) {
					usingBundlesString.add(displayOption.toString(usingBundle));
				}
			usingBundles = Collections.unmodifiableCollection(usingBundlesString);
		}

		/**
		 * Returns the list of bundles using the OSGi service.
		 * 
		 * @return list of bundles (given as strings)
		 */
		public Collection<String> getUsingBundles() {
			return usingBundles;
		}

		/**
		 * Returns a map containing the OSGi service properties.
		 * 
		 * @return service properties map
		 */
		public Map<String, Object> getProperties() {
			return properties;
		}

		/**
		 * Returns the owning bundle (as String).
		 * 
		 * @return service owning bundle
		 */
		public String getBundle() {
			return bundle;
		}
	}


	private final Map<String, Object> properties = new LinkedHashMap<String, Object>();
	private final String state;
	private final Date lastModified;
	private final Collection<String> exportedPackages = new ArrayList<String>();
	private final Collection<String> importedPackages = new ArrayList<String>();
	private final Collection<OsgiService> registeredServices = new ArrayList<OsgiService>();
	private final Collection<OsgiService> servicesInUse = new ArrayList<OsgiService>();
	private final Bundle bundle;


	/**
	 * Constructs a new <code>BundleInfo</code> instance.
	 * 
	 * @param bundle OSGi bundle
	 */
	public BundleInfo(Bundle bundle) {
		this.bundle = bundle;
		// initialize properties
		Dictionary headers = bundle.getHeaders();
		addKeyValueForHeader(Constants.BUNDLE_ACTIVATOR, headers);
		addKeyValueForHeader(Constants.BUNDLE_CLASSPATH, headers);
		addKeyValueForHeader(Constants.BUNDLE_NAME, headers);
		addKeyValueForHeader(Constants.BUNDLE_SYMBOLICNAME, headers);
		properties.put(Constants.BUNDLE_VERSION, OsgiBundleUtils.getBundleVersion(bundle));
		this.state = OsgiStringUtils.bundleStateAsString(bundle);
		this.lastModified = new Date(bundle.getLastModified());

	}

	private void addKeyValueForHeader(String headerName, Dictionary headers) {
		properties.put(headerName, headers.get(headerName));
	}

	/**
	 * Returns bundle properties.
	 * 
	 * @return bundle properties
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	void addProperty(String name, Object value) {
		properties.put(name, (value == null ? "" : value));
	}

	/**
	 * Returns the backing bundle.
	 * 
	 * @return backing bundle
	 */
	public Bundle getBundle() {
		return bundle;
	}

	/**
	 * Returns the backing bundle location.
	 * 
	 * @return backing bundle location
	 */
	public String getLocation() {
		return bundle.getLocation();
	}

	/**
	 * Returns the bundle state (as a String).
	 * 
	 * @return backing bundle state
	 */
	public String getState() {
		return state;
	}

	/**
	 * Returns the backing bundle last modified date.
	 * 
	 * @return backing bundle last modified date
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * Returns a collection of packages exported by the backing bundle.
	 * 
	 * @return collection of exported packages
	 */
	public Collection<String> getExportedPackages() {
		return exportedPackages;
	}

	void addExportedPackages(String... exportedPackage) {
		CollectionUtils.mergeArrayIntoCollection(exportedPackage, exportedPackages);
	}

	/**
	 * Returns a collection of packages imported by the backing bundle.
	 * 
	 * @return collectio of imported packages
	 */
	public Collection<String> getImportedPackages() {
		return importedPackages;
	}

	void addImportedPackages(String... importedPackage) {
		CollectionUtils.mergeArrayIntoCollection(importedPackage, importedPackages);
	}

	/**
	 * Returns a collection of services registered by the backing bundle.
	 * 
	 * @return collection of registered services
	 */
	public Collection<OsgiService> getRegisteredServices() {
		return registeredServices;
	}

	void addRegisteredServices(OsgiService registeredService) {
		this.registeredServices.add(registeredService);
	}

	/**
	 * Returns a collection of services used by the backing bundle.
	 * 
	 * @return collection of services in use
	 */
	public Collection<OsgiService> getServicesInUse() {
		return servicesInUse;
	}

	void addServiceInUse(OsgiService serviceInUse) {
		this.servicesInUse.add(serviceInUse);
	}
}
