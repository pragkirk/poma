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

package org.springframework.osgi.web.extender.internal.scanner;

import org.osgi.framework.Bundle;

/**
 * War scanner. Indicates if the given bundle is a war or not.
 * 
 * @author Costin Leau
 */
public interface WarScanner {

	/**
	 * Indicates if the given bundle is a war or not. Implementations are free
	 * to choose the appropriate strategy.
	 * 
	 * @param bundle OSGi bundle
	 * @return true if the given bundle is a war, false otherwise
	 */
	boolean isWar(Bundle bundle);
}
