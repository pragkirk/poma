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

package org.springframework.osgi.iandt.nonosgicl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Hashtable;

import javax.sql.DataSource;

import org.osgi.framework.Constants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.osgi.iandt.BaseIntegrationTest;

/**
 * Integration test that checks whether the non-osgi classloader for JDK/OSGi
 * classes are properly considered when creating service proxies.
 * 
 * @author Costin Leau
 */
public class NonOSGiLoaderProxyTest extends BaseIntegrationTest {

	// a service that implements several custom classes
	private class Service implements DataSource, Comparator, InitializingBean, Constants {

		public Connection getConnection() throws SQLException {
			return null;
		}

		public Connection getConnection(String username, String password) throws SQLException {
			return null;
		}

		public int getLoginTimeout() throws SQLException {
			return 0;
		}

		public PrintWriter getLogWriter() throws SQLException {
			return null;
		}

		public void setLoginTimeout(int seconds) throws SQLException {
		}

		public void setLogWriter(PrintWriter out) throws SQLException {
		}

		public void afterPropertiesSet() throws Exception {
		}

		public int compare(Object arg0, Object arg1) {
			return 0;
		}

		public boolean isWrapperFor(Class iface) {
			return false;
		}

		public Object unwrap(Class c) {
			return null;
		}
	}


	public void testProxy() throws Exception {
		// publish service
		bundleContext.registerService(new String[] { DataSource.class.getName(), Comparator.class.getName(),
			InitializingBean.class.getName(), Constants.class.getName() }, new Service(), new Hashtable());

		ConfigurableApplicationContext ctx = getNestedContext();
		assertNotNull(ctx);
		Object proxy = ctx.getBean("service");
		assertNotNull(proxy);
		assertTrue(proxy instanceof DataSource);
		assertTrue(proxy instanceof Comparator);
		assertTrue(proxy instanceof Constants);
		assertTrue(proxy instanceof InitializingBean);
		ctx.close();
	}

	private ConfigurableApplicationContext getNestedContext() throws Exception {
		OsgiBundleXmlApplicationContext ctx = new OsgiBundleXmlApplicationContext(
			new String[] { "org/springframework/osgi/iandt/nonosgicl/context.xml" });

		ctx.setBundleContext(bundleContext);
		ctx.refresh();
		return ctx;
	}
}
