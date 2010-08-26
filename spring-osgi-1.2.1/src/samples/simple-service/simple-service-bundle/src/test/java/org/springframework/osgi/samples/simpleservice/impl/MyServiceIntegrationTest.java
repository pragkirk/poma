/*
 * Copyright 2006 the original author or authors.
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
package org.springframework.osgi.samples.simpleservice.impl;

import org.springframework.osgi.samples.simpleservice.MyService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Integration test the service locally (outside of OSGi).
 * Use AbstractOsgiTests and a separate integration test project
 * for testing inside of OSGi.
 * 
 * @author Adrian Colyer
 */
public class MyServiceIntegrationTest extends AbstractDependencyInjectionSpringContextTests {

	private MyService service;
	
	protected String[] getConfigLocations() {
		return new String[] {"META-INF/spring/simpleservice.xml"};
	}
	
	public void testSimpleService() {
		assertEquals("simple service at your service",
				service.stringValue());
	}

	public void setService(MyService service) {
		this.service = service;
	}
	
}
