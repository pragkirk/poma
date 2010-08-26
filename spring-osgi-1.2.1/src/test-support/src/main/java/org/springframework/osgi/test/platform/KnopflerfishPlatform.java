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

package org.springframework.osgi.test.platform;

import java.io.File;
import java.util.Properties;

import org.knopflerfish.framework.Framework;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.test.internal.util.IOUtils;

/**
 * Knopflerfish 2.0.4+ Platform.
 * 
 * @author Costin Leau
 */
public class KnopflerfishPlatform extends AbstractOsgiPlatform {

	private BundleContext context;

	private Framework framework;

	private File kfStorageDir;


	public KnopflerfishPlatform() {
		toString = "Knopflerfish OSGi Platform";
	}

	Properties getPlatformProperties() {
		if (kfStorageDir == null) {
			kfStorageDir = createTempDir("kf");
			kfStorageDir.deleteOnExit();
			if (log.isDebugEnabled())
				log.debug("KF temporary storage dir is " + kfStorageDir.getAbsolutePath());

		}

		// default properties
		Properties props = new Properties();
		props.setProperty("org.osgi.framework.dir", kfStorageDir.getAbsolutePath());
		props.setProperty("org.knopflerfish.framework.bundlestorage", "file");
		props.setProperty("org.knopflerfish.framework.bundlestorage.file.reference", "true");
		props.setProperty("org.knopflerfish.framework.bundlestorage.file.unpack", "false");
		props.setProperty("org.knopflerfish.startlevel.use", "true");
		props.setProperty("org.knopflerfish.osgi.setcontextclassloader", "true");
		// embedded mode
		props.setProperty("org.knopflerfish.framework.exitonshutdown", "false");
		// disable patch CL
		props.setProperty("org.knopflerfish.framework.patch", "false");
		// new in KF 2.0.4 - automatically exports system packages based on the JRE version
		props.setProperty("org.knopflerfish.framework.system.export.all", "true");

		// add strict bootpath delegation (introduced in KF 2.3.0)
		// since otherwise classes will be loaded from the booth classpath
		// when generating JDK proxies instead of the OSGi space
		// since KF thinks that a non-OSGi class is making the call.
		props.setProperty("org.knopflerfish.framework.strictbootclassloading", "true");

		return props;
	}

	public BundleContext getBundleContext() {
		return context;
	}

	public void start() throws Exception {
		if (framework == null) {
			// copy configuration properties to sys properties
			System.getProperties().putAll(getConfigurationProperties());

			framework = new Framework(this);
			framework.launch(0);
			context = framework.getSystemBundleContext();
		}
	}

	public void stop() throws Exception {
		if (framework != null) {
			context = null;
			try {
				framework.shutdown();
			}
			finally {
				framework = null;
				IOUtils.delete(kfStorageDir);
			}
		}
	}
}