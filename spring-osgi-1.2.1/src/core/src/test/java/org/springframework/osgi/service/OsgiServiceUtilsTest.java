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
 *
 * Created on 25-Jan-2006 by Adrian Colyer
 */
package org.springframework.osgi.service;

import java.io.Serializable;
import java.util.Arrays;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author Adrian Colyer
 * @author Costin Leau
 * @since 2.0
 */
public class OsgiServiceUtilsTest extends TestCase {

	private MockControl mockControl;

	private BundleContext bundleContext;

	protected void setUp() throws Exception {
		super.setUp();
		this.mockControl = MockControl.createControl(BundleContext.class);
		this.bundleContext = (BundleContext) this.mockControl.getMock();
	}

//	public void testGetServiceWithBadFilter() throws InvalidSyntaxException {
//		this.bundleContext.getServiceReferences(ApplicationContext.class.getName(), "junk-filter");
//		this.mockControl.setThrowable(new InvalidSyntaxException("aaa", "xxx"));
//		this.mockControl.replay();
//		try {
//			OsgiServiceUtils.getService(this.bundleContext, ApplicationContext.class, "junk-filter");
//			fail("Should throw IllegalArgumentException");
//		}
//		catch (IllegalArgumentException ex) {
//			assertEquals("aaa", ex.getMessage());
//		}
//		this.mockControl.verify();
//	}
//
//	public void testGetServiceWithOneMatch() throws InvalidSyntaxException {
//		this.bundleContext.getServiceReferences(ApplicationContext.class.getName(), "");
//		ServiceReference sRef = getServiceReference();
//		ServiceReference[] refs = new ServiceReference[] { sRef };
//		this.mockControl.setReturnValue(refs);
//		this.mockControl.replay();
//		ServiceReference ret = OsgiServiceUtils.getService(this.bundleContext, ApplicationContext.class, "");
//		this.mockControl.verify();
//		assertSame("Should get the reference returned by osgi", sRef, ret);
//	}
//
//	public void testGetServiceWithNoMatches() throws InvalidSyntaxException {
//		this.bundleContext.getServiceReferences(ApplicationContext.class.getName(), "");
//		this.mockControl.setReturnValue(new ServiceReference[0]);
//		this.mockControl.replay();
//		try {
//			OsgiServiceUtils.getService(this.bundleContext, ApplicationContext.class, "");
//			fail("Expecting NoSuchServiceException");
//		}
//		catch (NoSuchServiceException ex) {
//			assertTrue("Should contain message about the service type we tried to look up", ex.getMessage().startsWith(
//					"A service of type 'org.springframework.context.ApplicationContext'"));
//			assertEquals("serviceType should be ApplicationContext", ApplicationContext.class, ex.getServiceType());
//			assertEquals("filter should be empty", "", ex.getFilter());
//		}
//		this.mockControl.verify();
//	}
//
//	public void testGetServiceWithMultipleMatches() throws InvalidSyntaxException {
//		this.bundleContext.getServiceReferences(ApplicationContext.class.getName(), "");
//		this.mockControl.setReturnValue(new ServiceReference[2]);
//		this.mockControl.replay();
//		try {
//			OsgiServiceUtils.getService(this.bundleContext, ApplicationContext.class, "");
//			fail("Expecting AmbiguousServiceReferencepException");
//		}
//		catch (AmbiguousServiceReferenceException ex) {
//			assertTrue("Should contain message about the service type we tried to look up", ex.getMessage().startsWith(
//					"Found 2 services of type"));
//			assertEquals("serviceType should be ApplicationContext", ApplicationContext.class, ex.getServiceType());
//			assertEquals("filter should be empty", "", ex.getFilter());
//		}
//		this.mockControl.verify();
//	}
//
//	public void testGetServicesWithBadFilter() throws InvalidSyntaxException {
//		this.bundleContext.getServiceReferences(ApplicationContext.class.getName(), "junk-filter");
//		this.mockControl.setThrowable(new InvalidSyntaxException("aaa", "xxx"));
//		this.mockControl.replay();
//		try {
//			OsgiServiceUtils.getServices(this.bundleContext, ApplicationContext.class, "junk-filter");
//			fail("Should throw IllegalArgumentException");
//		}
//		catch (IllegalArgumentException ex) {
//			assertEquals("aaa", ex.getMessage());
//		}
//		this.mockControl.verify();
//	}
//
//	public void testGetServicesWithNoMatches() throws InvalidSyntaxException {
//		this.bundleContext.getServiceReferences(ApplicationContext.class.getName(), "");
//		this.mockControl.setReturnValue(new ServiceReference[0]);
//		this.mockControl.replay();
//		ServiceReference[] ret = OsgiServiceUtils.getServices(this.bundleContext, ApplicationContext.class, "");
//		this.mockControl.verify();
//		assertEquals("no services should be found", 0, ret.length);
//	}
//
//	public void testGetServicesWithMatches() throws InvalidSyntaxException {
//		this.bundleContext.getServiceReferences(ApplicationContext.class.getName(), "");
//		ServiceReference[] sRefs = new ServiceReference[2];
//		sRefs[0] = getServiceReference();
//		sRefs[1] = getServiceReference();
//		this.mockControl.setReturnValue(sRefs);
//		this.mockControl.replay();
//		ServiceReference[] ret = OsgiServiceUtils.getServices(this.bundleContext, ApplicationContext.class, "");
//		this.mockControl.verify();
//		assertEquals("2 services should be found", 2, ret.length);
//		assertSame(sRefs[0], ret[0]);
//		assertSame(sRefs[1], ret[1]);
//	}

	private ServiceReference getServiceReference() {
		MockControl sRefControl = MockControl.createNiceControl(ServiceReference.class);
		return (ServiceReference) sRefControl.getMock();
	}

	public void testSimpleClassDetermination() throws Exception {
		Class[] classes = new Class[] { Object.class, Serializable.class, Cloneable.class };
		Class[] expected = new Class[] { Serializable.class, Cloneable.class };
		Class[] clazz = org.springframework.osgi.util.internal.ClassUtils.removeParents(classes);

		assertTrue(Arrays.equals(expected, clazz));
	}

	public void testIntefacesAlreadyContainedInTheSpecifiedClass() throws Exception {
		Class[] classes = new Class[] { Serializable.class, Number.class, Comparable.class, Object.class };
		Class[] expected = new Class[] { Number.class, Comparable.class };
		Class[] clazz = org.springframework.osgi.util.internal.ClassUtils.removeParents(classes);
		assertTrue(Arrays.equals(expected, clazz));
	}

	public void testMultipleClassesAndInterfaces() throws Exception {
		Class[] classes = new Class[] { Serializable.class, Number.class, Comparable.class, Object.class, Long.class,
				Integer.class };
		Class[] expected = new Class[] { Long.class, Integer.class };
		Class[] clazz = org.springframework.osgi.util.internal.ClassUtils.removeParents(classes);
		assertTrue(Arrays.equals(expected, clazz));
	}

	public void tstProxyCreation() throws Exception {
		ProxyFactory pf = new ProxyFactory();
		pf.setInterfaces(new Class[] { Serializable.class, Comparable.class });
		//pf.setTargetClass(Number.class);
		pf.setProxyTargetClass(true);
		Object proxy = pf.getProxy();
		System.out.println(ObjectUtils.nullSafeToString(ClassUtils.getAllInterfaces(proxy)));
		assertTrue(proxy instanceof Number);
	}
}
