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

package org.springframework.osgi.extender;

import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.DelegatedExecutionOsgiBundleApplicationContext;

/**
 * Extender hook for customizing the OSGi application context creation. Each
 * bundle started by the OSGi platform can create (or customize) an application
 * context that becomes managed by the Spring-DM extender.
 * 
 * For example, to create an application context based on the presence of a
 * manifest header, one could use the following code:
 * 
 * <pre class="code">
 * class HeaderBasedAppCtxCreator implements OsgiApplicationContextCreator {
 * 
 * 	/** location header &#42;/
 * 	private static final String HEADER = &quot;Context-Locations&quot;;
 * 
 * 
 * 	public DelegatedExecutionOsgiBundleApplicationContext createApplicationContext(BundleContext bundleContext) {
 * 		Bundle owningBundle = bundleContext.getBundle();
 * 
 * 		Object value = owningBundle.getHeaders().get(HEADER);
 * 		String[] locations = null;
 * 		if (value != null &amp;&amp; value instanceof String) {
 * 			locations = StringUtils.commaDelimitedListToStringArray((String) value);
 * 		}
 * 		else {
 * 			locations = &lt;default values&gt;
 * 		}
 * 
 * 		// create application context from 'locations'  
 * 		
 * 		return applicationContext;	
 * 	}
 * }
 * </pre>
 * 
 * <p/><b>Note:</b> The application contexts should be only created and
 * initialized but not started (i.e. <code>refresh()</code> method should not
 * be called).
 * 
 * <p/>The recommended way of configuring the extender is to attach any relevant
 * <code>OsgiApplicationContextCreator</code> implementation as fragments to
 * extender bundle. Please see the OSGi specification and Spring-DM reference
 * documentation for more information on how to do that.
 * 
 * <p/>Note the extender also supports <code>OsgiBeanFactoryPostProcessor</code>
 * for application context customization.
 * 
 * <p/>The creation of an application context doesn't guarantee that a bundle
 * becomes Spring-DM managed. The Spring-DM extender can do additional post
 * filtering that can discard the bundle (and associated context).
 * 
 * @author Costin Leau
 * 
 */
public interface OsgiApplicationContextCreator {

	/**
	 * Creates an application context for the given bundle context. If no
	 * application context needs to be created, then <code>null</code> should
	 * be returned. Exceptions will be caught and logged but will not prevent the
	 * creation of other application contexts.
	 * 
	 * @param bundleContext OSGi bundle context determining the context creation
	 * @return <code>null</code> if no context should be created, non-<code>null</code>
	 * otherwise
	 * @throws Exception if something goes wrong
	 */
	DelegatedExecutionOsgiBundleApplicationContext createApplicationContext(BundleContext bundleContext)
			throws Exception;
}
