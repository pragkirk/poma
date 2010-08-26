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

package org.springframework.osgi.samples.console.service.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.osgi.io.OsgiBundleResourcePatternResolver;
import org.springframework.osgi.samples.console.service.OsgiConsole;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.stereotype.Service;

/**
 * Default OSGi console implementation.
 * 
 * @author Costin Leau
 * 
 */
@Service
public class DefaultOsgiConsole implements OsgiConsole {

	@Autowired
	private BundleContext bundleContext;
	@Autowired
	private PackageAdmin pa;


	public long getDefaultBundleId() {
		return bundleContext.getBundle().getBundleId();
	}

	public Bundle[] listBundles() {
		return bundleContext.getBundles();
	}

	public Bundle getBundle(long bundleId) {
		return bundleContext.getBundle(bundleId);
	}

	public String[] getExportedPackages(Bundle bundle) {
		ExportedPackage[] pkgs = pa.getExportedPackages(bundle);
		if (pkgs == null)
			return new String[0];

		List<String> packages = new ArrayList<String>(pkgs.length);
		for (ExportedPackage exportedPackage : pkgs) {
			packages.add(exportedPackage.getName() + ";version=" + exportedPackage.getVersion());
		}

		return (String[]) packages.toArray(new String[packages.size()]);
	}

	public String[] getImportedPackages(Bundle bundle) {
		Set<String> importedPackages = new TreeSet<String>();
		Set<Bundle> seenBundles = new HashSet<Bundle>();

		Bundle[] bundles = bundleContext.getBundles();

		for (int i = 0; i < bundles.length; i++) {
			Bundle analyzedBundle = bundles[i];
			if (!seenBundles.contains(analyzedBundle)) {
				seenBundles.add(bundle);
				ExportedPackage[] epa = pa.getExportedPackages(analyzedBundle);
				if (epa != null)
					for (int j = 0; j < epa.length; j++) {
						ExportedPackage exportedPackage = epa[j];
						Bundle[] importingBundles = exportedPackage.getImportingBundles();
						if (importingBundles != null)
							for (int k = 0; k < importingBundles.length; k++) {
								if (bundle.equals(importingBundles[k])) {
									importedPackages.add(exportedPackage.getName() + ";version="
											+ exportedPackage.getVersion());
								}
							}
					}
			}
		}

		return (String[]) importedPackages.toArray(new String[importedPackages.size()]);
	}

	public ServiceReference[] getRegisteredServices(Bundle bundle) {
		ServiceReference[] services = bundle.getRegisteredServices();
		return (services == null ? new ServiceReference[0] : services);
	}

	public ServiceReference[] getServicesInUse(Bundle bundle) {
		ServiceReference[] services = bundle.getServicesInUse();
		return (services == null ? new ServiceReference[0] : services);
	}

	public Collection<String> search(Bundle bundle, String pattern) {
		OsgiBundleResourcePatternResolver patternResolver = new OsgiBundleResourcePatternResolver(bundle);
		Collection<String> result = new ArrayList<String>();
		try {
			for (Resource resource : patternResolver.getResources(pattern)) {
				result.add(resource.getURI().toString());
			}
		}
		catch (IOException ex) {
			// invalid pattern
			return null;
		}

		return result;
	}
}
