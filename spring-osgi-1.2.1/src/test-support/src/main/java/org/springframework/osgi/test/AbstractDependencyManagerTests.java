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

package org.springframework.osgi.test;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.springframework.core.JdkVersion;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.osgi.test.internal.util.PropertiesUtil;
import org.springframework.osgi.test.provisioning.ArtifactLocator;
import org.springframework.osgi.test.provisioning.internal.LocalFileSystemMavenRepository;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Dependency manager class - deals with locating of various artifacts required
 * by the OSGi test. The artifacts are considered to be OSGi bundles that will
 * be installed during the OSGi platform startup. Additionally this class
 * installs the testing framework required bundles (such as Spring, Spring-DM).
 * 
 * <p/>This implementation uses internally an {@link ArtifactLocator} to
 * retrieve the required dependencies for the running test. By default, the
 * artifact locator uses the local maven 2 repository. Maven configurations
 * (such as &lt;settings.xml&gt;) are supported. Alternatively for Maven
 * repositories located in non-default locations, one can use the
 * <code>localRepository</code> system property to specify the folder URL.
 * 
 * @author Costin Leau
 * 
 */
public abstract class AbstractDependencyManagerTests extends AbstractSynchronizedOsgiTests {

	private static final String TEST_FRRAMEWORK_BUNDLES_CONF_FILE = "/org/springframework/osgi/test/internal/boot-bundles.properties";

	private static final String IGNORE = "ignore";

	/**
	 * Artifact locator (by default the Local Maven repository).
	 */
	private ArtifactLocator locator = new LocalFileSystemMavenRepository();


	/**
	 * 
	 * Default constructor. Constructs a new
	 * <code>AbstractDependencyManagerTests</code> instance.
	 * 
	 */
	public AbstractDependencyManagerTests() {
		super();
	}

	/**
	 * 
	 * Constructs a new <code>AbstractDependencyManagerTests</code> instance.
	 * 
	 * @param name test name
	 */
	public AbstractDependencyManagerTests(String name) {
		super(name);
	}


	private static final String SPRING_OSGI_VERSION_PROP_KEY = "ignore.spring.osgi.version";

	private static final String SPRING_VERSION_PROP_KEY = "ignore.spring.version";

	/** uninitialised - read from the properties file */
	private String springOsgiVersion = null;

	/** uninitialised - read from the properties file */
	private String springBundledVersion = null;


	/**
	 * Returns the version of the Spring-DM bundles installed by the testing
	 * framework.
	 * 
	 * @return Spring-DM bundles version
	 */
	protected String getSpringDMVersion() {
		if (springOsgiVersion == null) {
			springOsgiVersion = readProperty(SPRING_OSGI_VERSION_PROP_KEY);
		}

		return springOsgiVersion;
	}

	/**
	 * Returns the version of the Spring bundles installed by the testing
	 * framework.
	 * 
	 * @return Spring framework dependency version
	 */
	protected String getSpringVersion() {
		if (springBundledVersion == null) {
			springBundledVersion = readProperty(SPRING_VERSION_PROP_KEY);
		}
		return springBundledVersion;
	}

