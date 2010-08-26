/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.osgi.extender.internal.support;

import java.io.IOException;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.osgi.mock.MockBundle;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Tests for NamespacePlugins support
 * 
 * @author Adrian Colyer
 */
public class NamespacePluginsTest extends TestCase {

	private NamespacePlugins namespacePlugins;


	protected void setUp() throws Exception {
		super.setUp();
		this.namespacePlugins = new NamespacePlugins();
	}

	public void testCantResolveWithNoPlugins() throws IOException, SAXException {
		assertNull("Should be unable to resolve namespace", this.namespacePlugins.resolve("http://org.xyz"));
		assertNull("Should be unable to resolve entity", this.namespacePlugins.resolveEntity("pub-id", "sys-id"));
	}

	public void testCanResolveNamespaceFromBundleAfterAddingPlugin() throws IOException, SAXException {
		Bundle b = new MockBundle();
		this.namespacePlugins.addHandler(b);
		NamespaceHandler handler = this.namespacePlugins.resolve("http://www.springframework.org/schema/testme");
		assertNotNull("should find handler", handler);
		assertTrue("should be TestHandler", handler instanceof TestHandler);
	}

	public void testCantResolveNamespaceAfterRemovingPlugin() throws IOException, SAXException {
		Bundle b = new MockBundle();
		this.namespacePlugins.addHandler(b);
		this.namespacePlugins.removeHandler(b);
		assertNull("Should be unable to resolve namespace",
			this.namespacePlugins.resolve("http://www.springframework.org/schema/testme"));
	}

	public void testCanResolveEntityAfterAddingPlugin() throws IOException, SAXException {
		Bundle b = new MockBundle();
		this.namespacePlugins.addHandler(b);
		InputSource resolver = this.namespacePlugins.resolveEntity("public-id",
			"http://www.springframework.org/schema/beans/testme.xsd");
		assertNotNull("Should find resolver", resolver);
	}

	public void testCantResolveEntityAfterRemovingPlugin() throws IOException, SAXException {
		Bundle b = new MockBundle();
		this.namespacePlugins.addHandler(b);
		this.namespacePlugins.removeHandler(b);
		InputSource resolver = this.namespacePlugins.resolveEntity("public-id",
			"http://www.springframework.org/schema/beans/testme.xsd");
		assertNull("Should be unable to resolve entity", resolver);
	}
}