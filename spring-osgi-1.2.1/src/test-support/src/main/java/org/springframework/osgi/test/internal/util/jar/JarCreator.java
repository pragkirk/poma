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

package org.springframework.osgi.test.internal.util.jar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.osgi.test.internal.util.jar.storage.MemoryStorage;
import org.springframework.osgi.test.internal.util.jar.storage.Storage;
import org.springframework.util.StringUtils;

/**
 * Helper class for creating Jar files. Note that this class is stateful and the
 * same instance should not be reused.
 * 
 * @author Costin Leau
 * 
 */
public class JarCreator {

	private static final Log log = LogFactory.getLog(JarCreator.class);

	public static final String CLASS_PATTERN = "/**/*.class";

	public static final String XML_PATTERN = "/**/*.xml";

	public static final String PROPS_PATTERN = "/**/*.properties";

	public static final String EVERYTHING_PATTERN = "/**/*";

	private static final String[] LIMITED_PATTERN = new String[] { CLASS_PATTERN, XML_PATTERN, PROPS_PATTERN };

	private static final String CLASS_EXT = ".class";

	private static final String TEST_CLASSES_DIR = "test-classes";

	private String[] contentPattern = new String[] { EVERYTHING_PATTERN };

	private ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();

	private Storage storage = new MemoryStorage();

	private String rootPath = determineRootPath();

	private boolean addFolders = true;

	/** collection of packages contained by this jar */
	private Collection containedPackages = new TreeSet();


	/**
	 * Resources' root path (the root path does not become part of the jar).
	 * 
	 * @return the root path
	 */
	public String determineRootPath() {
		// load file using absolute path. This seems to be necessary in IntelliJ
		try {
			ResourceLoader fileLoader = new DefaultResourceLoader();
			Resource res = fileLoader.getResource(getClass().getName().replace('.', '/').concat(".class"));
			String fileLocation = "file://" + res.getFile().getAbsolutePath();
			fileLocation = fileLocation.substring(0, fileLocation.indexOf(TEST_CLASSES_DIR)) + TEST_CLASSES_DIR;
			if (res.exists()) {
				return fileLocation;
			}
		}
		catch (Exception e) {
		}

		return "file:./target/" + TEST_CLASSES_DIR;
	}

	/**
	 * Actual jar creation.
	 * 
	 * @param manifest to use
	 * @param content array of resource to include in the jar
	 * @return the number of bytes written to the underlying stream.
	 * 
	 * @throws IOException
	 */
	protected int addJarContent(Manifest manifest, Map entries) throws IOException {
		// load manifest
		// add it to the jar
		if (log.isTraceEnabled()) {
			if (manifest != null)
				log.trace("Adding MANIFEST.MF [" + manifest.getMainAttributes().entrySet() + "]");
			log.trace("Adding entries:");
			Set key = entries.keySet();
			for (Iterator iter = key.iterator(); iter.hasNext();) {
				log.trace(iter.next());
			}
		}

		return JarUtils.createJar(manifest, entries, storage.getOutputStream());
	}

	/**
	 * Create a jar using the current settings and return a {@link Resource}
	 * pointing to the jar.
	 * 
	 * @param manifest
	 */
	public Resource createJar(Manifest manifest) {
		return createJar(manifest, resolveContent());
	}

	/**
	 * Create a jar using the current settings and return a {@link Resource}
	 * pointing to the jar.
	 * 
	 * @param manifest
	 */
	public Resource createJar(Manifest manifest, Map content) {
		try {
			addJarContent(manifest, content);
			return storage.getResource();
		}
		catch (IOException ex) {
			throw (RuntimeException) new IllegalStateException("Cannot create jar").initCause(ex);
		}
	}

	/**
	 * Small utility method used for determining the file name by striping the
	 * root path from the file full path.
	 * 
	 * @param rootPath
	 * @param resource
	 * @return
	 */
	private String determineRelativeName(String rootPath, Resource resource) {
		try {
			String path = StringUtils.cleanPath(resource.getURL().toExternalForm());
			return path.substring(path.indexOf(rootPath) + rootPath.length());
		}
		catch (IOException ex) {
			throw (RuntimeException) new IllegalArgumentException("illegal resource " + resource.toString()).initCause(ex);
		}
	}

