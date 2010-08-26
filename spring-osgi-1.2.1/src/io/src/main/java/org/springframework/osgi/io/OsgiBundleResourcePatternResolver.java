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

package org.springframework.osgi.io;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.osgi.io.internal.OsgiHeaderUtils;
import org.springframework.osgi.io.internal.OsgiResourceUtils;
import org.springframework.osgi.io.internal.OsgiUtils;
import org.springframework.osgi.io.internal.resolver.DependencyResolver;
import org.springframework.osgi.io.internal.resolver.ImportedBundle;
import org.springframework.osgi.io.internal.resolver.PackageAdminResolver;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

/**
 * OSGi-aware {@link ResourcePatternResolver}.
 * 
 * Can find resources in the <em>bundle jar</em> and <em>bundle space</em>.
 * See {@link OsgiBundleResource} for more information.
 * 
 * <p/><b>ClassPath support</b>
 * 
 * <p/>As mentioned by {@link PathMatchingResourcePatternResolver}, class-path
 * pattern matching needs to resolve the class-path structure to a file-system
 * location (be it an actual folder or a jar). Inside the OSGi environment this
 * is problematic as the bundles can be loaded in memory directly from input
 * streams. To avoid relying on each platform bundle storage structure, this
 * implementation tries to determine the bundles that assemble the given bundle
 * class-path and analyze each of them individually. This involves the bundle
 * archive (including special handling of the <code>Bundle-Classpath</code> as
 * it is computed at runtime), the bundle required packages and its attached
 * fragments.
 * 
 * Depending on the configuration of running environment, this might cause
 * significant IO activity which can affect performance.
 * 
 * <p/><b>Note:</b> Currently, <em>static</em> imports as well as
 * <code>Bundle-Classpath</code> and <code>Required-Bundle</code> entries
 * are supported. Support for <code>DynamicPackage-Import</code> depends on
 * how/when the underlying platform does the wiring between the dynamically
 * imported bundle and the given bundle.
 * 
 * <p/><b>Portability Note:</b> Since it relies only on the OSGi API, this
 * implementation depends heavily on how closely the platform implements the
 * OSGi spec. While significant tests have been made to ensure compatibility,
 * one <em>might</em> experience different behaviour especially when dealing
 * with jars with missing folder entries or boot-path delegation. It is strongly
 * recommended that wildcard resolution be thoroughly tested before switching to
 * a different platform before you rely on it.
 * 
 * @see Bundle
 * @see OsgiBundleResource
 * @see PathMatchingResourcePatternResolver
 * 
 * @author Costin Leau
 * 
 */
public class OsgiBundleResourcePatternResolver extends PathMatchingResourcePatternResolver {

	/**
	 * Our own logger to protect against incompatible class changes.
	 */
	private static final Log logger = LogFactory.getLog(OsgiBundleResourcePatternResolver.class);

	/**
	 * The bundle on which this resolver works on.
	 */
	private final Bundle bundle;

	/**
	 * The bundle context associated with this bundle.
	 */
	private final BundleContext bundleContext;

	private static final String FOLDER_SEPARATOR = "/";

	private static final String FOLDER_WILDCARD = "**";

	private static final String JAR_EXTENSION = ".jar";

	private static final String BUNDLE_DEFAULT_CP = ".";

	private static final char SLASH = '/';

	private static final char DOT = '.';

	// use the default package admin version
	private final DependencyResolver resolver;


	public OsgiBundleResourcePatternResolver(Bundle bundle) {
		this(new OsgiBundleResourceLoader(bundle));
	}

	public OsgiBundleResourcePatternResolver(ResourceLoader resourceLoader) {
		super(resourceLoader);
		if (resourceLoader instanceof OsgiBundleResourceLoader) {
			this.bundle = ((OsgiBundleResourceLoader) resourceLoader).getBundle();
		}
		else {
			this.bundle = null;
		}

		this.bundleContext = (bundle != null ? OsgiUtils.getBundleContext(this.bundle) : null);
		this.resolver = (bundleContext != null ? new PackageAdminResolver(bundleContext) : null);
	}

