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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.osgi.config.internal.adapter.OsgiServiceLifecycleListenerAdapter;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.service.importer.ImportedOsgiServiceProxy;
import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;
import org.springframework.osgi.service.importer.ServiceReferenceProxy;
import org.springframework.osgi.service.importer.support.internal.aop.PublicStaticSwappingServiceReferenceProxy;
import org.springframework.osgi.util.internal.MapBasedDictionary;

/**
 * @author Costin Leau
 */
public class OsgiServiceLifecycleListenerAdapterTest extends TestCase {

	protected static class JustListener implements OsgiServiceLifecycleListener {

		public static int BIND_CALLS = 0;

		public static int UNBIND_CALLS = 0;


		public void bind(Object service, Map properties) throws Exception {
			BIND_CALLS++;
		}

		public void unbind(Object service, Map properties) throws Exception {
			UNBIND_CALLS++;
		}
	}

	protected static class CustomListener {

		public static int BIND_CALLS = 0;

		public static int UNBIND_CALLS = 0;

		public static List BIND_SERVICES = new ArrayList();

		public static List UNBIND_SERVICES = new ArrayList();


		public void myBind(Object service, Map properties) {
			BIND_CALLS++;
			BIND_SERVICES.add(service);

		}

		public void myUnbind(Object service, Map properties) {
			UNBIND_CALLS++;
			UNBIND_SERVICES.add(service);

		}

		public void wrongBind() {
			BIND_CALLS++;
		}

		public void wrongUnbind() {
			UNBIND_CALLS--;
		}
	}

	protected static class CustomAndListener extends JustListener {

		public Integer aBind(Object service, Map props) throws Exception {
			super.bind(service, props);
			return null;
		}

		public void aUnbind(Object service, Map props) throws Exception {
			super.unbind(service, props);
		}
	}

	protected static class OverloadedCustomMethods extends CustomListener {

		public void myBind(Date service, Map properties) {
			super.myBind(service, properties);
		}

		public void myUnbind(String service, Map properties) {
			super.myUnbind(service, properties);
		}
	}

	/**
	 * Override standard methods with ones that throw exceptions.
	 * 
	 * @author Costin Leau
	 */
	protected static class ExceptionListener extends CustomAndListener {

		public void bind(Object service, Map properties) throws Exception {
			throw new Exception("expected!") {

				public synchronized Throwable fillInStackTrace() {
					return null;
				}
			};
		}

		public void unbind(Object service, Map properties) throws Exception {
			throw new IOException("expected!") {

				public synchronized Throwable fillInStackTrace() {
					return null;
				}
			};
		}
	}

	protected static class ExceptionCustomListener extends CustomListener {

		public void myBind(Date service, Map properties) {
			throw (RuntimeException) new RuntimeException("expected!") {

				public synchronized Throwable fillInStackTrace() {
					return null;
				}
			};
		}

		public void myUnbind(String service, Map properties) throws IOException {
			throw new IOException("expected!") {

				public synchronized Throwable fillInStackTrace() {
					return null;
				}
			};
		}
	}

	// piggy backs on JustListener static fields
	protected static class DictionaryAndMapCustomListener {

		public void bind(Object service, Dictionary properties) {
			JustListener.BIND_CALLS++;
		}

		public void unbind(Object service, Map props) {
			JustListener.UNBIND_CALLS++;
		}

		public void unbind(Object service, Dictionary props) throws Exception {
			JustListener.UNBIND_CALLS++;
		}
	}

	protected static class OverridingMethodListener extends CustomListener {

		public void myBind(Object service, Map properties) {
			BIND_CALLS++;
		}

		public void myUnbind(Object service, Map properties) {
			UNBIND_CALLS++;
		}
	}

	protected static class JustBind {

		public void myBind(Object service, Map properties) {
			JustListener.BIND_CALLS++;
		}
	}

	protected static class JustUnbind {

		public void myUnbind(Object service, Map properties) {
			JustListener.UNBIND_CALLS++;
		}
	}

	protected static class CustomServiceRefListener {

		private void myUnbind(ServiceReference ref) {
			JustListener.UNBIND_CALLS++;
		}

		private void myBind(ServiceReference ref) {
			JustListener.BIND_CALLS++;
		}
	}


	private OsgiServiceLifecycleListenerAdapter listener;

	private static final String BEAN_NAME = "bla";


	protected void setUp() throws Exception {
		JustListener.BIND_CALLS = 0;
		JustListener.UNBIND_CALLS = 0;
		OverloadedCustomMethods.BIND_SERVICES = new ArrayList();
		OverloadedCustomMethods.UNBIND_SERVICES = new ArrayList();

		CustomListener.BIND_CALLS = 0;
		CustomListener.UNBIND_CALLS = 0;
	}

