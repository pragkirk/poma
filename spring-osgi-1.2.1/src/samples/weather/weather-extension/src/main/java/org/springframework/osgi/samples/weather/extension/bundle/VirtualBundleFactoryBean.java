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
 *
 */
package org.springframework.osgi.samples.weather.extension.bundle;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.util.Assert;

/**
 * BundleFactoryBean that creates bundles on the fly from regular jar files or
 * maven project jars. Version and identifying information is injected
 * dynamically as the jar is loaded. Bundle exports are synthesized based on the
 * packages incorporated in the jar file. Jars are by default loaded from the
 * local maven repository, but a remote repository can also be specified using
 * the bundleURL property.
 * 
 * @author Andy Piper
 */
// FIXME andyp -- this class is looking a lot like Project, maybe the two should
// be merged
public class VirtualBundleFactoryBean implements InitializingBean, FactoryBean, BundleContextAware {
	private String artifactId;

	private String groupId = "org.example.group";

	private String version = "1.0";

	private Set/* <String> */exports = Collections.EMPTY_SET;

	private Set/* <String> */imports = Collections.EMPTY_SET;

	private Resource location;

	private BundleContext bundleContext;

	private Bundle bundle;

	protected Bundle loadBundle() throws Exception {
		URL url = null;
		Project project;
		if (location == null) {
			url = new URL("file", "", getLocalRepository());
			project = new Project(groupId, artifactId, version, "jar", Collections.EMPTY_SET, exports, imports);
			// System.out.println("Repository is: " + url.toString());
		}
		else {
			url = location.getURL();
			project = new Project(groupId, artifactId, version, "jar", url.toString(), Collections.EMPTY_SET, exports,
					imports);
		}
		return new MavenBundleManager(bundleContext, url).installBundle(project);

	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(artifactId, "artifactId not supplied");
		bundle = loadBundle();
	}

	public Bundle getBundle() {
		return bundle;
	}

	public Object getObject() {
		return getBundle();
	}

	public Class getObjectType() {
		return (bundle == null ? Bundle.class : bundle.getClass());
	}

	public boolean isSingleton() {
		return true;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Set/* <String> */getExports() {
		return exports;
	}

	public void setExports(Set/* <String> */exports) {
		if (exports == null)
			this.exports = Collections.EMPTY_SET;
		else
			this.exports = exports;
	}

	public Set/* <String> */getImports() {
		return imports;
	}

	public void setImports(Set/* <String> */imports) {
		if (imports == null)
			this.imports = Collections.EMPTY_SET;
		else
			this.imports = imports;
	}

	private String getLocalRepository() {
		return new File(System.getProperty("user.home"), ".m2/repository").getAbsolutePath();
	}

	/**
	 * Returns the location.
	 * 
	 * @return Returns the location
	 */
	public Resource getLocation() {
		return location;
	}

	/**
	 * @param location The location to set.
	 */
	public void setLocation(Resource location) {
		this.location = location;
	}

	/**
	 * @param bundleContext The bundleContext to set.
	 */
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
}
