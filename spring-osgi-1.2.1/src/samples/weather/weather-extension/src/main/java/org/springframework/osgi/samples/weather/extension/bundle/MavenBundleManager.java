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
 * Taken from xbean 2.x on 4-May-2006 by Andy Piper
 */
package org.springframework.osgi.samples.weather.extension.bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.util.StringUtils;

/**
 * Creates virtual bundles using maven jars and artifact information, then installs those
 * bundles into an OSGi runtime.
 *
 * @author Dain Sundstrom
 * @author Andy Piper
 * @author Adrian Colyer
 */
/* package */
class MavenBundleManager
{
	
	private static final int NO_MORE_DATA = -1;
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	
	private final BundleContext bundleContext;
	private final URL localRepository;

	/**
	 * Create a new maven bundle manager that finds artifacts in the given maven repository
	 * and installs bundles into a running OSGi platform via the given bundleContext
	 * @param bundleContext  context to use for installing bundles into OSGi runtime
	 * @param localRepository location of maven repository to use for artifact discovery
	 */
	public MavenBundleManager(BundleContext bundleContext, URL localRepository) {
		this.bundleContext = bundleContext;
		this.localRepository = localRepository;
	}

	/**
	 * Load the maven Project Object Model (pom) for the given artifact
	 * @param artifact
	 * @return
	 */
	public Project loadProject(Artifact artifact) {
		if (artifact instanceof Project) {
			return (Project) artifact;
		} else {
			return new Project(artifact.getGroupId(),
					           artifact.getArtifactId(),
					           artifact.getVersion(),
					           artifact.getType());
		}
	}

	/**
	 * Load the maven Project Object Model (pom) for the artifact with the given
	 * groupId, artifactId, and version.
	 * @param groupId    maven groupId of the pom to be loaded
	 * @param artifactId maven artifactId of the pom to be loaded
	 * @param version version of the pom to be loaded
	 * @return
	 */
	public Project loadProject(String groupId, String artifactId, String version) {
		return new Project(groupId, artifactId, version, "jar");
	}

	/**
	 * Find the maven artifact represented by the given groupId, artifactId, and version,
	 * and install that artifact as a bundle into the OSGi runtime
	 * @param groupId  the groupId of the artifact to be installed
	 * @param artifactId the artifactId of the artifact to be installed
	 * @param version the version of the artifact to be installed
	 * @return a Bundle object representing the newly installed bundle
	 * @throws Exception
	 */
	public Bundle installBundle(String groupId, String artifactId, String version) throws Exception {
		return installBundle(loadProject(groupId, artifactId, version));
	}

	/**
	 * Create and install an OSGi bundle representing the given artifact.
	 * @param artifact the artifact to be installed
	 * @return a Bundle object representing the newly installed bundle
	 * @throws Exception
	 */
	public Bundle installBundle(Artifact artifact) throws Exception {
		String symbolicName = artifact.getGroupId() + "." + artifact.getArtifactId();
		String bundleVersion = coerceToOsgiVersion(artifact.getVersion());

		// check if we already loaded this bundle
		// REVIEW andyp -- I'm not convinced this is the right way to go.
		Bundle alreadyInstalledBundle = findBundle(symbolicName,bundleVersion);
		if (alreadyInstalledBundle != null) {
			return alreadyInstalledBundle;
		}

		// load the project object model for this artifact
		Project project = loadProject(artifact);

		// build an OSGi manifest for the project
		Manifest manifest = createOsgiManifest(project);

		// create in-memory jar file
		byte[] jarBytes = createInMemoryJar(project, manifest);
		ByteArrayInputStream bin = new ByteArrayInputStream(jarBytes);

		// install the in-memory jar
		Bundle bundle = bundleContext.installBundle(symbolicName, bin);
		bin.close();

		// install bundles for all of the dependencies
		for (Iterator iterator = project.getDependencies().iterator(); iterator.hasNext();) {
			Artifact dependency = (Artifact) iterator.next();
			installBundle(dependency);
		}

		return bundle;
	}

