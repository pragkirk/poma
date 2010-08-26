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

package org.springframework.osgi.test.provisioning.internal;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.DefaultDocumentLoader;
import org.springframework.beans.factory.xml.DocumentLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.osgi.test.provisioning.ArtifactLocator;
import org.springframework.util.xml.DomUtils;
import org.springframework.util.xml.XmlValidationModeDetector;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Locator for artifacts found in the local maven repository. Does <strong>not</strong>
 * use Maven libraries, it rather uses the maven patterns and conventions to
 * identify the artifacts.
 * 
 * @author Hal Hidlebrand
 * @author Costin Leau
 * 
 */
public class LocalFileSystemMavenRepository implements ArtifactLocator {

	private static final char SLASH_CHAR = '/';

	private static final String LOCAL_REPOSITORY_ELEM = "localRepository";

	private static final Log log = LogFactory.getLog(LocalFileSystemMavenRepository.class);

	/** local repo system property */
	private static final String SYS_PROPERTY = LOCAL_REPOSITORY_ELEM;
	/** user home system property */
	private static final String USER_HOME_PROPERTY = "user.home";
	/** m2 local user settings */
	private static final String M2_DIR = ".m2";
	/** maven settings xml */
	private static final String M2_SETTINGS = M2_DIR.concat("/settings.xml");
	/** default local repository */
	private static final String DEFAULT_DIR = M2_DIR.concat("/repository");
	/** discovered local m2 repository home */
	private String repositoryHome;


	/**
	 * Initialization method It determines the repository path by checking the
	 * existence of <code>localRepository</code> system property and falling
	 * back to the <code>settings.xml</code> file and then the traditional
	 * <code>user.home/.m2/repository</code>.
	 * 
	 * <p/> This method is used to postpone initialization until an artifact is
	 * actually located. As the test class is instantiated on each test run, the
	 * init() method prevents repetitive, waste-less initialization.
	 * 
	 */
	private void init() {
		// already discovered a repository home, bailing out
		if (repositoryHome != null)
			return;

		boolean trace = log.isDebugEnabled();

		final String[] sysProperties = new String[2];
		// check system property
		AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				sysProperties[0] = System.getProperty(SYS_PROPERTY);
				sysProperties[1] = System.getProperty(USER_HOME_PROPERTY);
				return null;
			}
		});
		String localRepository = sysProperties[0];
		String userHome = sysProperties[1];

		if (trace)
			log.trace("M2 system property [" + SYS_PROPERTY + "] has value=" + localRepository);

		if (localRepository == null) {
			// if it's not present then check settings.xml local repository property
			Resource settingsFile = new FileSystemResource(new File(userHome, M2_SETTINGS));
			localRepository = getMavenSettingsLocalRepository(settingsFile);
			if (trace)
				log.trace("Falling back to M2 settings.xml [" + settingsFile + "]; found value=" + localRepository);
			if (localRepository == null) {
				// fall back to the default location
				localRepository = new File(userHome, DEFAULT_DIR).getAbsolutePath();
				if (trace)
					log.trace("No custom setting found; using default M2 local repository=" + localRepository);

			}
		}

		repositoryHome = localRepository;
		log.info("Local Maven2 repository used: [" + repositoryHome + "]");
	}

	/**
	 * Returns the <code>localRepository</code> settings as indicated by the
	 * <code>settings.xml</code> file.
	 * 
	 * @return local repository as indicated by a Maven settings.xml file
	 */
	String getMavenSettingsLocalRepository(Resource m2Settings) {
		// no file found, return null to continue the discovery process
		if (!m2Settings.exists())
			return null;

		try {
			DocumentLoader docLoader = new DefaultDocumentLoader();
			Document document = docLoader.loadDocument(new InputSource(m2Settings.getInputStream()), null, null,
				XmlValidationModeDetector.VALIDATION_NONE, false);

			return (DomUtils.getChildElementValueByTagName(document.getDocumentElement(), LOCAL_REPOSITORY_ELEM));
		}
		catch (Exception ex) {
			throw (RuntimeException) new RuntimeException(new ParserConfigurationException("error parsing resource="
					+ m2Settings).initCause(ex));
		}
	}

	/**
	 * Find a local maven artifact. First tries to find the resource as a
	 * packaged artifact produced by a local maven build, and if that fails will
	 * search the local maven repository.
	 * 
	 * @param groupId - the groupId of the organization supplying the bundle
	 * @param artifactId - the artifact id of the bundle
	 * @param version - the version of the bundle
	 * @return the String representing the URL location of this bundle
	 */
	public Resource locateArtifact(String groupId, String artifactId, String version) {
		return locateArtifact(groupId, artifactId, version, DEFAULT_ARTIFACT_TYPE);
	}

	/**
	 * Find a local maven artifact. First tries to find the resource as a
	 * packaged artifact produced by a local maven build, and if that fails will
	 * search the local maven repository.
	 * 
	 * @param groupId - the groupId of the organization supplying the bundle
	 * @param artifactId - the artifact id of the bundle
	 * @param version - the version of the bundle
	 * @param type - the extension type of the artifact
	 * @return
	 */
	public Resource locateArtifact(final String groupId, final String artifactId, final String version,
			final String type) {
		init();

		return (Resource) AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				try {

					return localMavenBuildArtifact(groupId, artifactId, version, type);
				}
				catch (IllegalStateException illStateEx) {
					Resource localMavenBundle = localMavenBundle(groupId, artifactId, version, type);
					if (log.isDebugEnabled()) {
						StringBuffer buf = new StringBuffer();
						buf.append("[");
						buf.append(groupId);
						buf.append("|");
						buf.append(artifactId);
						buf.append("|");
						buf.append(version);
						buf.append("]");
						log.debug(buf
								+ " local maven build artifact detection failed, falling back to local maven bundle "
								+ localMavenBundle.getDescription());
					}
					return localMavenBundle;
				}
			}
		});
	}

	/**
	 * Return the resource of the indicated bundle in the local Maven repository
	 * 
	 * @param groupId - the groupId of the organization supplying the bundle
	 * @param artifact - the artifact id of the bundle
	 * @param version - the version of the bundle
	 * @return
	 */
	protected Resource localMavenBundle(String groupId, String artifact, String version, String type) {
		StringBuffer location = new StringBuffer(groupId.replace('.', SLASH_CHAR));
		location.append(SLASH_CHAR);
		location.append(artifact);
		location.append(SLASH_CHAR);
		location.append(version);
		location.append(SLASH_CHAR);
		location.append(artifact);
		location.append('-');
		location.append(version);
		location.append(".");
		location.append(type);

		return new FileSystemResource(new File(repositoryHome, location.toString()));
	}

	/**
	 * Find a local maven artifact in the current build tree. This searches for
	 * resources produced by the package phase of a maven build.
	 * 
	 * @param artifactId
	 * @param version
	 * @param type
	 * @return
	 */
	protected Resource localMavenBuildArtifact(String groupId, String artifactId, String version, String type) {
		try {
			File found = new MavenPackagedArtifactFinder(groupId, artifactId, version, type).findPackagedArtifact(new File(
				"."));
			Resource res = new FileSystemResource(found);
			if (log.isDebugEnabled()) {
				log.debug("[" + artifactId + "|" + version + "] resolved to " + res.getDescription()
						+ " as a local maven artifact");
			}
			return res;
		}
		catch (IOException ioEx) {
			throw (RuntimeException) new IllegalStateException("Artifact " + artifactId + "-" + version + "." + type
					+ " could not be found").initCause(ioEx);
		}
	}
}