	/**
	 * Transform the pattern and rootpath into actual resources.
	 * 
	 * @return
	 * @throws Exception
	 */
	private Resource[][] resolveResources() {
		ResourcePatternResolver resolver = getPatternResolver();

		String[] patterns = getContentPattern();
		Resource[][] resources = new Resource[patterns.length][];

		// transform Strings into Resources
		for (int i = 0; i < patterns.length; i++) {
			StringBuffer buffer = new StringBuffer(rootPath);

			// do checking on lost slashes
			if (!rootPath.endsWith(JarUtils.SLASH) && !patterns[i].startsWith(JarUtils.SLASH))
				buffer.append(JarUtils.SLASH);

			buffer.append(patterns[i]);
			try {
				resources[i] = resolver.getResources(buffer.toString());
			}
			catch (IOException ex) {
				IllegalStateException re = new IllegalStateException("cannot resolve pattern " + buffer.toString());
				re.initCause(ex);
				throw re;
			}
		}

		return resources;
	}

	/**
	 * Resolve the jar content based on its path. Will return a map containing
	 * the entries relative to the jar root path as keys and Spring Resource
	 * pointing to the actual resources as values. It will also determine the
	 * packages contained by this package.
	 * 
	 * @return
	 */
	public Map resolveContent() {
		Resource[][] resources = resolveResources();

		URL rootURL;
		String rootP = getRootPath();
		try {
			rootURL = new URL(rootP);
		}
		catch (MalformedURLException ex) {
			throw (RuntimeException) new IllegalArgumentException("illegal root path given " + rootP).initCause(ex);
		}
		String rootPath = StringUtils.cleanPath(rootURL.getPath());

		// remove duplicates
		Map entries = new TreeMap();
		// save contained bundle packages
		containedPackages.clear();

		// empty stream used for folders
		Resource folderResource = new ByteArrayResource(new byte[0]);

		// add folder entries also
		for (int i = 0; i < resources.length; i++) {
			for (int j = 0; j < resources[i].length; j++) {
				String relativeName = determineRelativeName(rootPath, resources[i][j]);
				// be consistent when adding resources to jar
				if (!relativeName.startsWith("/"))
					relativeName = "/" + relativeName;
				entries.put(relativeName, resources[i][j]);

				// look for class entries
				if (relativeName.endsWith(CLASS_EXT)) {

					// determine package (exclude first char)
					String clazzName = relativeName.substring(1, relativeName.length() - CLASS_EXT.length()).replace(
						'/', '.');
					// remove class name
					int index = clazzName.lastIndexOf('.');
					if (index > 0)
						clazzName = clazzName.substring(0, index);
					// add it to the collection
					containedPackages.add(clazzName);
				}

				String token = relativeName;
				// get folder and walk up to the root
				if (addFolders) {
					// add META-INF
					entries.put("/META-INF/", folderResource);
					int slashIndex;
					// stop at root folder
					while ((slashIndex = token.lastIndexOf('/')) > 1) {
						// add the folder with trailing /
						entries.put(token.substring(0, slashIndex + 1), folderResource);
						// walk the tree
						token = token.substring(0, slashIndex);
					}
					// add root folder
					//entries.put("/", folderResource);
				}
			}
		}

		if (log.isTraceEnabled())
			log.trace("The following packages were discovered in the bundle: " + containedPackages);

		return entries;
	}

	public Collection getContainedPackages() {
		return containedPackages;
	}

	/**
	 * @return Returns the contentPattern.
	 */
	public String[] getContentPattern() {
		return contentPattern;
	}

	/**
	 * Pattern for content matching. Note that using {@link #EVERYTHING_PATTERN}
	 * can become problematic on windows due to file system locking.
	 * 
	 * @param contentPattern The contentPattern to set.
	 */
	public void setContentPattern(String[] contentPattern) {
		this.contentPattern = contentPattern;
	}

	/**
	 * @return Returns the patternResolver.
	 */
	public ResourcePatternResolver getPatternResolver() {
		return patternResolver;
	}

	/**
	 * @param patternResolver The patternResolver to set.
	 */
	public void setPatternResolver(ResourcePatternResolver patternResolver) {
		this.patternResolver = patternResolver;
	}

	/**
	 * @return Returns the jarStorage.
	 */
	public Storage getStorage() {
		return storage;
	}

	/**
	 * @param jarStorage The jarStorage to set.
	 */
	public void setStorage(Storage jarStorage) {
		this.storage = jarStorage;
	}

	/**
	 * @param
	 */
	public String getRootPath() {
		return rootPath;
	}

	/**
	 * @param rootPath The rootPath to set.
	 */
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	/**
	 * @return Returns the addFolders.
	 */
	public boolean isAddFolders() {
		return addFolders;
	}

	/**
	 * Whether the folders in which the files reside, should be added to the
	 * archive. Default is true since otherwise, the archive will contains only
	 * files and no folders.
	 * 
	 * @param addFolders The addFolders to set.
	 */
	public void setAddFolders(boolean addFolders) {
		this.addFolders = addFolders;
	}
}