	/**
	 * Create an in-memory jar file that contains all of the entries in the project's jar,
	 * using the supplied OSGi manifest
	 * @param project the maven project artifact used as the source of the jar entries
	 * @param manifest the OSGi manifest for the project
	 * @return byte array containing an in-memory representation of the newly created jar file
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private byte[] createInMemoryJar(Project project, Manifest manifest) throws MalformedURLException, IOException {
		URL jarPath = project.getJarPath(localRepository.toString());

		// streams we need to close afterwards...
		InputStream in = null;
		JarInputStream jin = null;
		ByteArrayOutputStream out = null;
		JarOutputStream jarOut = null;

		try {
			in = jarPath.openStream();
			jin = new JarInputStream(jarPath.openStream());
			
			// create a jar in-memory for the manifest
			out = new ByteArrayOutputStream();
			jarOut = new JarOutputStream(out, manifest);
			byte[] entryDataBuffer = new byte[DEFAULT_BUFFER_SIZE];

			// Copy entries from the real jar to our virtual jar
			for (JarEntry ze = jin.getNextJarEntry(); ze != null; ze = jin.getNextJarEntry()) {
				jarOut.putNextEntry(ze);
				byte[] b = readEntryBytes(jin, entryDataBuffer);
				jarOut.write(b, 0, b.length);
				jin.closeEntry();
				jarOut.closeEntry();
			}
		}
		finally {
			closeStreams(in, jin, out, jarOut);
		}
		
		byte[] jarBytes = out.toByteArray();
		return jarBytes;
	}

	/**
	 * Read the bytes from an entry in a jar input stream
	 * @param jin the jar entry input stream
	 * @param entryDataBuffer buffer to use for reading data
	 * @return the bytes contained in the jar entry
	 * @throws IOException
	 */
	private byte[] readEntryBytes(JarInputStream jin, byte[] entryDataBuffer) throws IOException {
		ByteArrayOutputStream baos = null; 
		try {
			baos = new ByteArrayOutputStream();
			int bytesRead = NO_MORE_DATA;
			while( (bytesRead = jin.read(entryDataBuffer)) != NO_MORE_DATA) {
				baos.write(entryDataBuffer, 0, bytesRead);
			}
		}
		finally {
			// attempt to close stream, but let exception propagate if there is one...
			if (baos != null ) {
				baos.close();
			}
		}
		return baos.toByteArray();
	}

	/**
	 * Close all of the streams used during creation of in-memory jar file.
	 * @param in raw input stream
	 * @param jin jar input stream wrapping raw input stream
	 * @param out raw output stream
	 * @param jarOut jar output stream wrapping raw output stream
	 * @throws IOException
	 */
	private void closeStreams(InputStream in, JarInputStream jin, ByteArrayOutputStream out, JarOutputStream jarOut) throws IOException {
		IOException closingException = null;
		// close input stream
		// closing outermost stream also closes wrapped streams...
		if (null != jin) {
			try {
				jin.close();
			}
			catch(IOException ex) {
				closingException = ex;
			}
		} 
		else if (null != in) {
			try {
				in.close();
			}
			catch(IOException ex) {
				closingException = ex;
			}
		}
		
		// close output stream
		if (null != jarOut) {
			try {
				jarOut.close();
			}
			catch (IOException ex) {
				closingException = ex;
			}
		} 
		else if (null != out) {
			try {
				out.close();
			}
			catch (IOException ex) {
				closingException = ex;
			}
		}
		if (closingException != null) {
			throw closingException;
		}
	}

