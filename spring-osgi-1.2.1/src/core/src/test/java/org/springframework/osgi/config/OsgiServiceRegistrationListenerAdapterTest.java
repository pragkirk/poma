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
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.osgi.config.internal.adapter.OsgiServiceRegistrationListenerAdapter;
import org.springframework.osgi.service.exporter.OsgiServiceRegistrationListener;
import org.springframework.osgi.util.internal.MapBasedDictionary;

/**
 * @author Costin Leau
 * 
 */
public class OsgiServiceRegistrationListenerAdapterTest extends TestCase {

	protected static class JustListener implements OsgiServiceRegistrationListener {

		public static int REG_CALLS = 0;

		public static int UNREG_CALLS = 0;

		public void registered(Object service, Map serviceProperties) throws Exception {
			REG_CALLS++;
		}

		public void unregistered(Object service, Map serviceProperties) throws Exception {
			UNREG_CALLS++;
		}
	}

	protected static class CustomListener {
		public static int REG_CALLS = 0;

		public static int UNREG_CALLS = 0;

		public static List REG_PROPS = new ArrayList();

		public static List UNREG_PROPS = new ArrayList();

		public void myReg(Object service, Map properties) throws Exception {
			REG_CALLS++;
			REG_PROPS.add(properties);
		}

		public void myUnreg(Object service, Map properties) throws Exception {
			UNREG_CALLS++;
			UNREG_PROPS.add(properties);
		}

		public void wrongReg() {
			REG_CALLS++;
		}

		public void wrongUnreg() {
			UNREG_CALLS--;
		}
	}

	protected static class CustomAndListener extends JustListener {

		public Integer aReg(Object service, Map props) throws Exception {
			super.registered(service, props);
			return null;
		}

		public void aUnreg(Object service, Map props) throws Exception {
			super.unregistered(service, props);
		}
	}

	/**
	 * Override standard methods with ones that throw exceptions.
	 * 
	 * @author Costin Leau
	 * 
	 */
	protected static class ExceptionListener extends CustomAndListener {

		public void registered(Object service, Map properties) throws Exception {
			throw new Exception("expected!") {
				public synchronized Throwable fillInStackTrace() {
					return null;
				}
			};
		}

		public void unregistered(Object service, Map properties) throws Exception {
			throw new Exception("expected!") {
				public synchronized Throwable fillInStackTrace() {
					return null;
				}
			};
		}
	}

	protected static class ExceptionCustomListener extends CustomListener {
		public void myReg(Map properties) {
			REG_CALLS++;
			throw (RuntimeException) new RuntimeException("expected!") {
				public synchronized Throwable fillInStackTrace() {
					return null;
				}
			};
		}

		public void myUnreg(Map properties) throws IOException {
			UNREG_CALLS++;
			throw new IOException("expected!") {
				public synchronized Throwable fillInStackTrace() {
					return null;
				}
			};
		}
	}

	protected static class DictionaryAndMapCustomListener {
		public void registered(Object service, Dictionary properties) {
			JustListener.REG_CALLS++;
		}

		public void unregistered(Object service, Map props) {
			JustListener.UNREG_CALLS++;
		}

		public void unregistered(Object service, Dictionary props) throws Exception {
			JustListener.UNREG_CALLS++;
		}
	}

	protected static class JustReg {

		private void myReg(Object service, Map properties) {
			JustListener.REG_CALLS++;
		}
	}

	protected static class JustUnreg {

		protected void myUnreg(Object service, Map properties) {
			JustListener.UNREG_CALLS++;
		}
	}

	private OsgiServiceRegistrationListenerAdapter listener;

	private Map props;
	
	private static final String BEAN_NAME = "bla";

	protected void setUp() throws Exception {
		JustListener.REG_CALLS = 0;
		JustListener.UNREG_CALLS = 0;

		CustomListener.REG_CALLS = 0;
		CustomListener.UNREG_CALLS = 0;
		CustomListener.REG_PROPS = new ArrayList();
		CustomListener.UNREG_PROPS = new ArrayList();
		props = new MapBasedDictionary(0);
	}

	protected void tearDown() throws Exception {
		listener = null;
		CustomListener.REG_PROPS = null;
		CustomListener.UNREG_PROPS = null;
		props = null;
	}

	public void testWrapperOverListener() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter();
		listener.setTarget(new JustListener());
		listener.setBeanFactory(createMockBF());
		listener.afterPropertiesSet();

