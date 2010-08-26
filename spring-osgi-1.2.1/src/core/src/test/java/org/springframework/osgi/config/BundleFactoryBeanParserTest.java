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
package org.springframework.osgi.config;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.osgi.TestUtils;
import org.springframework.osgi.bundle.BundleAction;
import org.springframework.osgi.bundle.BundleFactoryBean;
import org.springframework.osgi.context.support.BundleContextAwareProcessor;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockBundleContext;

/**
 * @author Costin Leau
 * 
 */
public class BundleFactoryBeanParserTest extends TestCase {

	private GenericApplicationContext appContext;

	private Bundle startBundle, installBundle, updateBundle, bundleA;

	private MockControl installBundleMC, startBundleMC, updateBundleMC;

	private static List INSTALL_BUNDLE_ACTION;

	private Bundle[] bundleToInstall = new Bundle[1];

	private static final String STREAM_TAG = "| stream |";

	protected void setUp() throws Exception {
		INSTALL_BUNDLE_ACTION = new ArrayList();

		installBundleMC = MockControl.createControl(Bundle.class);
		installBundle = (Bundle) installBundleMC.getMock();
		installBundleMC.expectAndReturn(installBundle.getSymbolicName(), "installBundle", MockControl.ZERO_OR_MORE);

		updateBundleMC = MockControl.createControl(Bundle.class);
		updateBundle = (Bundle) updateBundleMC.getMock();
		updateBundleMC.expectAndReturn(updateBundle.getSymbolicName(), "updateBundle", MockControl.ONE_OR_MORE);

		startBundleMC = MockControl.createControl(Bundle.class);
		startBundle = (Bundle) startBundleMC.getMock();
		startBundleMC.expectAndReturn(startBundle.getSymbolicName(), "startBundle", MockControl.ONE_OR_MORE);

		bundleA = new MockBundle("bundleA");

		final Bundle[] bundles = new Bundle[] { installBundle, startBundle, updateBundle, bundleA };

		BundleContext bundleContext = new MockBundleContext() {
			// return proper bundles
			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[0];
			}

			public Bundle[] getBundles() {
				return bundles;
			}

			public Bundle installBundle(String location, InputStream input) throws BundleException {
				INSTALL_BUNDLE_ACTION.add(location + STREAM_TAG + input);
				return bundleToInstall[0];
			}

			public Bundle installBundle(String location) throws BundleException {
				INSTALL_BUNDLE_ACTION.add(location);
				return bundleToInstall[0];
			}

		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());

	}

	protected void tearDown() throws Exception {
		appContext.close();
	}

	private void refresh() {
		installBundleMC.replay();
		startBundleMC.replay();
		updateBundleMC.replay();

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		reader.loadBeanDefinitions(new ClassPathResource("bundleBeanFactoryTest.xml", getClass()));

		appContext.refresh();
	}

	public void testWithSymName() throws Exception {
		refresh();
		BundleFactoryBean fb = (BundleFactoryBean) appContext.getBean("&wSymName", BundleFactoryBean.class);
		assertSame(bundleA, fb.getObject());
		assertNull(fb.getLocation());
		assertNull(fb.getResource());

	}

	public void testLocationAndResource() throws Exception {
		refresh();
		BundleFactoryBean fb = (BundleFactoryBean) appContext.getBean("&wLocation", BundleFactoryBean.class);
		assertEquals("fromServer", fb.getLocation());
		assertNull(fb.getSymbolicName());
		assertNotNull(fb.getResource());
	}

	public void testStartBundle() throws Exception {
		bundleToInstall[0] = startBundle;
		startBundle.start();

		refresh();

		BundleFactoryBean fb = (BundleFactoryBean) appContext.getBean("&start", BundleFactoryBean.class);

		BundleAction action = getAction(fb);
		assertSame(BundleAction.START, action);
		assertNull(getDestroyAction(fb));

		assertSame(startBundle, appContext.getBean("start"));
		startBundleMC.verify();
	}

	public void testStopBundle() throws Exception {
		bundleToInstall[0] = startBundle;

		// invoked on shutdown
		startBundle.stop();

		refresh();

		BundleFactoryBean fb = (BundleFactoryBean) appContext.getBean("&stop", BundleFactoryBean.class);
		assertSame(BundleAction.STOP, getDestroyAction(fb));

		assertSame(startBundle, appContext.getBean("stop"));

		appContext.close();
		startBundleMC.verify();
	}

	public void testUpdateBundle() throws Exception {
		bundleToInstall[0] = updateBundle;

		updateBundle.update();
		updateBundle.stop();
		refresh();

		BundleFactoryBean fb = (BundleFactoryBean) appContext.getBean("&update", BundleFactoryBean.class);

		BundleAction action = getAction(fb);
		assertSame(BundleAction.UPDATE, action);
		assertSame(BundleAction.STOP, getDestroyAction(fb));

		assertSame(updateBundle, appContext.getBean("update"));
		appContext.close();
		updateBundleMC.verify();
	}

	public void testInstall() throws Exception {
		bundleToInstall[0] = installBundle;

		installBundle.start();
		installBundle.uninstall();

		refresh();

		BundleFactoryBean fb = (BundleFactoryBean) appContext.getBean("&install", BundleFactoryBean.class);
		assertEquals("fromClient", fb.getLocation());
		assertEquals(1, INSTALL_BUNDLE_ACTION.size());
		assertEquals("fromClient", INSTALL_BUNDLE_ACTION.get(0));

		assertSame(installBundle, appContext.getBean("install"));
		appContext.close();
		installBundleMC.verify();
	}

	public void testInstallImpliedByUpdateUsingRealLocation() throws Exception {
		bundleToInstall[0] = installBundle;

		installBundle.update();
		installBundle.uninstall();

		refresh();

		BundleFactoryBean fb = (BundleFactoryBean) appContext.getBean("&updateFromActualLocation",
			BundleFactoryBean.class);
		assertEquals(1, INSTALL_BUNDLE_ACTION.size());

		assertSame(BundleAction.UPDATE, getAction(fb));
		assertSame(BundleAction.UNINSTALL, getDestroyAction(fb));

		assertTrue(((String) INSTALL_BUNDLE_ACTION.get(0)).indexOf(STREAM_TAG) >= -1);

		assertSame(installBundle, appContext.getBean("updateFromActualLocation"));
		appContext.close();
		installBundleMC.verify();
	}

	public void testNestedBundleDeclaration() throws Exception {
		MockControl ctrl = MockControl.createControl(Bundle.class);
		Bundle bnd = (Bundle) ctrl.getMock();

		bnd.start();
		ctrl.replay();
		appContext.getBeanFactory().registerSingleton("createdByTheTest", bnd);
		refresh();

		appContext.getBean("nested");
		BundleFactoryBean fb = (BundleFactoryBean) appContext.getBean("&nested", BundleFactoryBean.class);

		ctrl.verify();
	}

	private BundleAction getAction(BundleFactoryBean fb) {
		return (BundleAction) TestUtils.getFieldValue(fb, "action");
	}

	private BundleAction getDestroyAction(BundleFactoryBean fb) {
		return (BundleAction) TestUtils.getFieldValue(fb, "destroyAction");
	}
}
