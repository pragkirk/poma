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

package org.springframework.osgi.iandt.context;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.osgi.iandt.BaseIntegrationTest;

/**
 * Test checking the context published interfaces.
 * 
 * @author Costin Leau
 * 
 */
public class PublishedInterfacesTest extends BaseIntegrationTest {

	public void testEmptyApplicationContext() throws Exception {
		checkedPublishedOSGiService(1);
	}

	public void testXmlOsgiContext() throws Exception {
		OsgiBundleXmlApplicationContext context = new OsgiBundleXmlApplicationContext(
			new String[] { "/org/springframework/osgi/iandt/context/no-op-context.xml" });
		context.setBundleContext(bundleContext);
		context.refresh();

		checkedPublishedOSGiService(2);
		context.close();
	}

	private void checkedPublishedOSGiService(int expectedContexts) throws Exception {
		ServiceReference[] refs = bundleContext.getServiceReferences(
			ConfigurableOsgiBundleApplicationContext.class.getName(), null);
		assertEquals("different number of published contexts encountered", expectedContexts, refs.length);

		for (int i = 0; i < refs.length; i++) {
			ServiceReference serviceReference = refs[i];
			String[] interfaces = (String[]) serviceReference.getProperty(Constants.OBJECTCLASS);
			assertEquals("not enough interfaces published", 13, interfaces.length);
			assertEquals(Version.emptyVersion, serviceReference.getProperty(Constants.BUNDLE_VERSION));
			assertEquals(bundleContext.getBundle().getSymbolicName(),
				serviceReference.getProperty(Constants.BUNDLE_SYMBOLICNAME));
		}
	}
}
