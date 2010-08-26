package org.springframework.osgi.iandt.config;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.iandt.configuration.ManagedServiceFactoryListener;
import org.springframework.osgi.iandt.configuration.ManagedServiceListener;
import org.springframework.osgi.util.OsgiServiceReferenceUtils;

/**
 * @author Hal Hildebrand Date: Jun 14, 2007 Time: 7:16:43 PM
 */

public abstract class ConfigTest extends BaseIntegrationTest {
	private ConfigurationAdmin admin;

	private String location;

	private static String CONFIG_DIR = "test-config";

	protected String[] getTestBundlesNames() {
		System.setProperty("com.gatespace.bundle.cm.store", CONFIG_DIR);
		initializeDirectory(CONFIG_DIR);
		return new String[] { "org.knopflerfish.bundles, log_all, 2.0.0", "org.knopflerfish.bundles, cm_all, 2.0.0",
				"org.springframework.osgi.iandt, configuration," + getSpringDMVersion() };
	}

	protected String getManifestLocation() {
		return null;
	}

	protected static void initializeDirectory(String dir) {
		File directory = new File(dir);
		remove(directory);
		assertTrue(dir + " directory successfully created", directory.mkdirs());
	}

	private static void remove(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isDirectory()) {
					remove(file);
				}
				else {
					assertTrue(file + " deleted", file.delete());
				}
			}
			assertTrue(directory + " directory successfully cleared", directory.delete());
		}
	}

	protected void onSetUp() throws Exception {
		ServiceReference ref = OsgiServiceReferenceUtils.getServiceReference(bundleContext,
			ConfigurationAdmin.class.getName(), null);
		admin = (ConfigurationAdmin) bundleContext.getService(ref);
		assertNotNull("Configuration Admin exists", admin);
		BundleContext bc = bundleContext;
		Bundle[] bundles = bc.getBundles();
		for (int i = 0; i < bundles.length; i++) {
			if ("org.springframework.osgi.iandt.configuration".equals(bundles[i].getSymbolicName())) {
				location = bundles[i].getLocation();
				break;
			}
		}
	}

	public void testManagedService() throws Exception {
		Thread.sleep(10);
		assertEquals(0, ManagedServiceListener.updates.size());
		Dictionary test = new Hashtable();
		test.put("foo", "bar");

		Configuration config = admin.getConfiguration(ManagedServiceListener.SERVICE_FACTORY_PID, location);
		config.update(test);
		Thread.sleep(10);

		assertEquals(2, ManagedServiceListener.updates.size());
		Dictionary props = (Dictionary) ManagedServiceListener.updates.get(0);
		assertEquals("bar", props.get("foo"));
		props = (Dictionary) ManagedServiceListener.updates.get(1);
		assertEquals("bar", props.get("foo"));
	}

	public void testManagedServiceFactory() throws Exception {
		Thread.sleep(10);
		assertEquals(0, ManagedServiceFactoryListener.updates.size());
		Dictionary test = new Hashtable();
		test.put("foo", "bar");

		Configuration config = admin.createFactoryConfiguration(ManagedServiceFactoryListener.SERVICE_FACTORY_PID,
			location);
		config.update(test);
		Thread.sleep(10);
		Thread.sleep(10);
		assertEquals(2, ManagedServiceFactoryListener.updates.size());
		Object[] update = (Object[]) ManagedServiceFactoryListener.updates.get(0);
		assertNotNull("instance Pid exists", update[0]);
		Dictionary props = (Dictionary) update[1];
		assertEquals("bar", props.get("foo"));

		update = (Object[]) ManagedServiceFactoryListener.updates.get(1);
		assertNotNull("instance Pid exists", update[0]);
		props = (Dictionary) update[1];
		assertEquals("bar", props.get("foo"));
	}
}
