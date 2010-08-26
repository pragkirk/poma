/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.osgi.iandt.io;

import org.osgi.framework.Constants;
import org.springframework.core.io.Resource;

/**
 * @author Costin Leau
 * 
 */
public class ClassSpaceWildcardTest extends BaseIoTest {

	//
	// Wild-card tests
	//

	public void testBundleClassPath() throws Exception {
		System.out.println("*** Bundle-ClassPath is " + bundle.getHeaders().get(Constants.BUNDLE_CLASSPATH));
		Resource res[] = patternLoader.getResources("classpath*:/org/springframework/osgi/iandt/io/ClassSpaceWildcardTest.class");
		assertEquals("invalid bundle-classpath entries should be skipped", 1, res.length);
		printPathWithinContext(res);
	}

	// finds all files at root level
	public void testWildcardAtRootFileLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/*");
		// only the bundle and its fragments should be considered (since no other META-INF/ is available on the classpath)
		assertEquals("not enough packages found", 3, res.length);
		printPathWithinContext(res);
	}

	// similar as the root test but inside META-INF
	public void testWildcardAtFolderLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/META-INF/*");
		assertEquals("not enough packages found", 1, res.length);
		printPathWithinContext(res);
	}

	public void testSingleClassWithWildcardAtFileLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/org/springframework/osgi/iandt/io/Class*Test.class");
		assertTrue("not enough packages found", res.length >= 1);
		printPathWithinContext(res);
	}

	public void testClassPathRootWildcard() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/**/osgi/iandt/io/Class*Test.class");
		assertTrue("not enough packages found", res.length >= 1);
	}

	public void testAllClassPathWildcardAtFolderLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:/META-INF/*");
		// only the bundle and its fragments should be considered (since no other META-INF/ is available on the classpath)
		assertEquals("not enough packages found", 3, res.length);
		printPathWithinContext(res);
	}

	public void testAllClassPathWOWildcardAtFolderLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:META-INF/");
		// only the bundle and its fragments should be considered (since no other META-INF/ is available on the classpath)
		assertEquals("not enough packages found", 3, res.length);
		printPathWithinContext(res);
	}

	public void testAllClassPathWithWildcardAtFileLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/org/springframework/osgi/iandt/io/Class*WildcardTest.class");
		assertEquals("not enough packages found", 1, res.length);
		printPathWithinContext(res);
	}

	public void testAllClassPathWithWithWildcardAtFileLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:/org/springframework/osgi/iandt/io/Class*WildcardTest.class");
		assertEquals("not enough packages found", 1, res.length);
		printPathWithinContext(res);
	}

	public void testAllClassPathRootWildcard() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/**/springframework/osgi/**/ClassSpaceWildcardTest.class");
		assertEquals("not enough packages found", 1, res.length);
		printPathWithinContext(res);
	}

	public void testAllClassPathRootWithWildcard() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:/**/springframework/osgi/**/ClassSpaceWildcardTest.class");
		assertEquals("not enough packages found", 1, res.length);
		printPathWithinContext(res);
	}

	//
	// Stress tests (as they pull a lot of content)
	//

	public void testMatchingALotOfClasses() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:/**/springframework/osgi/iandt/io/*.class");
		// at least 2 classes should be in there
		assertTrue("not enough packages found", res.length > 1);
		printPathWithinContext(res);
	}

	// EQ = 48
	// KF = 48
	// FX = 38
	public void testMatchingALotOfFolders() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:/**/springframework/osgi/**");
		System.out.println("resources count " + res.length);
		assertTrue("not enough packages found", res.length > 10);
		printPathWithinContext(res);
	}

	// ask for everything springframework :)
	// EQ = 147
	// KF = 147
	// FX = 135 (no fragment support)
	public void testMatchingABulkOfResources() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:**/springframework/**");
		Resource resWitSlash[] = patternLoader.getResources("classpath*:/**/springframework/**");
		System.out.println("resources w/o slash count " + res.length);
		System.out.println("resources w/ slash count " + resWitSlash.length);
		assertEquals("slash should not make a difference", res.length, resWitSlash.length);
		assertTrue("not enough packages found", res.length > 50);
		printPathWithinContext(res);
	}

	// ask for everything org :)
	// EQ = 271 (since it considers the system bundle also)
	// KF = 147 (doesn't consider system bundle)
	// FX = 135
	public void testMatchingAHugeSetOfResources() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:org/**");
		Resource resWitSlash[] = patternLoader.getResources("classpath*:/org/**");
		System.out.println("resources w/o slash count " + res.length);
		System.out.println("resources w/ slash count " + resWitSlash.length);
		assertEquals("slash should not make a difference", res.length, resWitSlash.length);
		assertTrue("not enough packages found", res.length > 100);
		printPathWithinContext(res);
	}

	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		// felix doesn't support fragments yet
		return (isFelix() && (testMethodName.equals("testAllClassPathWildcardAtFolderLevel")
				|| testMethodName.equals("testWildcardAtRootFileLevel") || testMethodName.equals("testAllClassPathWOWildcardAtFolderLevel")));
	}
}
