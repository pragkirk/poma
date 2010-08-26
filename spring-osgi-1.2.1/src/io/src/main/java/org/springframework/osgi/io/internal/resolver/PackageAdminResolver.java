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

package org.springframework.osgi.io.internal.resolver;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.springframework.osgi.io.internal.OsgiHeaderUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * {@link PackageAdmin} based dependency resolver.
 * 
 * <p/> This implementation uses the OSGi PackageAdmin service to determine
 * dependencies between bundles. Since it's highly dependent on an external
 * service, it might be better to use a listener based implementation for poor
 * performing environments.
 * 
 * <p/> This implementation does consider required bundles.
 * 
 * @author Costin Leau
 * 
 */
public class PackageAdminResolver implements DependencyResolver {

	/** logger */
	private static final Log log = LogFactory.getLog(PackageAdminResolver.class);

	private final BundleContext bundleContext;


	public PackageAdminResolver(BundleContext bundleContext) {
		Assert.notNull(bundleContext);
		this.bundleContext = bundleContext;
	}

	public ImportedBundle[] getImportedBundles(Bundle bundle) {
		boolean trace = log.isTraceEnabled();

		PackageAdmin pa = getPackageAdmin();

		// create map with bundles as keys and a list of packages as value
		Map importedBundles = new LinkedHashMap(8);

		// 1. consider required bundles first

		// see if there are required bundle(s) defined
		String[] entries = OsgiHeaderUtils.getRequireBundle(bundle);

		// 1. if so, locate the bundles
		for (int i = 0; i < entries.length; i++) {
			String[] parsed = OsgiHeaderUtils.parseRequiredBundleString(entries[i]);
			// trim the strings just to be on the safe side (some implementations allows whitespaces, some don't)
			String symName = parsed[0].trim();
			String versionRange = parsed[1].trim();
			Bundle[] foundBundles = pa.getBundles(symName, versionRange);

			if (!ObjectUtils.isEmpty(foundBundles)) {
				Bundle requiredBundle = foundBundles[0];

				// find exported packages
				ExportedPackage[] exportedPackages = pa.getExportedPackages(requiredBundle);
				if (exportedPackages != null)
					addExportedPackages(importedBundles, requiredBundle, exportedPackages);
			}
			else {
				if (trace) {
					log.trace("Cannot find required bundle " + symName + "|" + versionRange);
				}
			}
		}

		// 2. determine imported bundles 
		// get all bundles
		Bundle[] bundles = bundleContext.getBundles();

		for (int i = 0; i < bundles.length; i++) {
			Bundle analyzedBundle = bundles[i];
			// if the bundle is already included (it's a required one), there's no need to look at it again
			if (!importedBundles.containsKey(analyzedBundle)) {
				ExportedPackage[] epa = pa.getExportedPackages(analyzedBundle);
				if (epa != null)
					for (int j = 0; j < epa.length; j++) {
						ExportedPackage exportedPackage = epa[j];
						Bundle[] importingBundles = exportedPackage.getImportingBundles();
						if (importingBundles != null)
							for (int k = 0; k < importingBundles.length; k++) {
								if (bundle.equals(importingBundles[k])) {
									addImportedBundle(importedBundles, exportedPackage);
								}
							}
					}
			}
		}

		List importedBundlesList = new ArrayList(importedBundles.size());

		for (Iterator iterator = importedBundles.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Bundle importedBundle = (Bundle) entry.getKey();
			List packages = (List) entry.getValue();
			importedBundlesList.add(new ImportedBundle(importedBundle,
				(String[]) packages.toArray(new String[packages.size()])));
		}

		return (ImportedBundle[]) importedBundlesList.toArray(new ImportedBundle[importedBundlesList.size()]);
	}

	/**
	 * Adds the imported bundle to the map of packages.
	 * 
	 * @param map
	 * @param bundle
	 * @param packageName
	 */
	private void addImportedBundle(Map map, ExportedPackage expPackage) {
		Bundle bnd = expPackage.getExportingBundle();
		List packages = (List) map.get(bnd);
		if (packages == null) {
			packages = new ArrayList(4);
			map.put(bnd, packages);
		}
		packages.add(new String(expPackage.getName()));
	}

	/**
	 * Adds the bundle exporting the given packages which are then imported by
	 * the owning bundle. This applies to special imports (such as
	 * Require-Bundle).
	 * 
	 * @param map
	 * @param bundle
	 * @param pkgs
	 */
	private void addExportedPackages(Map map, Bundle bundle, ExportedPackage[] pkgs) {
		List packages = (List) map.get(bundle);
		if (packages == null) {
			packages = new ArrayList(pkgs.length);
			map.put(bundle, packages);
		}
		for (int i = 0; i < pkgs.length; i++) {
			packages.add(pkgs[i].getName());
		}
	}

	private PackageAdmin getPackageAdmin() {

		return (PackageAdmin) AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				ServiceReference ref = bundleContext.getServiceReference(PackageAdmin.class.getName());
				if (ref == null)
					throw new IllegalStateException(PackageAdmin.class.getName() + " service is required");
				// don't do any proxying since PackageAdmin is normally a framework service
				// we can assume for now that it will always be available
				return (PackageAdmin) bundleContext.getService(ref);
			}
		});
	}
}