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
package org.springframework.osgi;

import java.beans.PropertyEditor;
import java.util.Dictionary;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class DictionaryEditorTest extends AbstractDependencyInjectionSpringContextTests {

	private Dictionary dictionary;

	
	protected void prepareTestInstance() throws Exception {
		setAutowireMode(AbstractDependencyInjectionSpringContextTests.AUTOWIRE_BY_NAME);
		super.prepareTestInstance();
	}

	/**
	 * @param dictionary The dictionary to set.
	 */
	public void setDictionary(Dictionary property) {
		this.dictionary = property;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.test.AbstractSingleSpringContextTests#customizeBeanFactory(org.springframework.beans.factory
	 * .support.DefaultListableBeanFactory)
	 */
	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
		// beanFactory.registerCustomEditor(Dictionary.class, new PropertiesEditor());
		beanFactory.registerCustomEditor(Dictionary.class, PropertiesEditor.class);
		super.customizeBeanFactory(beanFactory);
	}

	protected String[] getConfigLocations() {
		return new String[] { "/org/springframework/osgi/dict-editor.xml" };
	}

	public void testInjection() {
		assertNotNull(dictionary);
	}

	public void testInjectedValue() {
		assertSame(applicationContext.getBean("dictionary"), dictionary);
	}
}
