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
package org.springframework.osgi.extensions.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method (typically a JavaBean setter method) as requiring an OSGi service reference.
 * 
 * @author Andy Piper
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceReference {
    /**
	 * The name of the bean that backs the injected service. May be null.
	 */
	String serviceBeanName() default "";

	/**
	 * The cardinality of the service reference, defaults to mandatory.
	 */
	ServiceReferenceCardinality cardinality() default ServiceReferenceCardinality.C1__1;

    /**
	 * The invocation context classloader setting. Defalts to the classloader of the client.
	 */
	ServiceReferenceClassLoader contextClassLoader() default ServiceReferenceClassLoader.CLIENT;

	/**
	 * Timeout for service resolution in milliseconds.
	 */
	int timeout() default 300000;

	/**
	 * Interface (or class) of the service to be injected
	 */
	Class<?>[] serviceTypes() default ServiceReference.class;

    /**
     * Whether or not to proxy greedily in collection references.
     */
    boolean greedyProxying() default false;

    /**
	 * filter used to narrow service matches, may be null
	 */
	String filter() default "";
}
