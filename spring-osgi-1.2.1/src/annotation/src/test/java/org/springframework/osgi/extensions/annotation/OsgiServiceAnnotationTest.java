/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.osgi.extensions.annotation;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.SortedSet;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.service.importer.support.ImportContextClassLoader;
import org.springframework.osgi.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

/**
 * @author Andy Piper
 */
public class OsgiServiceAnnotationTest extends TestCase {

	private ServiceReferenceInjectionBeanPostProcessor processor;

	private BundleContext context;


	protected void setUp() throws Exception {
		super.setUp();
		processor = new ServiceReferenceInjectionBeanPostProcessor();
		context = new MockBundleContext();
		processor.setBundleContext(context);
		processor.setBeanClassLoader(getClass().getClassLoader());
		MockControl factoryControl = MockControl.createControl(BeanFactory.class);
		BeanFactory factory = (BeanFactory) factoryControl.getMock();
		processor.setBeanFactory(factory);
	}

	protected void tearDown() throws Exception {
	}

	/**
	 * Disabled since it doesn't work as we can't proxy final classes.
	 */
	public void tstGetServicePropertySetters() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setStringType", new Class[] { String.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);

		processor.getServiceProperty(pfb, ref, setter, null);
		Class[] intfs = (Class[]) getPrivateProperty(pfb, "serviceTypes");
		assertEquals(intfs[0], String.class);

