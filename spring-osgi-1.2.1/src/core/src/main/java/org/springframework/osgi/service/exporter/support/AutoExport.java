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

package org.springframework.osgi.service.exporter.support;

import org.springframework.core.enums.StaticLabeledEnum;
import org.springframework.osgi.util.internal.ClassUtils;

/**
 * Enum-like class indicatin class exporters available to
 * {@link OsgiServiceFactoryBean} for registering object as OSGi services.
 * 
 * @author Costin Leau
 */
public abstract class AutoExport extends StaticLabeledEnum {

	/** Do not export anything */
	public static final AutoExport DISABLED = new AutoExport(0, "DISABLED") {

		private static final long serialVersionUID = -8297270116184239840L;

		private final Class[] clazz = new Class[0];


		Class[] getExportedClasses(Class targetClass) {
			return clazz;
		}
	};

	/**
	 * Export all interfaces (and their hierarchy) implemented by the given
	 * class
	 */
	public static final AutoExport INTERFACES = new AutoExport(1, "INTERFACES") {

		private static final long serialVersionUID = -8336152449611885031L;


		public Class[] getExportedClasses(Class targetClass) {
			return ClassUtils.getClassHierarchy(targetClass, ClassUtils.INCLUDE_INTERFACES);
		}
	};

	/**
	 * Export the class hierarchy (all classes inherited by the given target
	 * excluding Object.class)
	 */
	public static final AutoExport CLASS_HIERARCHY = new AutoExport(2, "CLASS_HIERARCHY") {

		private static final long serialVersionUID = 6464782616822538297L;


		public Class[] getExportedClasses(Class targetClass) {
			return ClassUtils.getClassHierarchy(targetClass, ClassUtils.INCLUDE_CLASS_HIERARCHY);

		}
	};

	/**
	 * Export every class, inherited or implemented by the given target. Similar
	 * to {@link #CLASS_HIERARCHY} + {@link #INTERFACES}
	 */
	public static final AutoExport ALL_CLASSES = new AutoExport(3, "ALL_CLASSES") {

		private static final long serialVersionUID = -6628398711158262852L;


		public Class[] getExportedClasses(Class targetClass) {
			return ClassUtils.getClassHierarchy(targetClass, ClassUtils.INCLUDE_ALL_CLASSES);
		}
	};


	/**
	 * Determines the exported classes given a certain target class.
	 * 
	 * @param targetClass class to be exported into OSGi
	 * @return array of classes that will be published for the OSGi service.
	 */
	abstract Class[] getExportedClasses(Class targetClass);

	/**
	 * Constructs a new <code>AutoExport</code> instance.
	 * 
	 * @param code
	 * @param label
	 */
	private AutoExport(int code, String label) {
		super(code, label);
	}
}
