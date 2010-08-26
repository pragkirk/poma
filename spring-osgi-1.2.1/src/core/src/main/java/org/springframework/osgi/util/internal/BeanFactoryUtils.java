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

package org.springframework.osgi.util.internal;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;

/**
 * Utility class for beans operations.
 * 
 * @author Costin Leau
 * 
 */
public abstract class BeanFactoryUtils {

	/**
	 * Return all beans depending directly or indirectly (transitively), on the
	 * bean identified by the beanName. When dealing with a FactoryBean, the
	 * factory itself can be returned or its product. Additional filtering can
	 * be executed through the type parameter. If no filtering is required, then
	 * null can be passed.
	 * 
	 * Note that depending on #rawFactoryBeans parameter, the type of the
	 * factory or its product can be used when doing the filtering.
	 * 
	 * @param beanFactory beans bean factory
	 * @param beanName root bean name
	 * @param rawFactoryBeans consider the factory bean itself or the its
	 * product
	 * @param type type of the beans returned (null to return all beans)
	 * @return bean names
	 */
	public static String[] getTransitiveDependenciesForBean(ConfigurableListableBeanFactory beanFactory,
			String beanName, boolean rawFactoryBeans, Class type) {
		Assert.notNull(beanFactory);
		Assert.hasText(beanName);

		Assert.isTrue(beanFactory.containsBean(beanName), "no bean by name [" + beanName + "] can be found");

		Set beans = new LinkedHashSet();

		getTransitiveBeans(beanFactory, beanName, rawFactoryBeans, beans);

		if (type != null) {
			// filter by type
			for (Iterator iter = beans.iterator(); iter.hasNext();) {
				String bean = (String) iter.next();
				if (!beanFactory.isTypeMatch(bean, type)) {
					iter.remove();
				}
			}
		}

		return (String[]) beans.toArray(new String[beans.size()]);
	}

	private static void getTransitiveBeans(ConfigurableListableBeanFactory beanFactory, String beanName,
			boolean rawFactoryBeans, Set beanNames) {
		String transformedBeanName = org.springframework.beans.factory.BeanFactoryUtils.transformedBeanName(beanName);
		// strip out '&' just in case
		String[] beans = beanFactory.getDependenciesForBean(transformedBeanName);

		for (int i = 0; i < beans.length; i++) {
			String bean = beans[i];
			// named nested beans are considered as well, filter them out
			if (beanFactory.containsBean(bean)) {
				// & if needed
				if (rawFactoryBeans && beanFactory.isFactoryBean(bean))
					bean = BeanFactory.FACTORY_BEAN_PREFIX + beans[i];

				if (!beanNames.contains(bean)) {
					beanNames.add(bean);
					getTransitiveBeans(beanFactory, bean, rawFactoryBeans, beanNames);
				}
			}
		}
	}
}