		Object service = new Object();
		assertEquals(0, JustListener.REG_CALLS);
		listener.registered(service, props);
		assertEquals(1, JustListener.REG_CALLS);
		assertEquals(0, JustListener.UNREG_CALLS);
		listener.registered(service, props);
		assertEquals(2, JustListener.REG_CALLS);
		assertEquals(0, JustListener.UNREG_CALLS);

		listener.unregistered(service, props);
		assertEquals(1, JustListener.UNREG_CALLS);
		assertEquals(2, JustListener.REG_CALLS);
		listener.unregistered(service, props);
		assertEquals(2, JustListener.UNREG_CALLS);
		assertEquals(2, JustListener.REG_CALLS);
	}

	public void testWrapperOverNoInvalidClass() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter();
		listener.setTarget(new Object());
		try {
			listener.afterPropertiesSet();
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testWrapperWithIncorrectCustomMethodNames() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter();
		listener.setTarget(new Object());
		listener.setRegistrationMethod("pop");
		listener.setUnregistrationMethod("corn");

		try {
			listener.afterPropertiesSet();
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testWrapperWithCorrectCustomMethodNamesButIncorrectArgumentTypes() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter();
		listener.setTarget(new CustomListener());
		listener.setRegistrationMethod("wrongReg");
		listener.setUnregistrationMethod("wrongUnreg");

		assertEquals(0, CustomListener.REG_CALLS);
		assertEquals(0, CustomListener.UNREG_CALLS);
		try {
			listener.afterPropertiesSet();
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}

		assertEquals(0, CustomListener.REG_CALLS);
		assertEquals(0, CustomListener.UNREG_CALLS);

	}

	public void testWrapperWithCustomMethods() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter();
		listener.setTarget(new CustomListener());
		listener.setRegistrationMethod("myReg");
		listener.setUnregistrationMethod("myUnreg");
		listener.setBeanFactory(createMockBF());
		listener.afterPropertiesSet();

		Map props = Collections.EMPTY_MAP;
		assertEquals(0, CustomListener.REG_CALLS);
		assertEquals(0, CustomListener.UNREG_CALLS);

		Object service = new Object();

		listener.registered(service, props);
		assertEquals(1, CustomListener.REG_CALLS);
		assertEquals(0, CustomListener.UNREG_CALLS);

		listener.registered(service, props);
		assertEquals(2, CustomListener.REG_CALLS);
		assertEquals(0, CustomListener.UNREG_CALLS);

		listener.unregistered(service, props);
		assertEquals(2, CustomListener.REG_CALLS);
		assertEquals(1, CustomListener.UNREG_CALLS);

		listener.unregistered(service, props);
		assertEquals(2, CustomListener.REG_CALLS);
		assertEquals(2, CustomListener.UNREG_CALLS);
	}

	public void testWrapperWithCustomMethodsAndNullProperties() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter();
		listener.setTarget(new CustomListener());
		listener.setRegistrationMethod("myReg");
		listener.setUnregistrationMethod("myUnreg");
		listener.setBeanFactory(createMockBF());
		listener.afterPropertiesSet();
		Object service = new Object();
		assertEquals(0, CustomListener.REG_CALLS);
		assertEquals(0, CustomListener.UNREG_CALLS);
		listener.registered(service, null);
		assertEquals("null properties allowed", 1, CustomListener.REG_CALLS);

		listener.unregistered(service, null);

		assertEquals("null properties allowed", 1, CustomListener.UNREG_CALLS);
	}

	public void testWrapperWithBothCustomAndInterfaceMethods() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter();
		listener.setTarget(new CustomAndListener());
		listener.setRegistrationMethod("aReg");
		listener.setUnregistrationMethod("aUnreg");
		listener.setBeanFactory(createMockBF());
		listener.afterPropertiesSet();
		Object service = new Object();
		assertEquals(0, CustomAndListener.REG_CALLS);
		assertEquals(0, CustomAndListener.UNREG_CALLS);
		listener.registered(service, props);
		assertEquals(2, CustomAndListener.REG_CALLS);
		assertEquals(0, CustomAndListener.UNREG_CALLS);

		listener.unregistered(service, props);
		assertEquals(2, CustomAndListener.REG_CALLS);
		assertEquals(2, CustomAndListener.UNREG_CALLS);

	}

	public void testExceptionOnListenerMethod() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter();
		listener.setTarget(new ExceptionListener());
		listener.setRegistrationMethod("aReg");
		listener.setUnregistrationMethod("aUnreg");
		listener.setBeanFactory(createMockBF());
		listener.afterPropertiesSet();
		Object service = new Object();
		assertEquals(0, JustListener.REG_CALLS);
		assertEquals(0, JustListener.UNREG_CALLS);
		listener.registered(service, props);
		assertEquals(1, JustListener.REG_CALLS);
		assertEquals(0, JustListener.UNREG_CALLS);

		listener.unregistered(service, props);
		assertEquals(1, JustListener.REG_CALLS);
		assertEquals(1, JustListener.UNREG_CALLS);
	}

	public void testExceptionOnCustomMethods() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter(
			);
		listener.setTarget(new ExceptionCustomListener());
		listener.setRegistrationMethod("myReg");
		listener.setUnregistrationMethod("myUnreg");
		listener.setBeanFactory(createMockBF());
		listener.afterPropertiesSet();
		Object service = new Object();
		assertEquals(0, ExceptionCustomListener.REG_CALLS);
		assertEquals(0, ExceptionCustomListener.UNREG_CALLS);
		listener.registered(service, props);
		assertEquals(1, ExceptionCustomListener.REG_CALLS);
		assertEquals(0, ExceptionCustomListener.UNREG_CALLS);

		listener.unregistered(service, props);
		assertEquals(1, ExceptionCustomListener.REG_CALLS);
		assertEquals(1, ExceptionCustomListener.UNREG_CALLS);
	}

	public void testStandardListenerWithListeningMethodsSpecifiedAsCustomOnes() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter();
		listener.setTarget(new JustListener());
		listener.setRegistrationMethod("registered");
		listener.setUnregistrationMethod("unregistered");
		listener.setBeanFactory(createMockBF());
		listener.afterPropertiesSet();
		Object service = new Object();
		assertEquals(0, JustListener.REG_CALLS);
		listener.registered(service, props);
		// only the interface is being called since the service is null
		assertEquals(2, JustListener.REG_CALLS);

		listener.registered(service, props);
		assertEquals(4, JustListener.REG_CALLS);
	}

	public void testListenerWithOverloadedTypesAndMultipleParameterTypes() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter();
		listener.setTarget(new DictionaryAndMapCustomListener());
		listener.setRegistrationMethod("registered");
		listener.setUnregistrationMethod("unregistered");
		listener.setBeanFactory(createMockBF());
		listener.afterPropertiesSet();
		Object service = new Object();
		assertEquals(0, JustListener.REG_CALLS);
		assertEquals(0, JustListener.UNREG_CALLS);
		listener.registered(service, props);

		assertEquals(1, JustListener.REG_CALLS);
		assertEquals(0, JustListener.UNREG_CALLS);

		listener.unregistered(service, props);
		assertEquals(1, JustListener.REG_CALLS);
		assertEquals("only one unregistered method should be called", 1, JustListener.UNREG_CALLS);
	}

	public void testJustCustomRegMethod() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter();
		listener.setTarget(new JustReg());
		listener.setRegistrationMethod("myReg");
		listener.setBeanFactory(createMockBF());
		listener.afterPropertiesSet();
		Object service = new Object();
		assertEquals(0, JustListener.REG_CALLS);
		assertEquals(0, JustListener.UNREG_CALLS);

		listener.registered(service, props);

		assertEquals(1, JustListener.REG_CALLS);
		assertEquals(0, JustListener.UNREG_CALLS);
	}

	public void testJustCustomUnregMethod() throws Exception {
		listener = new OsgiServiceRegistrationListenerAdapter();
		listener.setTarget(new JustUnreg());
		listener.setUnregistrationMethod("myUnreg");
		listener.setBeanFactory(createMockBF());
		listener.afterPropertiesSet();
		Object service = new Object();
		assertEquals(0, JustListener.REG_CALLS);
		assertEquals(0, JustListener.UNREG_CALLS);

		listener.unregistered(service, props);

		assertEquals(0, JustListener.REG_CALLS);
		assertEquals(1, JustListener.UNREG_CALLS);
	}
	
	private BeanFactory createMockBF() {
		MockControl ctrl = MockControl.createNiceControl(BeanFactory.class);
		BeanFactory cbf = (BeanFactory) ctrl.getMock();

		//ctrl.expectAndReturn(cbf.getBean(BEAN_NAME), target);
		//ctrl.expectAndReturn(cbf.getType(BEAN_NAME), target.getClass());

		ctrl.replay();
		return cbf;
	}
}
