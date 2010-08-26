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

package org.springframework.osgi.iandt.cycles;

import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;

/**
 * @author Costin Leau
 * 
 */
public class ExporterCycleTest extends AbstractConfigurableBundleCreatorTests {

	private ListenerA listenerA;
	private ListenerB listenerB;
	private ServiceRegistration registration;


	protected String[] getConfigLocations() {
		return new String[] { "/org/springframework/osgi/iandt/cycles/top-level-exporter.xml" };
	}

	public void testListenerA() throws Exception {
		assertSame(registration, listenerA.getTarget());
	}
	
	public void testListenerB() throws Exception {
		assertSame(registration, listenerB.getTarget());
	}

	
	public void testListenersBetweenThem() throws Exception {
		assertSame(listenerB.getTarget(), listenerA.getTarget());
	}


	public void setListenerA(ListenerA listener) {
		this.listenerA = listener;
	}

	public void setListenerB(ListenerB nestedListener) {
		this.listenerB = nestedListener;
	}

	public void setRegistration(ServiceRegistration registration) {
		this.registration = registration;
	}

}
