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

package org.springframework.osgi.web.extender.internal.scanner;

import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.springframework.osgi.util.OsgiStringUtils;

/**
 * Scanner for Web application ARchives (WARs). This implementation considers as
 * <tt>WAR</tt>s bundles that match the following rules (in this order):
 * <ol>
 * <li>have a <tt>.war</tt> extension</li>
 * <li>contain the <tt>/WEB-INF/</tt> folder</li>
 * </ol>
 * 
 * @author Costin Leau
 */
public class DefaultWarScanner implements WarScanner {

	/** war extension */
	private static final String WAR_EXT = ".war";
	private static final String SLASH = "/";
	private static final String WEB_INF = "WEB-INF";

	/** logger */
	private static final Log log = LogFactory.getLog(DefaultWarScanner.class);


	public boolean isWar(Bundle bundle) {
		boolean trace = log.isTraceEnabled();
		boolean isWar = false;

		if (trace)
			log.trace("Scanning bundle " + OsgiStringUtils.nullSafeSymbolicName(bundle));

		if (bundle == null)
			return false;

		// check bundle extension
		String location = bundle.getLocation();

		if (location != null) {
			// handle unpacked bundles
			if (location.endsWith(SLASH)) {
				// remove trailing slash
				location = location.substring(0, location.length() - 1);
			}
			if (trace)
				log.trace("Scanning for war bundle location " + location);
			isWar = location.endsWith(WAR_EXT);
			if (isWar) {
				if (trace)
					log.trace("Location [" + location + "] has " + WAR_EXT + " extension; considering the bundle a WAR");
				return true;
			}
			else {
				if (trace)
					log.trace("Location [" + location + "] doesn't indicate a WAR; scanning the presence of /"
							+ WEB_INF + "/");
			}
		}
		return isWebInfPresent(bundle);
	}

	/**
	 * Checks if <tt>/WEB-INF/</tt> is available in the bundle.
	 * 
	 * @param bundle OSGi bundle
	 * @return true if the folder is contained, false otherwise.
	 */
	private boolean isWebInfPresent(Bundle bundle) {
		if (!hasEntry(bundle, SLASH, WEB_INF)) {
			// since folders might not be stored inside the jar, try using a different approach
			return hasEntry(bundle, WEB_INF, null);
		}
		return true;
	}

	private boolean hasEntry(Bundle bundle, String path, String pattern) {
		Enumeration enm = bundle.findEntries(path, pattern, false);
		return (enm != null && enm.hasMoreElements());
	}
}
