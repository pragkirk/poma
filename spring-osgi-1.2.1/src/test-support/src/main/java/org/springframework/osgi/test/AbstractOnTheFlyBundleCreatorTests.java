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

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.objectweb.asm.ClassReader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.osgi.test.internal.util.DependencyVisitor;
import org.springframework.osgi.test.internal.util.jar.JarCreator;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Enhanced subclass of {@link AbstractDependencyManagerTests} that facilitates
 * OSGi testing by creating at runtime, on the fly, a jar using the indicated
 * manifest and resource patterns (by default all files found under the root
 * path).
 * 
 * <p/>The test class can automatically determine the imports required by the
 * test, create the OSGi bundle manifest and pack the test and its resources in
 * a jar that can be installed inside an OSGi platform.
 * 
 * <p/>Additionally, a valid OSGi manifest is automatically created for the
 * resulting test if the user does not provide one. The classes present in the
 * archive are analyzed and based on their byte-code, the required
 * <code>Import-Package</code> entries (for packages not found in the bundle)
 * are created.
 * 
 * Please see the reference documentation for an in-depth explanation and usage
 * examples.
 * 
 * <p/>Note that in more complex scenarios, dedicated packaging tools (such as
 * ant scripts or maven2) should be used.
 * 
 * <p/>It is recommend to extend {@link AbstractConfigurableBundleCreatorTests}
 * rather then this class as the former offers sensible defaults.
 * 
 * @author Costin Leau
 * 
 */
public abstract class AbstractOnTheFlyBundleCreatorTests extends AbstractDependencyManagerTests {

	private static final String META_INF_JAR_LOCATION = "/META-INF/MANIFEST.MF";

	JarCreator jarCreator;

	/** field used for caching jar content */
	private Map jarEntries;
	/** discovered manifest */
	private Manifest manifest;


	public AbstractOnTheFlyBundleCreatorTests() {
		initializeJarCreator();
	}

	public AbstractOnTheFlyBundleCreatorTests(String testName) {
		super(testName);
		initializeJarCreator();
	}

