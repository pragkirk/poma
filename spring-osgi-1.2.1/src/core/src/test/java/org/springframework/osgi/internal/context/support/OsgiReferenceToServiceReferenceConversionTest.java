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
package org.springframework.osgi.internal.context.support;

import java.awt.Polygon;
import java.awt.Shape;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;

import junit.framework.TestCase;

import org.osgi.framework.ServiceReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.osgi.mock.ArrayEnumerator;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;

/**
 * 
 * @author Costin Leau
 * 
 */
public class OsgiReferenceToServiceReferenceConversionTest extends TestCase {

	private MockBundleContext context;

	private OsgiBundleXmlApplicationContext appCtx;

	private Shape service;

	public static class RefContainer {
		private static ServiceReference reference;

		public void setServiceReference(ServiceReference ref) {
			RefContainer.reference = ref;
		}
	}

	protected void setUp() throws Exception {
		service = new Polygon();
		RefContainer.reference = null;

		MockBundle bundle = new MockBundle() {
			public Enumeration findEntries(String path, String filePattern, boolean recurse) {
				try {
					return new ArrayEnumerator(
							new URL[] { new ClassPathResource(
									"/org/springframework/osgi/internal/context/support/serviceReferenceConversion.xml").getURL() });
				}
				catch (IOException io) {
					throw new RuntimeException(io);
				}
			}
		};

		context = new MockBundleContext(bundle) {
			public Object getService(ServiceReference reference) {
				String[] classes = OsgiServiceReferenceUtils.getServiceObjectClasses(reference);
				if (Arrays.equals(classes, new String[] { Shape.class.getName() }))
					return service;
				else
					return null;
			}
		};

		appCtx = new OsgiBundleXmlApplicationContext(new String[] { "serviceReferenceConversion.xml" });
		appCtx.setBundleContext(context);
		appCtx.setPublishContextAsService(false);
		appCtx.refresh();
	}

	protected void tearDown() throws Exception {
		context = null;
		appCtx.close();
		appCtx = null;
		service = null;
		RefContainer.reference = null;
	}

	public void testApplicationContextStarted() throws Exception {
		assertEquals(2, appCtx.getBeanDefinitionCount());
	}

	public void testConversion() throws Exception {
		assertNotNull(RefContainer.reference);
		System.out.println(RefContainer.reference);
	}

}