	/**
	 * Finds existing resources. This method returns the actual resources found
	 * w/o adding any extra decoration (such as non-existing resources).
	 * 
	 * @param locationPattern location pattern
	 * @return found resources (w/o any decoration)
	 * @throws IOException in case of I/O errors
	 */
	protected Resource[] findResources(String locationPattern) throws IOException {
		Assert.notNull(locationPattern, "Location pattern must not be null");
		int type = OsgiResourceUtils.getSearchType(locationPattern);

		// look for patterns (includes classpath*:)
		if (getPathMatcher().isPattern(locationPattern)) {
			// treat classpath as a special case
			if (OsgiResourceUtils.isClassPathType(type))
				return findClassPathMatchingResources(locationPattern, type);

			return findPathMatchingResources(locationPattern, type);
		}
		// even though we have no pattern
		// the OSGi space can return multiple entries for the same resource name
		// - treat this case below
		else {
			Resource[] result = null;

			OsgiBundleResource resource = new OsgiBundleResource(bundle, locationPattern);

			switch (type) {
				// same as bundle space
				case OsgiResourceUtils.PREFIX_TYPE_NOT_SPECIFIED:
					// consider bundle-space which can return multiple URLs
				case OsgiResourceUtils.PREFIX_TYPE_BUNDLE_SPACE:
					result = resource.getAllUrlsFromBundleSpace(locationPattern);
					break;
				// for the rest go with the normal resolving
				default:
					if (!resource.exists())
						result = new Resource[] { resource };
					break;
			}
			return result;
		}
	}

	// add a non-existing resource, if none was found and no pattern was specified
	public Resource[] getResources(final String locationPattern) throws IOException {

		Resource[] resources = findResources(locationPattern);

		// check whether we found something or we should fall-back to a
		// non-existing resource
		if (ObjectUtils.isEmpty(resources) && (!getPathMatcher().isPattern(locationPattern))) {
			return new Resource[] { getResourceLoader().getResource(locationPattern) };
		}
		// return the original array
		return resources;

	}