	private String readProperty(final String name) {
		if (System.getSecurityManager() != null) {
			return (String) AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					return System.getProperty(name);
				}
			});
		}
		else
			return System.getProperty(name);
	}

	/**
	 * Returns the bundles that have to be installed as part of the test setup.
	 * This method provides an alternative to {@link #getTestBundles()} as it
	 * allows subclasses to specify just the bundle name w/o worrying about
	 * locating the artifact (which is resolved through the
	 * {@link ArtifactLocator}).
	 * 
	 * <p/>A bundle name can have any value and depends on the format expected
	 * by the {@link ArtifactLocator} implementation. By default, a CSV (Comma
	 * Separated Values) format is expected.
	 * 
	 * <p/>This method allows a declarative approach in declaring bundles as
	 * opposed to {@link #getTestBundles()} which provides a programmatic one.
	 * 
	 * @return an array of testing framework bundle identifiers
	 * @see #locateBundle(String)
	 */
	protected String[] getTestBundlesNames() {
		return new String[0];
	}

	/**
	 * Returns the bundles that have to be installed as part of the test setup.
	 * This method is preferred as the bundles are by their names rather then as
	 * {@link Resource}s. It allows for a <em>declarative</em> approach for
	 * specifying bundles as opposed to {@link #getTestBundles()} which provides
	 * a programmatic one.
	 * 
	 * <p/>This implementation reads a predefined properties file to determine
	 * the bundles needed. If the configuration needs to be changed, consider
	 * changing the configuration location.
	 * 
	 * @return an array of testing framework bundle identifiers
	 * @see #getTestingFrameworkBundlesConfiguration()
	 * @see #locateBundle(String)
	 * 
	 */
	protected String[] getTestFrameworkBundlesNames() {
		// load properties file
		Properties props = PropertiesUtil.loadAndExpand(getTestingFrameworkBundlesConfiguration());

		if (props == null)
			throw new IllegalArgumentException("cannot load default configuration from "
					+ getTestingFrameworkBundlesConfiguration());

		boolean trace = logger.isTraceEnabled();

		if (trace)
			logger.trace("Loaded properties " + props);

		// pass properties to test instance running inside OSGi space
		System.getProperties().put(SPRING_OSGI_VERSION_PROP_KEY, props.get(SPRING_OSGI_VERSION_PROP_KEY));
		System.getProperties().put(SPRING_VERSION_PROP_KEY, props.get(SPRING_VERSION_PROP_KEY));

		Properties excluded = PropertiesUtil.filterKeysStartingWith(props, IGNORE);

		if (trace) {
			logger.trace("Excluded ignored properties " + excluded);
		}

		// filter bundles which are Tiger/JDK 1.5 specific
		String sign = null;
		if (JdkVersion.isAtLeastJava15()) {
			sign = "-15";
		}
		else {
			sign = "+15";
		}

		excluded = PropertiesUtil.filterValuesStartingWith(props, sign);
		if (trace)
			logger.trace("JDK " + JdkVersion.getJavaVersion() + " excluded bundles " + excluded);

		String[] bundles = (String[]) props.keySet().toArray(new String[props.size()]);
		// sort the array (as the Properties file doesn't respect the order)
		bundles = StringUtils.sortStringArray(bundles);

		if (logger.isDebugEnabled())
			logger.debug("Default framework bundles :" + ObjectUtils.nullSafeToString(bundles));

		return bundles;
	}

	/**
	 * Returns the location of the test framework bundles configuration.
	 * 
	 * @return the location of the test framework bundles configuration
	 */
	protected Resource getTestingFrameworkBundlesConfiguration() {
		return new InputStreamResource(
			AbstractDependencyManagerTests.class.getResourceAsStream(TEST_FRRAMEWORK_BUNDLES_CONF_FILE));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p/>Default implementation that uses the {@link ArtifactLocator} to
	 * resolve the bundles specified in {@link #getTestBundlesNames()}.
	 * 
	 * Subclasses that override this method should decide whether they want to
	 * support {@link #getTestBundlesNames()} or not.
	 * 
	 * @see org.springframework.osgi.test.AbstractOsgiTests#getTestBundles()
	 */
	protected Resource[] getTestBundles() {
		return locateBundles(getTestBundlesNames());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p/> Default implementation that uses
	 * {@link #getTestFrameworkBundlesNames()} to discover the bundles part of
	 * the testing framework.
	 * 
	 * @see org.springframework.osgi.test.AbstractOsgiTests#getTestFrameworkBundles()
	 */
	protected Resource[] getTestFrameworkBundles() {
		return locateBundles(getTestFrameworkBundlesNames());
	}

	/**
	 * Locates the given bundle identifiers. Will delegate to
	 * {@link #locateBundle(String)}.
	 * 
	 * @param bundles bundle identifiers
	 * @return an array of Spring resources for the given bundle indentifiers
	 */
	protected Resource[] locateBundles(String[] bundles) {
		if (bundles == null)
			bundles = new String[0];

		Resource[] res = new Resource[bundles.length];
		for (int i = 0; i < bundles.length; i++) {
			res[i] = locateBundle(bundles[i]);
		}
		return res;
	}

	// Set log4j property to avoid TCCL problems during startup
	/**
	 * {@inheritDoc}
	 * 
	 * <p/>Sets specific log4j property to avoid class loading problems during
	 * start up related to the thread context class loader.
	 */
	protected void preProcessBundleContext(BundleContext platformBundleContext) throws Exception {
		AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				System.setProperty("log4j.ignoreTCL", "true");
				return null;
			}
		});

		super.preProcessBundleContext(platformBundleContext);
	}

	/**
	 * Locates (through the {@link ArtifactLocator}) an OSGi bundle given as a
	 * String.
	 * 
	 * The default implementation expects the argument to be in Comma Separated
	 * Values (CSV) format which indicates an artifact group, id, version and
	 * optionally the type.
	 * 
	 * @param bundleId the bundle identifier in CSV format
	 * @return a resource pointing to the artifact location
	 */
	protected Resource locateBundle(String bundleId) {
		Assert.hasText(bundleId, "bundleId should not be empty");

		// parse the String
		String[] artifactId = StringUtils.commaDelimitedListToStringArray(bundleId);

		Assert.isTrue(artifactId.length >= 3, "the CSV string " + bundleId + " contains too few values");
		// TODO: add a smarter mechanism which can handle 1 or 2 values CSVs
		for (int i = 0; i < artifactId.length; i++) {
			artifactId[i] = StringUtils.trimWhitespace(artifactId[i]);
		}

		ArtifactLocator aLocator = getLocator();

		return (artifactId.length == 3 ? aLocator.locateArtifact(artifactId[0], artifactId[1], artifactId[2])
				: aLocator.locateArtifact(artifactId[0], artifactId[1], artifactId[2], artifactId[3]));
	}

	/**
	 * Returns the ArtifactLocator used by this test suite. Subclasses should
	 * override this method if the default locator (searching the local
	 * projects, falling back to the Maven2 repository) is not enough.
	 * 
	 * <p>
	 * <b>Note</b>: This method will be used each time a bundle has to be
	 * retrieved; it is highly recommended to return a cached instance instead
	 * of a new one each time.
	 * 
	 * @return artifact locator used by this test.
	 */
	protected ArtifactLocator getLocator() {
		return locator;
	}
}
