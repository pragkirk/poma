/*
 * Copyright 2006-2009 the original author or authors.
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
package org.springframework.osgi.extender.internal.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

/**
 * Internal utility used for internal purposes.
 * 
 * @author Costin Leau
 */
public abstract class BundleUtils {
	public static final String DM_CORE_ID = "spring.osgi.core.bundle.id";
	public static final String DM_CORE_TS = "spring.osgi.core.bundle.timestamp";

	public static Bundle getDMCoreBundle(BundleContext ctx) {
		ServiceReference ref = ctx.getServiceReference(PackageAdmin.class.getName());
		if (ref != null) {
			Object service = ctx.getService(ref);
			if (service instanceof PackageAdmin) {
				PackageAdmin pa = (PackageAdmin) service;
				if (pa != null) {
					return pa.getBundle(OsgiBundleXmlApplicationContext.class);
				}
			}
		}
		return null;
	}

	public static String createNamespaceFilter(BundleContext ctx) {
		Bundle bnd = getDMCoreBundle(ctx);
		if (bnd != null) {
			return "(|(" + DM_CORE_ID + "=" + bnd.getBundleId() + ")(" + DM_CORE_TS + "=" + bnd.getLastModified()
					+ "))";
		}
		return "";
	}
}