	/**
	 * Look for bundle with the given symbolic name and version already installed in
	 * the OSGi runtime
	 * @param symbolicName  symbolic name of bundle to search for
	 * @param bundleVersion version of bundle to search for
	 * @return a Bundle object for the installed bundled, if found; null otherwise
	 */
	private Bundle findBundle(String symbolicName, String bundleVersion) {
		Bundle[] bundles = bundleContext.getBundles();
		for (int i = 0; i < bundles.length; i++) {
			Bundle bundle = bundles[i];
			if (symbolicName.equals(bundle.getSymbolicName()) &&
					bundleVersion.equals(bundle.getHeaders().get(Constants.BUNDLE_VERSION))) {
				return bundle;
			}
		}		
		return null;
	}

	/**
	 * Create an OSGi-manifest for a given maven project
	 * @param project the project to create a manifest for
	 * @return Manifest object with correct OSGi headers
	 * @throws IOException
	 */
	public Manifest createOsgiManifest(Project project) throws IOException {
		String groupId = project.getGroupId();
		String artifactId = project.getArtifactId();
		String version = project.getVersion();

		Manifest manifest = new Manifest();
		Attributes attributes = manifest.getMainAttributes();
		attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		attributes.putValue(Constants.BUNDLE_MANIFESTVERSION, "2");
		attributes.putValue(Constants.BUNDLE_VENDOR, groupId);
		attributes.putValue(Constants.BUNDLE_NAME, artifactId);
		attributes.putValue(Constants.BUNDLE_VERSION, coerceToOsgiVersion(version));
		attributes.putValue(Constants.BUNDLE_SYMBOLICNAME, groupId + "." + artifactId);

		String exportPackageHeader = getExportPackageHeader(project);
		attributes.putValue(Constants.EXPORT_PACKAGE, exportPackageHeader);

		// Import user configured packages.
		if (!project.getImports().isEmpty()) {
			String importPackageHeader = getImportPackageHeader(project);
			attributes.putValue(Constants.IMPORT_PACKAGE, importPackageHeader);
		}

		// REVIEW andyp -- according to the OSGi gurus require-bundle is bad practice
		String requireBundleHeader = getRequireBundleHeaders(project);
		if (requireBundleHeader.length() > 0) {
			attributes.putValue(Constants.REQUIRE_BUNDLE, requireBundleHeader);
		}

		return manifest;
	}

	/**
	 * Create an Import-Package header from the project imports
	 * @param project
	 * @return
	 */
	private String getImportPackageHeader(Project project) {
		StringBuffer imports = new StringBuffer();
		for (Iterator iter = project.getImports().iterator(); iter.hasNext();) {
			PackageSpecification packageImport = (PackageSpecification) iter.next();
			if (imports.length() > 0) {
				imports.append(",");
			}
			imports.append(packageImport.getName());
			// Add optional version
			if (StringUtils.hasText(packageImport.getVersion())) {
				imports.append(";version=").append(packageImport.getVersion());
			}
		}
		return imports.toString();
	}

	/**
	 * Create an Export-Package header. If user has specified exports in the pom, use
	 * user-supplied list, else calculate exports from project artifact jar file.
	 * @param project
	 * @return
	 * @throws IOException
	 */
	private String getExportPackageHeader(Project project) throws IOException {
		StringBuffer exports;
		URL jarPath = project.getJarPath(localRepository.toString());
		if (!project.getExports().isEmpty()) {
			exports = new StringBuffer();
			for (Iterator iter = project.getExports().iterator(); iter.hasNext();) {
				PackageSpecification packageExport = (PackageSpecification) iter.next();
				if (exports.length() > 0) {
					exports.append(",");
				}
				exports.append(packageExport.getName());
				// Add optional version
				if (StringUtils.hasText(packageExport.getVersion())) {
					exports.append(";version=").append(coerceToOsgiVersion(packageExport.getVersion()));
				}
			}
		} else {
			exports = createExportList(jarPath);
		}
		return exports.toString();
	}

