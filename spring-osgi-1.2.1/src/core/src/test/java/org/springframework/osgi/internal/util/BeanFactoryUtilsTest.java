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
package org.springframework.osgi.internal.util;

import java.util.Arrays;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.osgi.util.internal.BeanFactoryUtils;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.util.ObjectUtils;

/**
 * @author Costin Leau
 * 
 */
public class BeanFactoryUtilsTest extends AbstractDependencyInjectionSpringContextTests {

	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/osgi/dependingBeans.xml" };
	}

	private ConfigurableListableBeanFactory bf;

	protected void onSetUp() throws Exception {
		bf = applicationContext.getBeanFactory();
	}

	public void testADependencies() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "a", false, null);
		assertTrue(ObjectUtils.isEmpty(deps));
	}

	public void testBDependencies() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "b", false, null);
		assertTrue(ObjectUtils.isEmpty(deps));
	}

	public void testCDependencies() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "c", false, null);
		assertTrue(Arrays.equals(new String[] { "b" }, deps));

	}

	public void testIntDependencies() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "int", false, null);
		assertTrue(Arrays.equals(new String[] { "c", "b" }, deps));
	}

	public void testTransitiveDependenciesForDependsOn() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "thread", false, null);
		assertTrue(Arrays.equals(new String[] { "buffer", "int", "c", "b" }, deps));
	}

	public void testTransitiveFBDependencies() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "secondBuffer", true, null);
		assertTrue(Arrays.equals(new String[] { "&field", "thread", "buffer", "int", "c", "b" }, deps));
	}

	public void testFiltering() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "secondBuffer", true, Number.class);
		assertTrue(Arrays.equals(new String[] { "int", "b" }, deps));
	}

	public void testFilteringOnFB() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "secondBuffer", true, FactoryBean.class);
		assertTrue(Arrays.equals(new String[] { "&field" }, deps));
	}
}