	private void initializeJarCreator() {
		AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				jarCreator = new JarCreator();
				return null;
			}
		});
	}

	/**
	 * Returns the root path used for locating the resources that will be packed
	 * in the test bundle (the root path does not become part of the jar).
	 * <p/>By default, the Maven2 test layout is used:
	 * <code>"file:./target/test-classes"</code>
	 * 
	 * @return root path given as a String
	 */
	protected String getRootPath() {
		return "file:./target/test-classes/";
	}

	/**
	 * Returns the patterns used for identifying the resources added to the jar.
	 * The patterns are added to the root path when performing the search. By
	 * default, the pattern is <code>*&#42;/*</code>.
	 * 
	 * <p/>In large test environments, performance can be improved by limiting
	 * the resource added to the bundle by selecting only certain packages or
	 * classes. This results in a small test bundle which is faster to create,
	 * deploy and install.
	 * 
	 * @return the patterns identifying the resources added to the jar
	 */
	protected String[] getBundleContentPattern() {
		return new String[] { JarCreator.EVERYTHING_PATTERN };
	}

	/**
	 * Returns the location (in Spring resource style) of the manifest location
	 * to be used. By default <code>null</code> is returned, indicating that
	 * the manifest should be picked up from the bundle content (if it's
	 * available) or be automatically created based on the test class imports.
	 * 
	 * @return the manifest location
	 * @see #getManifest()
	 * @see #createDefaultManifest()
	 */
	protected String getManifestLocation() {
		return null;
	}

	/**
	 * Returns the current test bundle manifest. The method tries to read the
	 * manifest from the given location; in case the location is
	 * <code>null</code> (default), it will search for
	 * <code>META-INF/MANIFEST.MF</code> file in jar content (as specified
	 * through the patterns) and, if it cannot find the file,
	 * <em>automatically</em> create a <code>Manifest</code> object
	 * containing default entries.
	 * 
	 * <p/> Subclasses can override this method to enhance the returned
	 * Manifest.
	 * 
	 * @return Manifest used for this test suite.
	 * 
	 * @see #createDefaultManifest()
	 */
	protected Manifest getManifest() {
		// return cached manifest
		if (manifest != null)
			return manifest;

		String manifestLocation = getManifestLocation();
		if (StringUtils.hasText(manifestLocation)) {
			logger.info("Using Manifest from specified location=[" + getManifestLocation() + "]");
			DefaultResourceLoader loader = new DefaultResourceLoader();
			manifest = createManifestFrom(loader.getResource(manifestLocation));
		}

		else {
			// set root path
			jarCreator.setRootPath(getRootPath());
			// add the content pattern
			jarCreator.setContentPattern(getBundleContentPattern());

			// see if the manifest already exists in the classpath
			// to resolve the patterns
			jarEntries = jarCreator.resolveContent();

			for (Iterator iterator = jarEntries.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				if (META_INF_JAR_LOCATION.equals(entry.getKey())) {
					logger.info("Using Manifest from the test bundle content=[/META-INF/MANIFEST.MF]");
					manifest = createManifestFrom((Resource) entry.getValue());
				}
			}
			// fallback to default manifest creation

			if (manifest == null) {
				logger.info("Automatically creating Manifest for the test bundle");
				manifest = createDefaultManifest();
			}
		}

		return manifest;
	}

	/**
	 * Indicates if the automatic manifest creation should consider only the
	 * test class (<code>true</code>) or all classes included in the test
	 * bundle(<code>false</code>). The latter should be used when the test
	 * bundle contains additional classes that help with the test case.
	 * 
	 * <p/> By default, this method returns <code>true</code>, meaning that
	 * only the test class will be searched for dependencies.
	 * 
	 * @return true if only the test hierarchy is searched for dependencies or
	 *         false if all classes discovered in the test archive need to be
	 *         parsed.
	 */
	protected boolean createManifestOnlyFromTestClass() {
		return true;
	}

	private Manifest createManifestFrom(Resource resource) {
		Assert.notNull(resource, "unable to create manifest for empty resources");
		try {
			return new Manifest(resource.getInputStream());
		}
		catch (IOException ex) {
			throw (RuntimeException) new IllegalArgumentException("cannot create manifest from " + resource).initCause(ex);
		}
	}

	/**
	 * Creates the default manifest in case none if found on the disk. By
	 * default, the imports are synthetised based on the test class bytecode.
	 * 
	 * @return default manifest for the jar created on the fly
	 */
	protected Manifest createDefaultManifest() {
		Manifest manifest = new Manifest();
		Attributes attrs = manifest.getMainAttributes();

		// manifest versions
		attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		attrs.putValue(Constants.BUNDLE_MANIFESTVERSION, "2");

		String description = getName() + "-" + getClass().getName();
		// name/description
		attrs.putValue(Constants.BUNDLE_NAME, "TestBundle-" + description);
		attrs.putValue(Constants.BUNDLE_SYMBOLICNAME, "TestBundle-" + description);
		attrs.putValue(Constants.BUNDLE_DESCRIPTION, "on-the-fly test bundle");

		// activator
		attrs.putValue(Constants.BUNDLE_ACTIVATOR, JUnitTestActivator.class.getName());

		// add Import-Package entry
		addImportPackage(manifest);

		if (logger.isDebugEnabled())
			logger.debug("Created manifest:" + manifest.getMainAttributes().entrySet());
		return manifest;
	}

	private void addImportPackage(Manifest manifest) {
		String[] rawImports = determineImports();

		boolean trace = logger.isTraceEnabled();

		if (trace)
			logger.trace("Discovered raw imports " + ObjectUtils.nullSafeToString(rawImports));

		Collection specialImportsOut = eliminateSpecialPackages(rawImports);
		Collection imports = eliminatePackagesAvailableInTheJar(specialImportsOut);

		if (trace)
			logger.trace("Filtered imports are " + imports);

		manifest.getMainAttributes().putValue(Constants.IMPORT_PACKAGE,
			StringUtils.collectionToCommaDelimitedString(imports));
	}

	/**
	 * Eliminate 'special' packages (java.*, test framework internal and the
	 * class declaring package)
	 * 
	 * @param rawImports
	 * @return
	 */
	private Collection eliminateSpecialPackages(String[] rawImports) {
		String currentPckg = ClassUtils.classPackageAsResourcePath(getClass()).replace('/', '.');

		Set filteredImports = new LinkedHashSet(rawImports.length);
		Set eliminatedImports = new LinkedHashSet(4);

		for (int i = 0; i < rawImports.length; i++) {
			String pckg = rawImports[i];

			if (!(pckg.startsWith("java.") || pckg.startsWith("org.springframework.osgi.test.internal") || pckg.equals(currentPckg)))
				filteredImports.add(pckg);
			else
				eliminatedImports.add(pckg);
		}

		if (!eliminatedImports.isEmpty() && logger.isTraceEnabled())
			logger.trace("Eliminated special packages " + eliminatedImports);

		return filteredImports;
	}

	/**
	 * Eliminates imports for packages already included in the bundle. Works
	 * only if the jar content is known (variable 'jarEntries' set).
	 * 
	 * @param imports
	 * @return
	 */
	private Collection eliminatePackagesAvailableInTheJar(Collection imports) {
		// no jar entry present, bail out.
		if (jarEntries == null || jarEntries.isEmpty())
			return imports;

		Set filteredImports = new LinkedHashSet(imports.size());
		Collection eliminatedImports = new LinkedHashSet(2);

		Collection jarPackages = jarCreator.getContainedPackages();
		for (Iterator iterator = imports.iterator(); iterator.hasNext();) {
			String pckg = (String) iterator.next();
			if (jarPackages.contains(pckg))
				eliminatedImports.add(pckg);
			else
				filteredImports.add(pckg);
		}
		if (!eliminatedImports.isEmpty() && logger.isTraceEnabled())
			logger.trace("Eliminated packages already present in the bundle " + eliminatedImports);

		return filteredImports;
	}

	/**
	 * Determine imports for the given bundle. Based on the user settings, this
	 * method will consider only the the test hierarchy until the testing
	 * framework is found or all classes available inside the test bundle. <p/>
	 * Note that split packages are not supported.
	 * 
	 * @return
	 */
	private String[] determineImports() {

		boolean useTestClassOnly = false;

		// no jar entry present, bail out.
		if (jarEntries == null || jarEntries.isEmpty()) {
			logger.debug("No test jar content detected, generating bundle imports from the test class");
			useTestClassOnly = true;
		}

		else if (createManifestOnlyFromTestClass()) {
			logger.info("Using the test class for generating bundle imports");
			useTestClassOnly = true;
		}
		else
			logger.info("Using all classes in the jar for the generation of bundle imports");

		// className, class resource
		Map entries;

		if (useTestClassOnly) {

			entries = new LinkedHashMap(4);

			// get current class (test class that bootstraps the OSGi infrastructure)
			Class clazz = getClass();
			String clazzPackage = null;
			String endPackage = AbstractOnTheFlyBundleCreatorTests.class.getPackage().getName();

			do {

				// consider inner classes as well
				List classes = new ArrayList(4);
				classes.add(clazz);
				CollectionUtils.mergeArrayIntoCollection(clazz.getDeclaredClasses(), classes);

				for (Iterator iterator = classes.iterator(); iterator.hasNext();) {
					Class classToInspect = (Class) iterator.next();

					Package pkg = classToInspect.getPackage();
					if (pkg != null) {
						clazzPackage = pkg.getName();
						String classFile = ClassUtils.getClassFileName(classToInspect);
						entries.put(classToInspect.getName().replace('.', '/').concat(ClassUtils.CLASS_FILE_SUFFIX),
							new InputStreamResource(classToInspect.getResourceAsStream(classFile)));
					}
					// handle default package
					else {
						logger.warn("Could not find package for class " + classToInspect + "; ignoring...");
					}
				}

				clazz = clazz.getSuperclass();

			} while (!endPackage.equals(clazzPackage));
		}
		else
			entries = jarEntries;

		return determineImportsFor(entries);

	}

	private String[] determineImportsFor(Map entries) {
		// get contained packages to do matching on the test hierarchy
		Collection containedPackages = jarCreator.getContainedPackages();
		Set cumulatedPackages = new LinkedHashSet();

		// make sure the collection package is valid
		boolean validPackageCollection = !containedPackages.isEmpty();

		boolean trace = logger.isTraceEnabled();

		for (Iterator iterator = entries.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String resourceName = (String) entry.getKey();

			// filter out the test hierarchy
			if (resourceName.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
				if (trace)
					logger.trace("Analyze imports for test bundle resource " + resourceName);
				String classFileName = StringUtils.getFilename(resourceName);
				String className = classFileName.substring(0, classFileName.length()
						- ClassUtils.CLASS_FILE_SUFFIX.length());
				String classPkg = resourceName.substring(0, resourceName.length() - classFileName.length()).replace(
					'/', '.');

				if (classPkg.startsWith("."))
					classPkg = classPkg.substring(1);

				if (classPkg.endsWith("."))
					classPkg = classPkg.substring(0, classPkg.length() - 1);

				// if we don't have the package, add it
				if (validPackageCollection && StringUtils.hasText(classPkg) && !containedPackages.contains(classPkg)) {
					logger.trace("Package [" + classPkg + "] is NOT part of the test archive; adding an import for it");
					cumulatedPackages.add(classPkg);
				}

				// otherwise parse the class byte-code
				else {
					if (trace)
						logger.trace("Package [" + classPkg + "] is part of the test archive; parsing " + className
								+ " bytecode to determine imports...");
					cumulatedPackages.addAll(determineImportsForClass(className, (Resource) entry.getValue()));
				}
			}
		}

		return (String[]) cumulatedPackages.toArray(new String[cumulatedPackages.size()]);
	}

	/**
	 * Determine imports for a class given as a String resource. This method
	 * doesn't do any search for the enclosing/inner classes as it considers
	 * that these should be handled at a higher level.
	 * 
	 * The returned set contains the packages in string format (i.e. java.io)
	 * 
	 * @param className
	 * @param resource
	 * @return
	 */
	private Set determineImportsForClass(String className, Resource resource) {
		Assert.notNull(resource, "a not-null class is required");
		DependencyVisitor visitor = new DependencyVisitor();

		boolean trace = logger.isTraceEnabled();

		ClassReader reader;

		try {
			if (trace)
				logger.trace("Visiting class " + className);
			reader = new ClassReader(resource.getInputStream());
		}
		catch (Exception ex) {
			throw (RuntimeException) new IllegalArgumentException("Cannot read class " + className).initCause(ex);
		}
		reader.accept(visitor, false);

		// convert from / to . format
		Set originalPackages = visitor.getPackages();
		Set pkgs = new LinkedHashSet(originalPackages.size());

		for (Iterator iterator = originalPackages.iterator(); iterator.hasNext();) {
			String pkg = (String) iterator.next();
			pkgs.add(pkg.replace('/', '.'));
		}
		return pkgs;
	}

	protected void postProcessBundleContext(BundleContext context) throws Exception {
		logger.debug("Post processing: creating test bundle");

		Resource jar;

		Manifest mf = getManifest();

		// if the jar content hasn't been discovered yet (while creating the manifest)
		// do so now
		if (jarEntries == null) {
			// set root path
			jarCreator.setRootPath(getRootPath());
			// add the content pattern
			jarCreator.setContentPattern(getBundleContentPattern());

			// use jar creator for pattern discovery
			jar = jarCreator.createJar(mf);
		}

		// otherwise use the cached resources
		else {
			jar = jarCreator.createJar(mf, jarEntries);
		}

		try {
			installAndStartBundle(context, jar);
		}
		catch (Exception e) {
			IllegalStateException ise = new IllegalStateException(
				"Unable to dynamically start generated unit test bundle");
			ise.initCause(e);
			throw ise;
		}

		// now do the delegation
		super.postProcessBundleContext(context);
	}

	private void installAndStartBundle(BundleContext context, Resource resource) throws Exception {
		// install & start
		Bundle bundle = context.installBundle("[onTheFly-test-bundle]" + ClassUtils.getShortName(getClass()) + "["
				+ hashCode() + "]", resource.getInputStream());

		String bundleString = OsgiStringUtils.nullSafeNameAndSymName(bundle);
		boolean debug = logger.isDebugEnabled();

		if (debug)
			logger.debug("Test bundle [" + bundleString + "] succesfully installed");
		bundle.start();
		if (debug)
			logger.debug("Test bundle [" + bundleString + "] succesfully started");
	}

}
