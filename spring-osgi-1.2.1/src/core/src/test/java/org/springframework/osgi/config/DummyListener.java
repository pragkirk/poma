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
package org.springframework.osgi.config;

import java.util.Map;

import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;

/**
 * @author Costin Leau
 * 
 */
public class DummyListener implements OsgiServiceLifecycleListener {

	static int BIND_CALLS = 0;
	static int UNBIND_CALLS = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.osgi.service.OsgiServiceLifecycleListener#bind(java.lang.String,
	 *      java.lang.Object)
	 */
	public void bind(Object service, Map props) {
		BIND_CALLS++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.osgi.service.OsgiServiceLifecycleListener#unbind(java.lang.String,
	 *      java.lang.Object)
	 */
	public void unbind(Object service, Map props) {
		UNBIND_CALLS++;
	}

}
