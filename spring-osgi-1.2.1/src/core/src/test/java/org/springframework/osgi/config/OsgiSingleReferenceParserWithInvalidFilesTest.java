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

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.osgi.context.support.BundleContextAwareProcessor;
import org.springframework.osgi.mock.MockBundleContext;

/**
 * @author Costin Leau
 * 
 */
public class OsgiSingleReferenceParserWithInvalidFilesTest extends TestCase {
	private GenericApplicationContext appContext;

	protected void setUp() throws Exception {
		BundleContext bundleContext = new MockBundleContext() {
			// service reference already registered
			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[0];
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());
	}

	protected void tearDown() throws Exception {
		appContext.close();
		appContext = null;
	}

	private void readCtxFromResource(String resourceName) {
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		reader.loadBeanDefinitions(new ClassPathResource(resourceName, getClass()));
		appContext.refresh();
	}

	private void expectException(String resourceName) {
		try {
			readCtxFromResource(resourceName);
			fail("should have thrown parsing exception, invalid resource " + resourceName);
		}
		catch (BeanDefinitionParsingException ex) {
			// expected
		}
	}

	public void testInlineInterfaceAndNestedInterfaces() throws Exception {
		expectException("osgiSingleReferenceInvalidInterface.xml");
	}

	public void testListenerWithNestedDefinitionAndInlinedRefVariant1() throws Exception {
		expectException("osgiSingleReferenceWithInvalidListener1.xml");
	}
	public void testListenerWithNestedDefinitionAndInlinedRefVariant2() throws Exception {
		expectException("osgiSingleReferenceWithInvalidListener2.xml");
	}

}