	/**
	 * Create a Require-Bundle header entry based on the project dependencies in the pom.
	 * @param project
	 * @return
	 */
	private String getRequireBundleHeaders(Project project) {
		StringBuffer requireBundle = new StringBuffer();
		for (Iterator iterator = project.getDependencies().iterator(); iterator.hasNext();) {
			Artifact dependency = (Artifact) iterator.next();
			if (requireBundle.length() > 0) {
				requireBundle.append(',');
			}
			requireBundle.append(dependency.getGroupId()).append('.').append(dependency.getArtifactId());
			requireBundle.append(";visibility:=reexport;bundle-version:=").append(coerceToOsgiVersion(dependency.getVersion()));
		}
		return requireBundle.toString();
	}

	/**
	 * Coerce a maven version string into a format acceptable by OSGi
	 * @param version
	 * @return
	 */
	private static String coerceToOsgiVersion(String version) {
		int partsFound = 0;
		String[] versionParts = new String[]{"0", "0", "0"};
		StringBuffer qualifier = new StringBuffer();
		for (StringTokenizer stringTokenizer = new StringTokenizer(version, ".-"); stringTokenizer.hasMoreTokens();) {
			String part = stringTokenizer.nextToken();
			if (partsFound < 4) {
				try {
					Integer.parseInt(part);
					versionParts[partsFound++] = part;
				} catch (NumberFormatException e) {
					partsFound = 4;
					qualifier.append(coerceToOsgiQualifier(part));
				}
			} else {
				if (qualifier.length() > 0) qualifier.append("_");
				qualifier.append(coerceToOsgiQualifier(part));
			}
		}

		StringBuffer osgiVersion = new StringBuffer();
		osgiVersion.append(versionParts[0]).append(".").append(versionParts[1]).append(".").append(versionParts[2]);
		if (qualifier.length() > 0) {
			osgiVersion.append(".").append(qualifier);
		}
		return osgiVersion.toString();
	}

	/**
	 * Coerce a maven version qualifier into a format acceptable to OSGi
	 * @param qualifier
	 * @return
	 */
	private static String coerceToOsgiQualifier(String qualifier) {
		char[] chars = qualifier.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
				chars[i] = '_';
			}
		}
		return new String(chars);
	}

	/**
	 * Find all the packages included in the given jar, and create an Export-Package
	 * OSGi manifest header to export them all.
	 * @param jarPath
	 * @return
	 * @throws IOException
	 */
	private static StringBuffer createExportList(URL jarPath) throws IOException {
		Set packages = determinePackagesIncludedInJar(jarPath);

		StringBuffer exports = new StringBuffer();
		for (Iterator iterator = packages.iterator(); iterator.hasNext();) {
			String packageName = (String) iterator.next();
			if (exports.length() > 0) {
				exports.append(";");
			}
			exports.append(packageName);
		}
		return exports;
	}

	/**
	 * Look for all packages in the given jar, excluding the default package and 
	 * META-INF.
	 * @param jarPath
	 * @return
	 * @throws IOException
	 */
	private static Set determinePackagesIncludedInJar(URL jarPath) throws IOException {
		Set packages = new HashSet();
		InputStream in = null;
		JarInputStream jarIn = null;
		try {
			// FIXME andyp -- don't retrieve the jar twice
			in = jarPath.openStream();
			jarIn = new JarInputStream(in);
			for (JarEntry jarEntry = jarIn.getNextJarEntry(); jarEntry != null; jarEntry = jarIn.getNextJarEntry()) {
				String packageName = jarEntry.getName();
				if (!jarEntry.isDirectory()) {
					int index = packageName.lastIndexOf("/");
					// we can't export the default package
					if (index > 0) {
						packageName = packageName.substring(0, index);
						if (!packageName.equals("META-INF")) {
							packageName = packageName.replace('/', '.');
							packages.add(packageName);
						}
					}
				}
			}
		}
		finally {
			if (jarIn != null) {
				try {
					jarIn.close();
				}
				catch (IOException e) {
					// ignore at this point
				}
			}
			else if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					// ignore at this point
				}
			}
		}
		return packages;
	}
}
