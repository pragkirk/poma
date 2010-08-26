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

package org.springframework.osgi.context;

import org.osgi.framework.BundleContext;

/**
 * Interface that enables beans to find the bundle context they are defined in.
 * 
 * Note that in most circumstances there is no need for a bean to implement this
 * interface.
 * 
 * @author Adrian Colyer
 */
public interface BundleContextAware {

	/**
	 * Set the {@link BundleContext} that this bean runs in. Normally this can
	 * be used to initialize an object.
	 * 
	 * @param bundleContext the <code>BundleContext</code> object to be used
	 * by this object
	 */
	 void setBundleContext(BundleContext bundleContext);
}
