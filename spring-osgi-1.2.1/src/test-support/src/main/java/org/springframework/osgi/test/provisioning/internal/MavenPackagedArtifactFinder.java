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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.DefaultDocumentLoader;
import org.springframework.beans.factory.xml.DocumentLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.xml.DomUtils;
import org.springframework.util.xml.XmlValidationModeDetector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Find a packaged maven artifact starting in some root directory of a maven
 * project.
 * 
 * Poor approximation as doesn't check groupId. Could probably be done better
 * using some maven API.... but I can't find any good doc.
 * 
 * @author Adrian Colyer
 */
public class MavenPackagedArtifactFinder {

	/** logger */
	private static final Log log = LogFactory.getLog(MavenPackagedArtifactFinder.class);

	private static final String POM_XML = "pom.xml";
	private static final String TARGET = "target";
	private static final String GROUP_ID_ELEM = "groupId";

	private final String artifactName;
	private final String groupId;


	public MavenPackagedArtifactFinder(String groupId, String artifactId, String version, String type) {
		this.groupId = groupId;
		this.artifactName = artifactId + "-" + version + "." + type;
	}

	File findPackagedArtifact(File startingDirectory) throws IOException {
		if (!isMavenProjectDirectory(startingDirectory)) {
			throw new IllegalStateException(startingDirectory + " does not contain a pom.xml file");
		}
		File rootMavenProjectDir = findRootMavenProjectDir(startingDirectory.getCanonicalFile());
		if (log.isTraceEnabled())
			log.trace("Starting local artifact search from " + rootMavenProjectDir.getAbsolutePath());

		File found = findInDirectoryTree(artifactName, rootMavenProjectDir);
		if (found == null) {
			throw new FileNotFoundException("Cannot find the artifact <" + artifactName + "> with groupId <" + groupId
					+ ">");
		}
		return found;
	}

	/**
	 * Check whether the folder is a maven dir.
	 * 
	 * @param dir
	 * @return
	 */
	private boolean isMavenProjectDirectory(File dir) {
		return (dir.isDirectory() && new File(dir, POM_XML).exists());
	}

	/**
	 * Returns the <tt>groupId</tt> setting in a <tt>pom.xml</tt> file.
	 * 
	 * @return a <tt>pom.xml</tt> <tt>groupId</tt>.
	 */
	String getGroupIdFromPom(Resource pomXml) {
		try {
			DocumentLoader docLoader = new DefaultDocumentLoader();
			Document document = docLoader.loadDocument(new InputSource(pomXml.getInputStream()), null, null,
				XmlValidationModeDetector.VALIDATION_NONE, false);

			String groupId = DomUtils.getChildElementValueByTagName(document.getDocumentElement(), GROUP_ID_ELEM);
			// no groupId specified, try the parent definition
			if (groupId == null) {
				if (log.isTraceEnabled())
					log.trace("No groupId defined; checking for the parent definition");
				Element parent = DomUtils.getChildElementByTagName(document.getDocumentElement(), "parent");
				if (parent != null)
					return DomUtils.getChildElementValueByTagName(parent, GROUP_ID_ELEM);
			}
			else {
				return groupId;
			}
		}
		catch (Exception ex) {
			throw (RuntimeException) new RuntimeException(new ParserConfigurationException("error parsing resource="
					+ pomXml).initCause(ex));
		}

		throw new IllegalArgumentException("no groupId or parent/groupId defined by resource ["
				+ pomXml.getDescription() + "]");

	}

	private File findRootMavenProjectDir(File dir) {
		File lastFoundMavenProjectDir = dir;
		File parentDir = dir.getParentFile();
		while (parentDir != null && isMavenProjectDirectory(parentDir)) {
			lastFoundMavenProjectDir = parentDir;
			parentDir = parentDir.getParentFile();
		}
		return lastFoundMavenProjectDir;
	}

	private File findInDirectoryTree(String fileName, File root) {
		boolean trace = log.isTraceEnabled();

		File targetDir = new File(root, TARGET);
		if (targetDir.exists()) {
			File artifact = new File(targetDir, fileName);
			// found artifact
			if (artifact.exists()) {
				if (trace)
					log.trace("Found artifact at " + artifact.getAbsolutePath() + "; checking the pom.xml groupId...");
				// check the groupId inside the parent pom
				// (happens late to allow filtering to occur)
				File pomXml = new File(root, POM_XML);
				String groupId = getGroupIdFromPom(new FileSystemResource(pomXml));
				if (trace)
					log.trace("Pom [" + pomXml.getAbsolutePath() + "] has groupId [" + groupId + "]");
				if (this.groupId.equals(groupId))
					return artifact;
			}
		}
		File[] children = root.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				if (!isMavenProjectDirectory(pathname)) {
					return false;
				}
				if (pathname.getName().equals(TARGET)) {
					return false;
				}
				if (pathname.getName().equals("src")) {
					return false;
				}
				if (pathname.getName().equals(".svn")) {
					return false;
				}
				return true;
			}
		});

		for (int i = 0; i < children.length; i++) {
			File found = findInDirectoryTree(fileName, children[i]);
			if (found != null) {
				return found;
			}
		}

		return null;
	}
}