	protected void tearDown() throws Exception {
		listener = null;
		OverloadedCustomMethods.BIND_SERVICES = null;
		OverloadedCustomMethods.UNBIND_SERVICES = null;
	}

	public void testWrapperOverListener() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new JustListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.afterPropertiesSet();

		assertEquals(0, JustListener.BIND_CALLS);
		listener.bind(null, null);
		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);
		listener.bind(null, null);
		assertEquals(2, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.unbind(null, null);
		assertEquals(1, JustListener.UNBIND_CALLS);
		assertEquals(2, JustListener.BIND_CALLS);
		listener.unbind(null, null);
		assertEquals(2, JustListener.UNBIND_CALLS);
		assertEquals(2, JustListener.BIND_CALLS);
	}

	public void testWrapperOverNoInvalidClass() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setTargetBeanName(BEAN_NAME);
		listener.setTarget(new Object());
		listener.setBeanFactory(createMockBF(new Object()));

		try {
			listener.afterPropertiesSet();
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testWrapperWithIncorrectCustomMethodNames() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setTargetBeanName(BEAN_NAME);
		listener.setTarget(new Object());
		listener.setBindMethod("pop");
		listener.setUnbindMethod("corn");

		try {
			listener.afterPropertiesSet();
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testWrapperWithCorrectCustomMethodNamesButIncorrectArgumentTypes() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setTarget(new CustomListener());
		listener.setBindMethod("wrongBind");
		listener.setUnbindMethod("wrongUnbind");

		assertEquals(0, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);
		try {
			listener.afterPropertiesSet();
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}

		assertEquals(0, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);

	}

	public void testWrapperWithCustomMethods() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new CustomListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		Object service = new Object();
		assertEquals(0, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);
		listener.bind(new Object(), null);
		assertEquals(1, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);

		listener.bind(service, null);
		assertEquals(2, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);

		listener.unbind(service, null);
		assertEquals(2, CustomListener.BIND_CALLS);
		assertEquals(1, CustomListener.UNBIND_CALLS);

		listener.unbind(service, null);
		assertEquals(2, CustomListener.BIND_CALLS);
		assertEquals(2, CustomListener.UNBIND_CALLS);
	}

	public void testWrapperWithCustomMethodsAndNullParameters() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new CustomListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		assertEquals(0, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);
		listener.bind(null, null);
		assertEquals("null services allowed", 1, CustomListener.BIND_CALLS);

		listener.unbind(null, null);

		assertEquals("null services allowed", 1, CustomListener.UNBIND_CALLS);
	}

	public void testWrapperWithBothCustomAndInterfaceMethods() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new CustomAndListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("aBind");
		listener.setUnbindMethod("aUnbind");
		listener.afterPropertiesSet();

		Object service = new Object();

		assertEquals(0, CustomAndListener.BIND_CALLS);
		assertEquals(0, CustomAndListener.UNBIND_CALLS);
		listener.bind(service, null);
		assertEquals(2, CustomAndListener.BIND_CALLS);
		assertEquals(0, CustomAndListener.UNBIND_CALLS);

		listener.unbind(service, null);
		assertEquals(2, CustomAndListener.BIND_CALLS);
		assertEquals(2, CustomAndListener.UNBIND_CALLS);

	}

	public void testWrapperWithCustomOverloadedMethodsAndDifferentServiceTypes() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new OverloadedCustomMethods()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		Object objService = new Object();
		Date dateService = new Date();
		String stringService = "token";

		assertEquals(0, OverloadedCustomMethods.BIND_CALLS);
		assertEquals(0, OverloadedCustomMethods.UNBIND_CALLS);
		listener.bind(objService, null);
		assertEquals("only one method accepts Object(s)", 1, OverloadedCustomMethods.BIND_CALLS);
		assertEquals(0, OverloadedCustomMethods.UNBIND_CALLS);

		listener.bind(dateService, null);
		assertEquals("two method accept Date(s)", 3, OverloadedCustomMethods.BIND_CALLS);
		assertEquals(0, OverloadedCustomMethods.UNBIND_CALLS);

		listener.unbind(stringService, null);
		assertEquals("two method accept Date(s)", 3, OverloadedCustomMethods.BIND_CALLS);
		assertEquals("two method accept String(s)", 2, OverloadedCustomMethods.UNBIND_CALLS);

		listener.unbind(objService, null);
		assertEquals("two method accept Date(s)", 3, OverloadedCustomMethods.BIND_CALLS);
		assertEquals("only one method accepts Object(s)", 3, OverloadedCustomMethods.UNBIND_CALLS);

		assertEquals(3, OverloadedCustomMethods.BIND_SERVICES.size());
		assertSame("incorrect call order", objService, OverloadedCustomMethods.BIND_SERVICES.get(0));
		assertSame("incorrect call order", dateService, OverloadedCustomMethods.BIND_SERVICES.get(1));
		assertSame("incorrect call order", dateService, OverloadedCustomMethods.BIND_SERVICES.get(2));

		assertEquals(3, OverloadedCustomMethods.UNBIND_SERVICES.size());
		assertSame("incorrect call order", stringService, OverloadedCustomMethods.UNBIND_SERVICES.get(0));
		assertSame("incorrect call order", stringService, OverloadedCustomMethods.UNBIND_SERVICES.get(1));
		assertSame("incorrect call order", objService, OverloadedCustomMethods.UNBIND_SERVICES.get(2));
	}

	public void testExceptionOnListenerMethod() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new ExceptionListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("aBind");
		listener.setUnbindMethod("aUnbind");
		listener.afterPropertiesSet();

		Object service = new Object();
		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);
		listener.bind(service, null);
		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.unbind(service, null);
		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals(1, JustListener.UNBIND_CALLS);
	}

	public void testExceptionOnCustomMethods() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new ExceptionCustomListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		Date service = new Date();
		assertEquals(0, ExceptionCustomListener.BIND_CALLS);
		assertEquals(0, ExceptionCustomListener.UNBIND_CALLS);
		listener.bind(service, null);
		assertEquals("should have called overloaded method with type Object", 1, ExceptionCustomListener.BIND_CALLS);
		assertEquals(0, ExceptionCustomListener.UNBIND_CALLS);

		listener.unbind(service, null);
		assertEquals(1, ExceptionCustomListener.BIND_CALLS);
		assertEquals("should have called overloaded method with type Object", 1, ExceptionCustomListener.UNBIND_CALLS);
	}

	public void testStandardListenerWithListeningMethodsSpecifiedAsCustomOnes() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new JustListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("bind");
		listener.setUnbindMethod("unbind");
		listener.afterPropertiesSet();

		Object service = null;
		assertEquals(0, JustListener.BIND_CALLS);
		listener.bind(service, null);
		// only the interface is being called since the service is null
		assertEquals(2, JustListener.BIND_CALLS);

		service = new Object();

		listener.bind(service, null);
		assertEquals(4, JustListener.BIND_CALLS);
	}

	public void testListenerWithOverloadedTypesAndMultipleParameterTypes() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new DictionaryAndMapCustomListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("bind");
		listener.setUnbindMethod("unbind");
		listener.afterPropertiesSet();

		Object service = new Date();
		MapBasedDictionary props = new MapBasedDictionary();

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);
		listener.bind(service, props);

		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.unbind(service, props);
		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals("only one unbind method should be called", 1, JustListener.UNBIND_CALLS);
	}

	public void testOverridingMethodsDiscovery() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new OverridingMethodListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		assertEquals(0, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);

		listener.bind(new Object(), null);
		assertEquals(1, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);

		listener.unbind(new Object(), null);
		assertEquals(1, CustomListener.BIND_CALLS);
		assertEquals(1, CustomListener.UNBIND_CALLS);

	}

	public void testJustCustomBindMethod() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new JustBind()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.afterPropertiesSet();

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.bind(new Object(), null);

		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);
	}

	public void testJustCustomUnbindMethod() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new JustUnbind()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.unbind(new Object(), null);

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(1, JustListener.UNBIND_CALLS);
	}

	public void testCustomServiceRefBind() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new CustomServiceRefListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.afterPropertiesSet();

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.bind(new ImportedOsgiServiceProxyMock(), null);

		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

	}

	public void testCustomServiceRefUnbind() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new CustomServiceRefListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.unbind(new ImportedOsgiServiceProxyMock(), null);

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(1, JustListener.UNBIND_CALLS);
	}


	private class ImportedOsgiServiceProxyMock implements ImportedOsgiServiceProxy {

		public Map getServiceProperties() {
			return new HashMap();
		}

		public ServiceReferenceProxy getServiceReference() {
			return new PublicStaticSwappingServiceReferenceProxy(new MockServiceReference());
		}
	}


	private ConfigurableBeanFactory createMockBF(Object target) {
		MockControl ctrl = MockControl.createNiceControl(ConfigurableBeanFactory.class);
		ConfigurableBeanFactory cbf = (ConfigurableBeanFactory) ctrl.getMock();

		ctrl.expectAndReturn(cbf.getBean(BEAN_NAME), target);
		ctrl.expectAndReturn(cbf.getType(BEAN_NAME), target.getClass());

		ctrl.replay();
		return cbf;
	}
}
