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

package org.springframework.osgi.service.importer.support.internal.controller;

import java.lang.reflect.Field;

import org.springframework.osgi.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;

/**
 * Importer-only delegate (it would be nice to have generics).
 * 
 * @author Costin Leau
 * 
 */
public abstract class ImporterControllerUtils {

	private static final String FIELD_NAME = "controller";

	private static final Field singleProxyField, collectionProxyField;

	static {
		Class clazz = null;
		try {
			clazz = OsgiServiceProxyFactoryBean.class;
			singleProxyField = clazz.getDeclaredField(FIELD_NAME);
			singleProxyField.setAccessible(true);

			clazz = OsgiServiceCollectionProxyFactoryBean.class;
			collectionProxyField = clazz.getDeclaredField(FIELD_NAME);
			collectionProxyField.setAccessible(true);
		}
		catch (NoSuchFieldException ex) {
			throw (RuntimeException) new IllegalStateException("Cannot read field [" + FIELD_NAME + "] on class ["
					+ clazz + "]").initCause(ex);
		}
	}


	public static ImporterInternalActions getControllerFor(Object importer) {
		Field field = (OsgiServiceProxyFactoryBean.class == importer.getClass() ? singleProxyField
				: collectionProxyField);
		try {
			return (ImporterInternalActions) field.get(importer);
		}
		catch (IllegalAccessException iae) {
			throw (RuntimeException) new IllegalArgumentException("Cannot access field [" + FIELD_NAME
					+ "] on object [" + importer + "]").initCause(iae);
		}
	}
}
