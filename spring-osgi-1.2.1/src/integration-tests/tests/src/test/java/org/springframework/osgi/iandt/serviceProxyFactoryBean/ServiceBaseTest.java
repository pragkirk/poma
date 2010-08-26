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

package org.springframework.osgi.iandt.serviceProxyFactoryBean;

import java.util.Dictionary;

import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.iandt.BaseIntegrationTest;

/**
 * @author Costin Leau
 * 
 */
public abstract class ServiceBaseTest extends BaseIntegrationTest {

	protected String[] getTestBundlesNames() {
		return new String[] { "net.sourceforge.cglib, com.springsource.net.sf.cglib, 2.1.3" };
	}

	protected ServiceRegistration publishService(Object obj, String name) throws Exception {
		return bundleContext.registerService(name, obj, null);
	}

	protected ServiceRegistration publishService(Object obj, String names[]) throws Exception {
		return bundleContext.registerService(names, obj, null);
	}

	protected ServiceRegistration publishService(Object obj, String names[], Dictionary dict) throws Exception {
		return bundleContext.registerService(names, obj, null);
	}

	protected ServiceRegistration publishService(Object obj) throws Exception {
		return publishService(obj, obj.getClass().getName());
	}

	protected ServiceRegistration publishService(Object obj, Dictionary dict) throws Exception {
		return bundleContext.registerService(obj.getClass().getName(), obj, dict);
	}

}
