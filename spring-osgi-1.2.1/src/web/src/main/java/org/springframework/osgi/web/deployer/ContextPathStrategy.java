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

package org.springframework.osgi.web.deployer;

import org.osgi.framework.Bundle;

/**
 * Strategy interface that encapsulates the creation of WARs context path.
 * 
 * @author Costin Leau
 * @see Bundle
 */
public interface ContextPathStrategy {

	/**
	 * Obtains the context path for the given OSGi bundle. The returned String
	 * should be not null and should not contain any spaces.
	 * 
	 * @param bundle OSGi bundle deployed as war
	 * @return the not-null context path (without any spaces) associated with
	 * the given bundle
	 */
	String getContextPath(Bundle bundle);
}
