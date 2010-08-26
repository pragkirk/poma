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

package org.springframework.osgi.extender.support.scanning;

import org.osgi.framework.Bundle;

/**
 * Convenience scanner locating suitable Spring configurations inside an OSGi
 * bundle. This interface can be implemented to customize Spring-DM default
 * definition of a 'Spring-powered' bundle by using different locations or
 * supplying defaults for bundles that do not provide a proper configuration.
 * 
 * <p/> Additionally, non-XML configurations (for example annotation-based) can
 * be plugged in. This would normally imply a custom application context creator
 * as well.
 * 
 * <p/><b>Note:</b>It is strongly recommended that the default locations (<tt>META-INF/spring/*.xml</tt>
 * or <tt>Spring-Context</tt> manifest header) are supported (through chaining
 * or by extending the default implementation) to avoid breaking bundles using
 * them.
 * 
 * <p/>This interface is intended for usage with the default
 * {@link org.springframework.osgi.extender.OsgiApplicationContextCreator}
 * implementation.
 * 
 * @see org.springframework.osgi.extender.support.DefaultOsgiApplicationContextCreator
 * @see org.springframework.osgi.extender.OsgiApplicationContextCreator
 * 
 * @author Costin Leau
 */
public interface ConfigurationScanner {

	/**
	 * Returns an array of existing Spring configuration locations (as Strings)
	 * for the given bundle. If no resource was found, an empty/null array
	 * should be returned.
	 * 
	 * @param bundle non-null bundle intended for scanning
	 * @return Spring configuration locations
	 */
	String[] getConfigurations(Bundle bundle);
}
