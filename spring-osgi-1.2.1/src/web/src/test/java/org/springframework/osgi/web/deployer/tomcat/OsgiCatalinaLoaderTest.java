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

package org.springframework.osgi.web.deployer.tomcat;

import org.apache.catalina.Container;
import org.easymock.MockControl;

import junit.framework.TestCase;

/**
 * 
 * @author Costin Leau
 */
public class OsgiCatalinaLoaderTest extends TestCase {

	private OsgiCatalinaLoader loader;


	protected void setUp() throws Exception {
		loader = new OsgiCatalinaLoader();
	}

	protected void tearDown() throws Exception {
		loader = null;
	}

	public void testAddPropertyChangeListener() {
		try {
			loader.addPropertyChangeListener(null);
			fail("expected exception");
		}
		catch (Exception ex) {
		}
	}

	public void testAddRepository() {
		try {
			loader.addRepository("gigel");
			fail("expected exception");
		}
		catch (Exception ex) {
		}
	}

	public void testFindRepositories() {
		try {
			loader.findRepositories();
			fail("expected exception");
		}
		catch (Exception ex) {
		}
	}

	public void testGetDelegate() {
		assertFalse(loader.getDelegate());
	}

	public void testGetInfo() {
		assertEquals(loader.toString(), loader.getInfo());
	}

	public void testGetReloadable() {
		assertFalse(loader.getReloadable());
	}

	public void testModified() {
		assertFalse(loader.modified());
	}

	public void testRemovePropertyChangeListener() {
		try {
			loader.removePropertyChangeListener(null);
			fail("expected exception");
		}
		catch (Exception ex) {
		}
	}

	public void testSetContainer() {
		Container container = (Container) MockControl.createControl(Container.class).getMock();
		loader.setContainer(container);
		assertSame(container, loader.getContainer());
	}

	public void testSetClassLoader() {
		ClassLoader cl = this.getClass().getClassLoader();
		loader.setClassLoader(cl);
		assertSame(cl, loader.getClassLoader());
	}

	public void testSetDelegate() {
		loader.setDelegate(false);
		try {
			loader.setDelegate(true);
			fail("should not allow delegate");
		}
		catch (Exception ex) {
		}
	}

	public void testSetReloadable() {
		loader.setReloadable(false);
		try {
			loader.setReloadable(true);
			fail("should not allow reloadable");
		}
		catch (Exception ex) {
		}
	}

	public void testPropertyChange() {
		loader.propertyChange(null);
	}
}
