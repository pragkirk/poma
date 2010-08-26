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
package org.springframework.osgi.context.support.internal;

import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

public class ScopeTests extends TestCase {

	private static Object tag;

	private static Runnable callback = null;

	private static abstract class AbstractScope implements Scope {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.Scope#getConversationId()
		 */
		public String getConversationId() {
			System.out.println("returning conversation id");
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.Scope#registerDestructionCallback(java.lang.String,
		 * java.lang.Runnable)
		 */
		public void registerDestructionCallback(String name, Runnable cb) {
			System.out.println("registering callback " + cb + " for bean " + name);
			callback = cb;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
		 */
		public Object remove(String name) {
			System.out.println("destroying bean " + name);
			return null;
		}
		
		public Object resolveContextualObject(String key) {
			return null;
		}
	}

	private class FooScope extends AbstractScope {

		public Object get(String name, ObjectFactory objectFactory) {
			System.out.println("tag is " + tag);
			System.out.println("requested " + name + " w/ objFact " + objectFactory);
			if (ScopeTests.tag == null) {
				Object obj = objectFactory.getObject();
				System.out.println("set tag to " + obj);
				System.out.println("obj is " + obj + "|hash=" + System.identityHashCode(obj));
				ScopeTests.tag = obj;
			}

			return tag;
		}

	}

	private DefaultListableBeanFactory bf;

	private class ScopedXmlFactory extends XmlBeanFactory {

		public ScopedXmlFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
			super(resource, parentBeanFactory);
		}

		public ScopedXmlFactory(Resource resource) throws BeansException {
			super(resource);
			registerScope("foo", new FooScope());
			registerScope("bar", new FooScope());
		}

	}

	protected void setUp() throws Exception {
		Resource file = new ClassPathResource("scopes.xml");
		bf = new ScopedXmlFactory(file);

		callback = null;
		tag = null;
	}

	protected void tearDown() throws Exception {
		bf.destroySingletons();
		callback = null;
		tag = null;
	}

	public void testScopes() throws Exception {

		assertNull(tag);
		Object a = bf.getBean("a");
		System.out.println("got a" + a);
		assertNotNull(tag);

		((Properties) a).put("goo", "foo");

		Object b = bf.getBean("b");
		System.out.println("request b;got=" + b);
		System.out.println("b class is" + b.getClass());
		b = bf.getBean("b");
		System.out.println("request b;got=" + b);
		System.out.println("b class is" + b.getClass());

		Object scopedA = bf.getBean("a");
		System.out.println(scopedA.getClass());
		System.out.println(a);
		System.out.println(scopedA);
		System.out.println(ObjectUtils.nullSafeToString(ClassUtils.getAllInterfaces(scopedA)));
	}

	public void testCallback() throws Exception {
		Object a = bf.getBean("a");
		// assertNotNull(callback);
		// Runnable aCallback = callback;
		Properties props = (Properties) a;
		props.put("foo", "bar");

		bf.destroyScopedBean("a");
		
		System.out.println(ObjectUtils.nullSafeToString(bf.getRegisteredScopeNames()));
		//assertTrue(props.isEmpty());
	}
}