		setter = AnnotatedBean.class.getMethod("setIntType", new Class[] { Integer.TYPE });
		ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);

		pfb = new OsgiServiceProxyFactoryBean();
		processor.getServiceProperty(pfb, ref, setter, null);
		intfs = (Class[]) getPrivateProperty(pfb, "serviceTypes");
		assertEquals(intfs[0], Integer.TYPE);

	}

	public void testGetServicePropertyCardinality() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithCardinality1_1",
			new Class[] { AnnotatedBean.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter, null);
		assertTrue(pfb.getCardinality().isMandatory());

		setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithCardinality0_1",
			new Class[] { AnnotatedBean.class });
		ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		pfb = new OsgiServiceProxyFactoryBean();
		processor.getServiceProperty(pfb, ref, setter, null);
		assertFalse(pfb.getCardinality().isMandatory());
	}

	public void testProperMultiCardinality() throws Exception {
		OsgiServiceCollectionProxyFactoryBean pfb = new OsgiServiceCollectionProxyFactoryBean();

		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithCardinality0_N",
			new Class[] { List.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter, null);
		assertFalse(pfb.getCardinality().isMandatory());

		setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithCardinality1_N",
			new Class[] { SortedSet.class });
		ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		pfb = new OsgiServiceCollectionProxyFactoryBean();
		processor.getServiceProperty(pfb, ref, setter, null);
		assertTrue(pfb.getCardinality().isMandatory());
	}

	public void testErrorMultiCardinality() throws Exception {
		OsgiServiceCollectionProxyFactoryBean pfb = new OsgiServiceCollectionProxyFactoryBean();

		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanErrorTypeWithCardinality1_N",
			new Class[] { SortedSet.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		pfb = new OsgiServiceCollectionProxyFactoryBean();
		try {
			processor.getServiceProperty(pfb, ref, setter, null);
			fail("IllegalArgumentException should have been thrown");
		}
		catch (Exception e) {
		}
	}

	public void testGetServicePropertyClassloader() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithClassLoaderClient",
			new Class[] { AnnotatedBean.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter, null);
		assertEquals(pfb.getContextClassLoader(), ImportContextClassLoader.CLIENT);

		pfb = new OsgiServiceProxyFactoryBean();
		setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithClassLoaderUmanaged",
			new Class[] { AnnotatedBean.class });
		ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter, null);

		assertEquals(pfb.getContextClassLoader(), ImportContextClassLoader.UNMANAGED);

		pfb = new OsgiServiceProxyFactoryBean();
		setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithClassLoaderServiceProvider",
			new Class[] { AnnotatedBean.class });
		ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter, null);
		assertEquals(pfb.getContextClassLoader(), ImportContextClassLoader.SERVICE_PROVIDER);
	}

	public void testGetServicePropertyBeanName() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithBeanName",
			new Class[] { AnnotatedBean.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter, null);
		String beanName = (String) getPrivateProperty(pfb, "serviceBeanName");
		;
		assertEquals(beanName, "myBean");
	}

	public void testGetServicePropertyFilter() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithFilter",
			new Class[] { AnnotatedBean.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter, null);
		String filter = (String) getPrivateProperty(pfb, "filter");
		;
		assertEquals(filter, "(wooey=fooo)");
	}

	public void testGetServicePropertyServiceClass() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithServiceType",
			new Class[] { AnnotatedBean.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter, null);
		Class[] intfs = (Class[]) getPrivateProperty(pfb, "interfaces");
		assertEquals(intfs[0], Object.class);
	}

	public void testGetServicePropertyComplex() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeComplex",
			new Class[] { AnnotatedBean.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter, null);
		Class[] intfs = (Class[]) getPrivateProperty(pfb, "interfaces");
		String filter = (String) getPrivateProperty(pfb, "filter");
		String beanName = (String) getPrivateProperty(pfb, "serviceBeanName");
		assertEquals(intfs[0], AnnotatedBean.class);
		assertFalse(pfb.getCardinality().isMandatory());
		assertEquals(ImportContextClassLoader.SERVICE_PROVIDER, pfb.getContextClassLoader());
		assertEquals(filter, "(id=fooey)");
		assertEquals(beanName, "myBean");
	}

	public void testServiceBeanInjection() throws Exception {
		ServiceBean bean = new ServiceBean();
		final MyService bean1 = new MyService() {

			public Object getId() {
				return this;
			}
		};
		final Serializable bean2 = new Serializable() {

			public String toString() {
				return "bean2";
			}
		};

		BundleContext context = new MockBundleContext() {

			public Object getService(org.osgi.framework.ServiceReference reference) {
				String clazz = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
				if (clazz == null)
					return null;
				else if (clazz.equals(MyService.class.getName())) {
					return bean1;
				}
				else if (clazz.equals(Serializable.class.getName())) {
					return bean2;
				}
				return null;
			}

		};

		ServiceReferenceInjectionBeanPostProcessor p = new ServiceReferenceInjectionBeanPostProcessor();
		p.setBundleContext(context);
		p.setBeanClassLoader(getClass().getClassLoader());
		MockControl factoryControl = MockControl.createControl(BeanFactory.class);
		BeanFactory factory = (BeanFactory) factoryControl.getMock();
		factoryControl.expectAndReturn(factory.containsBean("&myBean"), true);
		factoryControl.replay();
		p.setBeanFactory(factory);

		p.postProcessAfterInitialization(bean, "myBean");
		assertSame(bean1.getId(), bean.getServiceBean().getId());
		assertSame(bean2.toString(), bean.getSerializableBean().toString());

		factoryControl.verify();
	}

	public void testServiceFactoryBeanNotInjected() throws Exception {
		ServiceFactoryBean bean = new ServiceFactoryBean();
		final MyService bean1 = new MyService() {

			public Object getId() {
				return this;
			}
		};
		final Serializable bean2 = new Serializable() {

			public String toString() {
				return "bean2";
			}
		};

		BundleContext context = new MockBundleContext() {

			public Object getService(org.osgi.framework.ServiceReference reference) {
				String clazz = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
				if (clazz == null)
					return null;
				else if (clazz.equals(MyService.class.getName())) {
					return bean1;
				}
				else if (clazz.equals(Serializable.class.getName())) {
					return bean2;
				}
				return null;
			}

		};

		ServiceReferenceInjectionBeanPostProcessor p = new ServiceReferenceInjectionBeanPostProcessor();
		p.setBundleContext(context);
		p.postProcessAfterInitialization(bean, "myBean");
		assertNull(bean.getServiceBean());
		assertNull(bean.getSerializableBean());
	}

	public void testServiceFactoryBeanInjected() throws Exception {
		ServiceFactoryBean bean = new ServiceFactoryBean();
		final MyService bean1 = new MyService() {

			public Object getId() {
				return this;
			}
		};
		final Serializable bean2 = new Serializable() {

			public String toString() {
				return "bean2";
			}
		};

		BundleContext context = new MockBundleContext() {

			public Object getService(org.osgi.framework.ServiceReference reference) {
				String clazz = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
				if (clazz == null)
					return null;
				else if (clazz.equals(MyService.class.getName())) {
					return bean1;
				}
				else if (clazz.equals(Serializable.class.getName())) {
					return bean2;
				}
				return null;
			}

		};

		ServiceReferenceInjectionBeanPostProcessor p = new ServiceReferenceInjectionBeanPostProcessor();
		p.setBundleContext(context);
		p.setBeanClassLoader(getClass().getClassLoader());
		PropertyValues pvs = p.postProcessPropertyValues(new MutablePropertyValues(), new PropertyDescriptor[] {
			new PropertyDescriptor("serviceBean", ServiceFactoryBean.class),
			new PropertyDescriptor("serializableBean", ServiceFactoryBean.class) }, bean, "myBean");

		MyService msb = (MyService) pvs.getPropertyValue("serviceBean").getValue();
		Serializable ssb = (Serializable) pvs.getPropertyValue("serializableBean").getValue();

		assertNotNull(msb);
		assertNotNull(ssb);

		assertSame(bean1.getId(), msb.getId());
		assertSame(bean2.toString(), ssb.toString());
	}

	public void testServiceBeanInjectedValues() throws Exception {
		ServiceBean bean = new ServiceBean();
		final MyService bean1 = new MyService() {

			public Object getId() {
				return this;
			}
		};
		final Serializable bean2 = new Serializable() {

			public String toString() {
				return "bean2";
			}
		};

		BundleContext context = new MockBundleContext() {

			public Object getService(org.osgi.framework.ServiceReference reference) {
				String clazz = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
				if (clazz == null)
					return null;
				else if (clazz.equals(MyService.class.getName())) {
					return bean1;
				}
				else if (clazz.equals(Serializable.class.getName())) {
					return bean2;
				}
				return null;
			}

		};

		ServiceReferenceInjectionBeanPostProcessor p = new ServiceReferenceInjectionBeanPostProcessor();
		p.setBundleContext(context);
		p.setBeanClassLoader(getClass().getClassLoader());
		PropertyValues pvs = p.postProcessPropertyValues(new MutablePropertyValues(), new PropertyDescriptor[] {
			new PropertyDescriptor("serviceBean", ServiceBean.class),
			new PropertyDescriptor("serializableBean", ServiceBean.class) }, bean, "myBean");

		MyService msb = (MyService) pvs.getPropertyValue("serviceBean").getValue();
		Serializable ssb = (Serializable) pvs.getPropertyValue("serializableBean").getValue();

		assertNotNull(msb);
		assertNotNull(ssb);

		assertSame(bean1.getId(), msb.getId());
		assertSame(bean2.toString(), ssb.toString());
	}

	protected Object getPrivateProperty(final Object target, final String fieldName) {
		final Field foundField[] = new Field[1];

		ReflectionUtils.doWithFields(target.getClass(), new FieldCallback() {

			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				field.setAccessible(true);
				foundField[0] = field;
			}

		}, new FieldFilter() {

			public boolean matches(Field field) {
				return fieldName.equals(field.getName());
			}

		});

		try {
			return foundField[0].get(target);
		}
		catch (Exception ex) {
			// translate
			throw new RuntimeException(ex);
		}
	}
}