	/**
	 * Special classpath method. Will try to detect the imported bundles (which
	 * are part of the classpath) and look for resources in all of them. This
	 * implementation will try to determine the bundles that compose the current
	 * bundle classpath and then it will inspect the bundle space of each of
	 * them individually.
	 * 
	 * <p/> Since the bundle space is considered, runtime classpath entries such
	 * as dynamic imports are not supported (yet).
	 * 
	 * @param locationPattern
	 * @param type
	 * @return classpath resources
	 */
	private Resource[] findClassPathMatchingResources(String locationPattern, int type) throws IOException {

		if (resolver == null)
			throw new IllegalArgumentException(
				"PackageAdmin service/a started bundle is required for classpath matching");

		final ImportedBundle[] importedBundles = resolver.getImportedBundles(bundle);

		// eliminate classpath path
		final String path = OsgiResourceUtils.stripPrefix(locationPattern);

		final Collection foundPaths = new LinkedHashSet();

		// 1. search the imported packages

		// find folder path matching 
		final String rootDirPath = determineFolderPattern(path);

		if (System.getSecurityManager() != null) {
			AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					for (int i = 0; i < importedBundles.length; i++) {
						final ImportedBundle importedBundle = importedBundles[i];
						if (!bundle.equals(importedBundle.getBundle())) {
							findImportedBundleMatchingResource(importedBundle, rootDirPath, path, foundPaths);
						}
					}
					return null;
				}
			});
		}
		else {
			for (int i = 0; i < importedBundles.length; i++) {
				final ImportedBundle importedBundle = importedBundles[i];
				if (!bundle.equals(importedBundle.getBundle())) {
					findImportedBundleMatchingResource(importedBundle, rootDirPath, path, foundPaths);
				}
			}
		}

		// 2. search the target bundle
		findSyntheticClassPathMatchingResource(bundle, path, foundPaths);

		// 3. resolve the entries using the official class-path method (as some of them might be hidden)
		List resources = new ArrayList(foundPaths.size());

		for (Iterator iterator = foundPaths.iterator(); iterator.hasNext();) {
			// classpath*: -> getResources()
			String resourcePath = (String) iterator.next();
			if (OsgiResourceUtils.PREFIX_TYPE_CLASS_ALL_SPACE == type) {
				CollectionUtils.mergeArrayIntoCollection(convertURLEnumerationToResourceArray(
					bundle.getResources(resourcePath), resourcePath), resources);
			}
			// classpath -> getResource()
			else {
				URL url = bundle.getResource(resourcePath);
				if (url != null)
					resources.add(new UrlContextResource(url, resourcePath));
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Fitered " + foundPaths + " to " + resources);
		}

		return (Resource[]) resources.toArray(new Resource[resources.size()]);
	}

	private String determineFolderPattern(String path) {
		int index = path.lastIndexOf(FOLDER_SEPARATOR);
		return (index > 0 ? path.substring(0, index + 1) : "");
	}

	private ContextResource[] convertURLEnumerationToResourceArray(Enumeration enm, String path) {
		Set resources = new LinkedHashSet(4);
		while (enm != null && enm.hasMoreElements()) {
			resources.add(new UrlContextResource((URL) enm.nextElement(), path));
		}
		return (ContextResource[]) resources.toArray(new ContextResource[resources.size()]);
	}

	/**
	 * Searches for the given pattern inside the imported bundle. This
	 * translates to pattern matching on the imported packages.
	 * 
	 * @param importedBundle imported bundle
	 * @param path path used for pattern matching
	 * @param foundPaths collection of found results
	 */
	private void findImportedBundleMatchingResource(final ImportedBundle importedBundle, String rootPath, String path,
			final Collection foundPaths) {

		final boolean trace = logger.isTraceEnabled();

		String[] packages = importedBundle.getImportedPackages();

		if (trace)
			logger.trace("Searching path [" + path + "] on imported pkgs " + ObjectUtils.nullSafeToString(packages)
					+ "...");

		final boolean startsWithSlash = rootPath.startsWith(FOLDER_SEPARATOR);

		for (int i = 0; i < packages.length; i++) {
			// transform the package name into a path
			String pkg = packages[i].replace(DOT, SLASH) + SLASH;

			if (startsWithSlash) {
				pkg = FOLDER_SEPARATOR + pkg;
			}

			final PathMatcher matcher = getPathMatcher();
			// if the imported package matches the path
			if (matcher.matchStart(path, pkg)) {
				// start the JAR analysis
				Enumeration entries = importedBundle.getBundle().getEntryPaths(pkg);
				while (entries != null && entries.hasMoreElements()) {
					String entry = (String) entries.nextElement();
					if (startsWithSlash)
						entry = FOLDER_SEPARATOR + entry;

					if (matcher.match(path, entry)) {
						if (trace)
							logger.trace("Found entry [" + entry + "]");
						foundPaths.add(entry);
					}
				}
			}
		}
	}

	/**
	 * Applies synthetic class-path analysis. That is, search the bundle space
	 * and the bundle class-path for entries matching the given path.
	 * 
	 * @param bundle
	 * @param path
	 * @param foundPaths
	 * @throws IOException
	 */
	private void findSyntheticClassPathMatchingResource(Bundle bundle, String path, Collection foundPaths)
			throws IOException {
		// 1. bundle space lookup
		OsgiBundleResourcePatternResolver localPatternResolver = new OsgiBundleResourcePatternResolver(bundle);
		Resource[] foundResources = localPatternResolver.findResources(path);

		boolean trace = logger.isTraceEnabled();

		if (trace)
			logger.trace("Found synthetic cp resources " + ObjectUtils.nullSafeToString(foundResources));

		for (int j = 0; j < foundResources.length; j++) {
			// assemble only the OSGi paths
			foundPaths.add(foundResources[j].getURL().getPath());
		}
		// 2. Bundle-Classpath lookup (on the path stripped of the prefix)
		Collection cpMatchingPaths = findBundleClassPathMatchingPaths(bundle, path);

		if (trace)
			logger.trace("Found Bundle-ClassPath matches " + cpMatchingPaths);

		foundPaths.addAll(cpMatchingPaths);

		// 3. Required-Bundle is considered already by the dependency resolver
	}

	/**
	 * Searches the bundle classpath (Bundle-Classpath) entries for the given
	 * pattern.
	 * 
	 * @param bundle
	 * @param pattern
	 * @return
	 * @throws IOException
	 */
	private Collection findBundleClassPathMatchingPaths(Bundle bundle, String pattern) throws IOException {
		// list of strings pointing to the matching resources 
		List list = new ArrayList(4);

		boolean trace = logger.isTraceEnabled();
		if (trace)
			logger.trace("Analyzing " + Constants.BUNDLE_CLASSPATH + " entries for bundle [" + bundle.getBundleId()
					+ "|" + bundle.getSymbolicName() + "]");
		// see if there is a bundle class-path defined
		String[] entries = OsgiHeaderUtils.getBundleClassPath(bundle);

		if (trace)
			logger.trace("Found " + Constants.BUNDLE_CLASSPATH + " entries " + ObjectUtils.nullSafeToString(entries));

		// 1. if so, look at the entries
		for (int i = 0; i < entries.length; i++) {
			String entry = entries[i];

			// make sure to exclude the default entry
			if (!entry.equals(BUNDLE_DEFAULT_CP)) {

				// 2. locate resource first from the bundle space (since it might not exist)
				OsgiBundleResource entryResource = new OsgiBundleResource(bundle, entry);
				// call the internal method to avoid catching an exception
				URL url = null;
				ContextResource res = entryResource.getResourceFromBundleSpace(entry);
				if (res != null) {
					url = res.getURL();
				}

				if (trace)
					logger.trace("Classpath entry [" + entry + "] resolves to [" + url + "]");
				// we've got a valid entry so let's parse it
				if (url != null) {
					String cpEntryPath = url.getPath();
					// is it a jar ?
					if (entry.endsWith(JAR_EXTENSION))
						findBundleClassPathMatchingJarEntries(list, url, pattern);
					// no, so it must be a folder
					else
						findBundleClassPathMatchingFolders(list, bundle, cpEntryPath, pattern);
				}
			}
		}

		return list;
	}

	/**
	 * Checks the jar entries from the Bundle-Classpath for the given pattern.
	 * 
	 * @param list
	 * @param ur
	 */
	private void findBundleClassPathMatchingJarEntries(List list, URL url, String pattern) throws IOException {
		// get the stream to the resource and read it as a jar
		JarInputStream jis = new JarInputStream(url.openStream());
		Set result = new LinkedHashSet(8);

		// parse the jar and do pattern matching
		try {
			while (jis.available() > 0) {
				JarEntry jarEntry = jis.getNextJarEntry();
				// if the jar has ended, the entry can be null (on Sun JDK at least)
				if (jarEntry != null) {
					String entryPath = jarEntry.getName();

					// strip leading "/" if it does exist
					if (entryPath.startsWith(FOLDER_SEPARATOR)) {
						entryPath = entryPath.substring(FOLDER_SEPARATOR.length());
					}
					if (getPathMatcher().match(pattern, entryPath)) {
						result.add(entryPath);
					}
				}
			}
		}
		finally {
			try {
				jis.close();
			}
			catch (IOException io) {
				// ignore it - nothing we can't do about it
			}
		}

		if (logger.isTraceEnabled())
			logger.trace("Found in nested jar [" + url + "] matching entries " + result);

		list.addAll(result);
	}

	/**
	 * Checks the folder entries from the Bundle-Classpath for the given
	 * pattern.
	 * 
	 * @param list
	 * @param bundle
	 * @param cpEntryPath
	 * @param pattern
	 * @throws IOException
	 */
	private void findBundleClassPathMatchingFolders(List list, Bundle bundle, String cpEntryPath, String pattern)
			throws IOException {
		// append path to the pattern and do a normal search
		// folder/<pattern> starts being applied

		String bundlePathPattern;

		boolean entryWithFolderSlash = cpEntryPath.endsWith(FOLDER_SEPARATOR);
		boolean patternWithFolderSlash = pattern.startsWith(FOLDER_SEPARATOR);
		// concatenate entry + pattern w/o double slashes
		if (entryWithFolderSlash) {
			if (patternWithFolderSlash)
				bundlePathPattern = cpEntryPath + pattern.substring(1, pattern.length());
			else
				bundlePathPattern = cpEntryPath + pattern;
		}
		else {
			if (patternWithFolderSlash)
				bundlePathPattern = cpEntryPath + pattern;
			else
				bundlePathPattern = cpEntryPath + FOLDER_SEPARATOR + pattern;
		}

		// search the bundle space for the detected resource
		OsgiBundleResourcePatternResolver localResolver = new OsgiBundleResourcePatternResolver(bundle);
		Resource[] resources = localResolver.getResources(bundlePathPattern);

		boolean trace = logger.isTraceEnabled();
		List foundResources = (trace ? new ArrayList(resources.length) : null);

		try {
			// skip when dealing with non-existing resources
			if (resources.length == 1 && !resources[0].exists()) {
				return;
			}
			else {
				int cutStartingIndex = cpEntryPath.length();
				// add the resource stripping the cp
				for (int i = 0; i < resources.length; i++) {
					String path = resources[i].getURL().getPath().substring(cutStartingIndex);
					list.add(path);
					if (trace)
						foundResources.add(path);
				}
			}
		}
		finally {
			if (trace)
				logger.trace("Searching for [" + bundlePathPattern + "] revealed resources (relative to the cp entry ["
						+ cpEntryPath + "]): " + foundResources);
		}
	}

	/**
	 * Replace the super class implementation to pass in the searchType
	 * parameter.
	 * 
	 * @see PathMatchingResourcePatternResolver#findPathMatchingResources(String)
	 */
	private Resource[] findPathMatchingResources(String locationPattern, int searchType) throws IOException {
		String rootDirPath = determineRootDir(locationPattern);
		String subPattern = locationPattern.substring(rootDirPath.length());
		Resource[] rootDirResources = getResources(rootDirPath);

		boolean trace = logger.isTraceEnabled();

		if (trace)
			logger.trace("Found root resources for [" + rootDirPath + "] :"
					+ ObjectUtils.nullSafeToString(rootDirResources));

		Set result = new LinkedHashSet();
		for (int i = 0; i < rootDirResources.length; i++) {
			Resource rootDirResource = rootDirResources[i];
			if (isJarResource(rootDirResource)) {
				result.addAll(doFindPathMatchingJarResources(rootDirResource, subPattern));
			}
			else {
				result.addAll(doFindPathMatchingFileResources(rootDirResource, subPattern, searchType));
			}
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Resolved location pattern [" + locationPattern + "] to resources " + result);
		}
		return (Resource[]) result.toArray(new Resource[result.size()]);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Overrides the default check up since computing the URL can be fairly
	 * expensive operation as there is no caching (due to the framework dynamic
	 * nature).
	 */
	protected boolean isJarResource(Resource resource) throws IOException {
		if (resource instanceof OsgiBundleResource) {
			// check the resource type
			OsgiBundleResource bundleResource = (OsgiBundleResource) resource;
			// if it's known, then it's not a jar
			if (bundleResource.getSearchType() != OsgiResourceUtils.PREFIX_TYPE_UNKNOWN) {
				return false;
			}
			// otherwise the normal parsing occur
		}
		return super.isJarResource(resource);
	}

	/**
	 * Based on the search type, uses the appropriate searching method.
	 * 
	 * @see OsgiBundleResource#BUNDLE_URL_PREFIX
	 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver#getResources(java.lang.String)
	 */
	private Set doFindPathMatchingFileResources(Resource rootDirResource, String subPattern, int searchType)
			throws IOException {

		String rootPath = null;

		if (rootDirResource instanceof OsgiBundleResource) {
			OsgiBundleResource bundleResource = (OsgiBundleResource) rootDirResource;
			rootPath = bundleResource.getPath();
			searchType = bundleResource.getSearchType();
		}
		else if (rootDirResource instanceof UrlResource) {
			rootPath = rootDirResource.getURL().getPath();
		}

		if (rootPath != null) {
			String cleanPath = OsgiResourceUtils.stripPrefix(rootPath);
			// sanitize the root folder (since it's possible to not specify the root which fails any further matches)
			if (!cleanPath.endsWith(FOLDER_SEPARATOR)) {
				cleanPath = cleanPath + FOLDER_SEPARATOR;
			}
			String fullPattern = cleanPath + subPattern;
			Set result = new LinkedHashSet();
			doRetrieveMatchingBundleEntries(bundle, fullPattern, cleanPath, result, searchType);
			return result;
		}
		else {
			return super.doFindPathMatchingFileResources(rootDirResource, subPattern);
		}
	}

	/**
	 * Searches each level inside the bundle for entries based on the search
	 * strategy chosen.
	 * 
	 * @param bundle the bundle to do the lookup
	 * @param fullPattern matching pattern
	 * @param dir directory inside the bundle
	 * @param result set of results (used to concatenate matching sub dirs)
	 * @param searchType the search strategy to use
	 * @throws IOException
	 */
	private void doRetrieveMatchingBundleEntries(Bundle bundle, String fullPattern, String dir, Set result,
			int searchType) throws IOException {

		Enumeration candidates;

		switch (searchType) {
			case OsgiResourceUtils.PREFIX_TYPE_NOT_SPECIFIED:
			case OsgiResourceUtils.PREFIX_TYPE_BUNDLE_SPACE:
				// returns an enumeration of URLs
				candidates = bundle.findEntries(dir, null, false);
				break;
			case OsgiResourceUtils.PREFIX_TYPE_BUNDLE_JAR:
				// returns an enumeration of Strings
				candidates = bundle.getEntryPaths(dir);
				break;
			case OsgiResourceUtils.PREFIX_TYPE_CLASS_SPACE:
				// returns an enumeration of URLs
				throw new IllegalArgumentException("class space does not support pattern matching");
			default:
				throw new IllegalArgumentException("unknown searchType " + searchType);
		}

		// entries are relative to the root path - miss the leading /
		if (candidates != null) {
			boolean dirDepthNotFixed = (fullPattern.indexOf(FOLDER_WILDCARD) != -1);
			while (candidates.hasMoreElements()) {
				Object path = candidates.nextElement();
				String currPath;

				if (path instanceof String)
					currPath = handleString((String) path);
				else
					currPath = handleURL((URL) path);

				if (!currPath.startsWith(dir)) {
					// Returned resource path does not start with relative
					// directory:
					// assuming absolute path returned -> strip absolute path.
					int dirIndex = currPath.indexOf(dir);
					if (dirIndex != -1) {
						currPath = currPath.substring(dirIndex);
					}
				}

				if (currPath.endsWith(FOLDER_SEPARATOR)
						&& (dirDepthNotFixed || StringUtils.countOccurrencesOf(currPath, FOLDER_SEPARATOR) < StringUtils.countOccurrencesOf(
							fullPattern, FOLDER_SEPARATOR))) {
					// Search subdirectories recursively: we manually get the
					// folders on only one level

					doRetrieveMatchingBundleEntries(bundle, fullPattern, currPath, result, searchType);
				}
				if (getPathMatcher().match(fullPattern, currPath)) {
					if (path instanceof URL)
						result.add(new UrlContextResource((URL) path, currPath));
					else
						result.add(new OsgiBundleResource(bundle, currPath));

				}
			}
		}
	}

	/**
	 * Handles candidates returned as URLs.
	 * 
	 * @param path
	 * @return
	 */
	private String handleURL(URL path) {
		return path.getPath();
	}

	/**
	 * Handles candidates returned as Strings.
	 * 
	 * @param path
	 * @return
	 */
	private String handleString(String path) {
		return FOLDER_SEPARATOR.concat(path);
	}
}
