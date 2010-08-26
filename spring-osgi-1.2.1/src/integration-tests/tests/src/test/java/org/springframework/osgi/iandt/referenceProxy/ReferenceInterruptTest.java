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

package org.springframework.osgi.iandt.referenceProxy;

import java.awt.Shape;

import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.service.ServiceUnavailableException;
import org.springframework.osgi.service.importer.support.Cardinality;
import org.springframework.osgi.service.importer.support.ImportContextClassLoader;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;

/**
 * @author Costin Leau
 * 
 */
public class ReferenceInterruptTest extends BaseIntegrationTest {

	public void testProxyInterrupt() throws Exception {
		long initialWait = 20 * 1000;
		final OsgiServiceProxyFactoryBean proxyFactory = new OsgiServiceProxyFactoryBean();
		proxyFactory.setBeanClassLoader(getClass().getClassLoader());
		proxyFactory.setBundleContext(bundleContext);
		proxyFactory.setCardinality(Cardinality.C_0__1);
		proxyFactory.setContextClassLoader(ImportContextClassLoader.UNMANAGED);
		proxyFactory.setInterfaces(new Class[] { Shape.class });
		proxyFactory.setTimeout(initialWait);
		proxyFactory.afterPropertiesSet();

		Runnable resetProxy = new Runnable() {

			public void run() {
				try {
					Thread.sleep(3 * 1000);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				proxyFactory.setTimeout(0);
			}
		};

		Object proxy = proxyFactory.getObject();
		assertNotNull(proxy);
		Thread stopThread = new Thread(resetProxy, "reset-proxy-thread");
		stopThread.start();

		long start = System.currentTimeMillis();
		logger.info("Invoking proxy...");
		try {
			proxy.toString();
			fail("no service should have been found...");
		}
		catch (ServiceUnavailableException sue) {
		}

		long stop = System.currentTimeMillis();
		assertTrue(stop - start < initialWait);
	}
}
