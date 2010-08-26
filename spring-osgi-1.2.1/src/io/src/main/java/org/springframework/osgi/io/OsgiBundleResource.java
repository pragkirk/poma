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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.osgi.io.internal.OsgiResourceUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Resource implementation for OSGi environments.
 * 
 * <p/> Lazy evaluation of the resource will be used.
 * 
 * This implementation allows resource location inside:
 * 
 * <ul>
 * <li><em>bundle space</em> - if <code>osgibundle:</code>/{@link #BUNDLE_URL_PREFIX}
 * prefix is being used or none is specified. This space cotnains the bundle jar
 * and its attached fragments.</li>
 * <li><em>bundle jar</em> - if <code>osgibundlejar:</code>/{@link #BUNDLE_JAR_URL_PREFIX}
 * is specified. This space contains just the bundle jar.</li>
 * <li><em>class space</em> - if
 * {@link org.springframework.util.ResourceUtils#CLASSPATH_URL_PREFIX} is
 * encountered. This space contains the bundle classpath, namely the bundle jar,
 * its attached fragments and imported packages.</li>
 * </ul>
 * 
 * For more explanations on resource locations in OSGi, please see the
 * <em>Access to Resources</em> chapter from the OSGi spec.
 * 
 * <p/> OSGi framework specific prefixes (such as <code>bundleentry:</code>
 * and <code>bundleresource:</code>under Equinox, <code>bundle:</code>
 * under Knopflefish and Felix, etc..) are supported. Resources outside the OSGi
 * space (<code>file:</code>, <code>http:</code>, etc..) are supported as
 * well as the path is being resolved to an <code>URL</code>.
 * 
 * <p/>If no prefix is specified, the <em>bundle space</em> will be used for
 * locating a resource.
 * 
 * <p/> <strong>Note:</strong> When the <em>bundle space</em> (bundle jar and
 * its attached fragments) is being searched, multiple URLs can be found but
 * this implementation will return only the first one. Consider using
 * {@link OsgiBundleResourcePatternResolver} to retrieve all entries.
 * 
 * @author Costin Leau
 * @author Adrian Colyer
 * @author Sam Brannen
 */
public class OsgiBundleResource extends AbstractResource implements ContextResource {

	/**
	 * Prefix for searching inside the owning bundle space. This translates to
	 * searching the bundle and its attached fragments. If no prefix is
	 * specified, this one will be used.
	 */
	public static final String BUNDLE_URL_PREFIX = "osgibundle:";

	/**
	 * Prefix for searching only the bundle raw jar. Will ignore attached
	 * fragments. Not used at the moment.
	 */
	public static final String BUNDLE_JAR_URL_PREFIX = "osgibundlejar:";

	private static final char PREFIX_SEPARATOR = ':';

	private static final String ABSOLUTE_PATH_PREFIX = "/";

	private final Bundle bundle;

	private final String path;

	// used to avoid removing the prefix every time the URL is required
	private final String pathWithoutPrefix;

	// Bundle resource possible searches
	private int searchType = OsgiResourceUtils.PREFIX_TYPE_NOT_SPECIFIED;


	/**
	 * 
	 * Constructs a new <code>OsgiBundleResource</code> instance.
	 * 
	 * @param bundle OSGi bundle used by this resource
	 * @param path resource path inside the bundle.
	 */
	public OsgiBundleResource(Bundle bundle, String path) {
		Assert.notNull(bundle, "Bundle must not be null");
		this.bundle = bundle;

		// check path
		Assert.notNull(path, "Path must not be null");

		this.path = StringUtils.cleanPath(path);

		this.searchType = OsgiResourceUtils.getSearchType(this.path);

		switch (this.searchType) {
			case OsgiResourceUtils.PREFIX_TYPE_NOT_SPECIFIED:
				pathWithoutPrefix = path;
				break;
			case OsgiResourceUtils.PREFIX_TYPE_BUNDLE_SPACE:
				pathWithoutPrefix = path.substring(BUNDLE_URL_PREFIX.length());
				break;
			case OsgiResourceUtils.PREFIX_TYPE_BUNDLE_JAR:
				pathWithoutPrefix = path.substring(BUNDLE_JAR_URL_PREFIX.length());
				break;
			case OsgiResourceUtils.PREFIX_TYPE_CLASS_SPACE:
				pathWithoutPrefix = path.substring(ResourceLoader.CLASSPATH_URL_PREFIX.length());
				break;
			// prefix unknown so the path will be resolved outside the context
			default:
				pathWithoutPrefix = null;
		}
	}

	/**
	 * Returns the path for this resource.
	 * 
	 * @return this resource path
	 */
	final String getPath() {
		return path;
	}

	/**
	 * Returns the bundle for this resource.
	 * 
	 * @return the resource bundle
	 */
	final Bundle getBundle() {
		return bundle;
	}

	/**
	 * Returns an <code>InputStream</code> to this resource. This
	 * implementation opens an
	 * <code>InputStream<code> for the given <code>URL</code>. It sets the
	 * <em>UseCaches</em> flag to <code>false</code>, mainly to avoid jar file
	 * locking on Windows.
	 * 
	 * @return input stream to the underlying resource
	 * @throws IOException if the stream could not be opened
	 * @see java.net.URL#openConnection()
	 * @see java.net.URLConnection#setUseCaches(boolean)
	 * @see java.net.URLConnection#getInputStream()
	 *
	 */
	public InputStream getInputStream() throws IOException {
		URLConnection con = getURL().openConnection();
		con.setUseCaches(false);
		return con.getInputStream();
	}

	/**
	 * Locates the resource in the underlying bundle based on the prefix, if it
	 * exists. Note that the location happens per call since due to the dynamic
	 * nature of OSGi, the classpath of the bundle (among others) can change
	 * during a bundle lifecycle (depending on its imports).
	 * 
	 * @return URL to this resource
	 * @throws IOException if the resource cannot be resolved as URL, i.e. if
	 *         the resource is not available as descriptor
	 * 
	 * @see org.osgi.framework.Bundle#getEntry(String)
	 * @see org.osgi.framework.Bundle#getResource(String)
	 */
	public URL getURL() throws IOException {
		ContextResource res = null;
		URL url = null;

		switch (searchType) {
			// same as bundle space but with a different string
			case OsgiResourceUtils.PREFIX_TYPE_NOT_SPECIFIED:
				res = getResourceFromBundleSpace(pathWithoutPrefix);
				break;
			case OsgiResourceUtils.PREFIX_TYPE_BUNDLE_SPACE:
				res = getResourceFromBundleSpace(pathWithoutPrefix);
				break;
			case OsgiResourceUtils.PREFIX_TYPE_BUNDLE_JAR:
				url = getResourceFromBundleJar(pathWithoutPrefix);
				break;
			case OsgiResourceUtils.PREFIX_TYPE_CLASS_SPACE:
				url = getResourceFromBundleClasspath(pathWithoutPrefix);
				break;
			// fallback
			default:
				// just try to convert it to an URL
				url = new URL(path);
				break;
		}

		if (res != null) {
			url = res.getURL();
		}

		if (url == null) {
			throw new FileNotFoundException(getDescription() + " cannot be resolved to URL because it does not exist");
		}

		return url;
	}

	/**
	 * Resolves a resource from *the bundle space* only. Only the bundle and its
	 * attached fragments are searched for the given resource. Note that this
	 * method returns only the first URL found, discarding the rest. To retrieve
	 * the entire set, consider using {@link OsgiBundleResourcePatternResolver}.
	 * 
	 * @param bundlePath the path to resolve
	 * @return a URL to the returned resource or null if none is found
	 * @throws IOException
	 * 
	 * @see {@link org.osgi.framework.Bundle#findEntries(String, String, boolean)}
	 */
	ContextResource getResourceFromBundleSpace(String bundlePath) throws IOException {
		ContextResource[] res = getAllUrlsFromBundleSpace(bundlePath);
		return (ObjectUtils.isEmpty(res) ? null : res[0]);
	}

	/**
	 * Resolves a resource from the *bundle jar* only. Only the bundle jar is
	 * searched (its attached fragments are ignored).
	 * 
	 * @param bundlePath the path to resolve
	 * @return URL to the specified path or null if none is found
	 * @throws IOException
	 * 
	 * @see {@link Bundle#getEntry(String)}
	 */
	URL getResourceFromBundleJar(String bundlePath) throws IOException {
		return bundle.getEntry(bundlePath);
	}

	/**
	 * Resolves a resource from the bundle's classpath. This will find resources
	 * in this bundle and also in imported packages from other bundles.
	 * 
	 * @param bundlePath
	 * @return a URL to the returned resource or null if none is found
	 * 
	 * @see org.osgi.framework.Bundle#getResource(String)
	 */
	URL getResourceFromBundleClasspath(String bundlePath) {
		return bundle.getResource(bundlePath);
	}

	/**
	 * Determine if the given path is relative or absolute.
	 * 
	 * @param locationPath
	 * @return
	 */
	boolean isRelativePath(String locationPath) {
		return ((locationPath.indexOf(PREFIX_SEPARATOR) == -1) && !locationPath.startsWith(ABSOLUTE_PATH_PREFIX));
	}

	/**
	 * Returns a resource relative to this resource. This implementation creates
	 * an <code>OsgiBundleResource</code>, applying the given path relative
	 * to the path of the underlying resource of this descriptor.
	 * 
	 * @param relativePath the relative path (relative to this resource)
	 * @return the resource handle for the relative resource
	 * @throws IOException if the relative resource cannot be determined
	 * @see org.springframework.util.StringUtils#applyRelativePath(String,
	 *      String)
	 */
	public Resource createRelative(String relativePath) {
		String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
		return new OsgiBundleResource(this.bundle, pathToUse);
	}

	/**
	 * Returns the filename of this resources. This implementation returns the
	 * name of the file that this bundle path resource refers to.
	 * 
	 * @return resource filename
	 * @see org.springframework.util.StringUtils#getFilename(String)
	 */
	public String getFilename() {
		return StringUtils.getFilename(this.path);
	}

	/**
	 * Returns a <code>File</code> handle for this resource. This method does
	 * a best-effort attempt to locate the bundle resource on the file system.
	 * It is strongly recommended to use {@link #getInputStream()} method
	 * instead which works no matter if the bundles are saved (in exploded form
	 * or not) on the file system.
	 * 
	 * @return File handle to this resource
	 * @throws IOException if the resource cannot be resolved as absolute file
	 *         path, i.e. if the resource is not available in a file system
	 */
	public File getFile() throws IOException {
		// locate the file inside the bundle only known prefixes
		if (searchType != OsgiResourceUtils.PREFIX_TYPE_UNKNOWN) {
			String bundleLocation = bundle.getLocation();
			int prefixIndex = bundleLocation.indexOf(ResourceUtils.FILE_URL_PREFIX);
			if (prefixIndex > -1) {
				bundleLocation = bundleLocation.substring(prefixIndex + ResourceUtils.FILE_URL_PREFIX.length());
			}
			File file = new File(bundleLocation, path);
			if (file.exists()) {
				return file;
			}
			// fall back to the URL discovery (just in case)
		}

		try {
			return ResourceUtils.getFile(getURI(), getDescription());
		}
		catch (IOException ioe) {
			throw (IOException) new FileNotFoundException(getDescription()
					+ " cannot be resolved to absolute file path").initCause(ioe);
		}
	}

	/**
	 * <p/> This implementation returns a description that includes the bundle
	 * location.
	 */
	public String getDescription() {
		StringBuffer buf = new StringBuffer();
		buf.append("OSGi resource[");
		buf.append(this.path);
		buf.append("|bnd.id=");
		buf.append(bundle.getBundleId());
		buf.append("|bnd.sym=");
		buf.append(bundle.getSymbolicName());
		buf.append("]");

		return buf.toString();
	}

	/**
	 * <p/> This implementation compares the underlying bundle and path
	 * locations.
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof OsgiBundleResource) {
			OsgiBundleResource otherRes = (OsgiBundleResource) obj;
			return (this.path.equals(otherRes.path) && ObjectUtils.nullSafeEquals(this.bundle, otherRes.bundle));
		}
		return false;
	}

	/**
	 * <p/> This implementation returns the hash code of the underlying class
	 * path location.
	 */
	public int hashCode() {
		return this.path.hashCode();
	}

	public long lastModified() throws IOException {
		URLConnection con = getURL().openConnection();
		con.setUseCaches(false);
		long time = con.getLastModified();
		// the implementation doesn't return the proper time stamp
		if (time == 0) {
			if (OsgiResourceUtils.PREFIX_TYPE_BUNDLE_JAR == searchType)
				return bundle.getLastModified();
		}
		// there is nothing else we can do
		return time;
	}

	/**
	 * @return Returns the searchType.
	 */
	int getSearchType() {
		return searchType;
	}

	/**
	 * Used internally to get all the URLs matching a certain location. The
	 * method is required to extract the folder from the given location as well
	 * the file.
	 * 
	 * @param location location to look for
	 * @return an array of URLs
	 * @throws IOException
	 */
	ContextResource[] getAllUrlsFromBundleSpace(String location) throws IOException {
		if (bundle == null)
			throw new IllegalArgumentException(
				"cannot locate items in bundle-space w/o a bundle; specify one when creating this resolver");

		Assert.notNull(location);
		Set resources = new LinkedHashSet(5);

		location = StringUtils.cleanPath(location);
		location = OsgiResourceUtils.stripPrefix(location);

		if (!StringUtils.hasText(location))
			location = OsgiResourceUtils.FOLDER_DELIMITER;

		// the root folder is requested (special case)
		if (OsgiResourceUtils.FOLDER_DELIMITER.equals(location)) {
			// there is no way to determine the URL to the root directly
			// through findEntries so we'll have to use another way

			// getEntry can't be used since it doesn't consider fragments
			// so we have to rely on findEntries 

			// we could ask for a known entry (such as META-INF)
			// but not all jars have a dedicated entry for it
			// so we'll just ask for whatever is present in the root
			Enumeration candidates = bundle.findEntries("/", null, false);

			// since there can be multiple root paths (when fragments are present)
			// iterate on all candidates
			while (candidates != null && candidates.hasMoreElements()) {

				URL url = (URL) candidates.nextElement();

				// determined the root path
				// we'll have to parse the string since some implementations
				// do not normalize the resulting URL resulting in mismatches
				String rootPath = OsgiResourceUtils.findUpperFolder(url.toExternalForm());
				resources.add(new UrlContextResource(rootPath));
			}
		}
		else {
			// remove leading and trailing / if any
			if (location.startsWith(OsgiResourceUtils.FOLDER_DELIMITER))
				location = location.substring(1);

			if (location.endsWith(OsgiResourceUtils.FOLDER_DELIMITER))
				location = location.substring(0, location.length() - 1);

			// do we have at least on folder or is this just a file
			boolean hasFolder = (location.indexOf(OsgiResourceUtils.FOLDER_DELIMITER) != -1);

			String path = (hasFolder ? location : OsgiResourceUtils.FOLDER_DELIMITER);
			String file = (hasFolder ? null : location);

			// find the file and path
			int separatorIndex = location.lastIndexOf(OsgiResourceUtils.FOLDER_DELIMITER);

			if (separatorIndex > -1 && separatorIndex + 1 < location.length()) {
				// update the path
				path = location.substring(0, separatorIndex);

				// determine file (if there is any)
				if (separatorIndex + 1 < location.length())
					file = location.substring(separatorIndex + 1);
			}

			Enumeration candidates = bundle.findEntries(path, file, false);
			// add the leading / to be consistent
			String contextPath = OsgiResourceUtils.FOLDER_DELIMITER + location;

			while (candidates != null && candidates.hasMoreElements()) {
				resources.add(new UrlContextResource((URL) candidates.nextElement(), contextPath));
			}
		}

		return (ContextResource[]) resources.toArray(new ContextResource[resources.size()]);
	}

	// TODO: can this return null or throw an exception
	public String getPathWithinContext() {
		return pathWithoutPrefix;
	}

	/**
	 * Return whether this resource actually exists in physical form.
	 * <p>
	 * This method performs a definitive existence check, whereas the existence
	 * of a <code>Resource</code> handle only guarantees a valid descriptor
	 * handle.
	 * 
	 * <p/>The existence check is done by opening an InputStream to the
	 * underlying resource (overriding the default implementation which checks
	 * first for the presence of a File).
	 */
	public boolean exists() {
		try {
			InputStream is = getInputStream();
			is.close();
			return true;
		}
		catch (Throwable isEx) {
			return false;
		}
	}
}
