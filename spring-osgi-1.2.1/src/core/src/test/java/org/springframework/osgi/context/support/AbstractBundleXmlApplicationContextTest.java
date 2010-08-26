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
package org.springframework.osgi.context.support;

import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.beans.BeansException;
import org.springframework.osgi.util.OsgiStringUtils;

/**
 * 
 * @author Costin Leau
 */
public class AbstractBundleXmlApplicationContextTest extends TestCase {

	OsgiBundleXmlApplicationContext xmlContext;

	MockControl bundleCtxCtrl, bundleCtrl;

	BundleContext context;

	Bundle bundle;

	Dictionary dictionary;

	protected void setUp() throws Exception {
		bundleCtxCtrl = MockControl.createNiceControl(BundleContext.class);
		context = (BundleContext) bundleCtxCtrl.getMock();
		bundleCtrl = MockControl.createNiceControl(Bundle.class);
		bundle = (Bundle) bundleCtrl.getMock();

		bundleCtxCtrl.expectAndReturn(context.getBundle(), bundle, MockControl.ONE_OR_MORE);

		dictionary = new Hashtable();

		// allow headers to be taken multiple times
		bundleCtrl.expectAndReturn(bundle.getHeaders(), dictionary, MockControl.ONE_OR_MORE);
	}

	private void createContext() {
		xmlContext = new OsgiBundleXmlApplicationContext(new String[] {}) {
			public void refresh() throws BeansException {
				// no-op
			}
		};
        xmlContext.setBundleContext(context);
    }

	protected void tearDown() throws Exception {
		// bundleCtxCtrl.verify();
		// bundleCtrl.verify();
		context = null;
		bundleCtxCtrl = null;
		xmlContext = null;
		bundle = null;
		bundleCtrl = null;
	}

	public void testGetBundleName() {
		String symbolicName = "symbolic";
		// bundleCtrl.reset();
		bundleCtrl.expectAndReturn(bundle.getSymbolicName(), symbolicName, MockControl.ONE_OR_MORE);
		bundleCtxCtrl.replay();
		bundleCtrl.replay();

		// check default
		createContext();

		assertEquals(symbolicName, OsgiStringUtils.nullSafeSymbolicName(bundle));
	}

	public void testGetBundleNameFallbackMechanism() {
		bundleCtxCtrl.replay();
		bundleCtrl.replay();

		String title = "Phat City";
		dictionary.put(Constants.BUNDLE_NAME, title);

		// check default
		createContext();

		// use the 2 symbolic name calls
		assertEquals(title, OsgiStringUtils.nullSafeName(bundle));
	}

	public void testGetServiceName() {
		String symbolicName = "symbolic";
		// bundleCtrl.reset();
		bundleCtrl.expectAndReturn(bundle.getSymbolicName(), symbolicName, MockControl.ONE_OR_MORE);
		bundleCtxCtrl.replay();
		bundleCtrl.replay();

		createContext();
		assertEquals(symbolicName, OsgiStringUtils.nullSafeSymbolicName(bundle));

	}

}
