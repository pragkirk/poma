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

import java.awt.Polygon;
import java.awt.Shape;

import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.util.OsgiServiceUtils;

/**
 * @author Costin Leau
 * 
 */
public abstract class BaseImporterCycleTest extends BaseIntegrationTest {

	protected ListenerA listenerA;
	protected ListenerB listenerB;
	private Shape service;
	private ServiceRegistration registration;

	protected void onSetUp() throws Exception {
		service = new Polygon();
		registration = bundleContext.registerService(Shape.class.getName(), service, null);
	}

	protected void onTearDown() throws Exception {
		service = null;
		OsgiServiceUtils.unregisterService(registration);
	}

	public void setListenerA(ListenerA listener) {
		this.listenerA = listener;
	}

	public void setListenerB(ListenerB nestedListener) {
		this.listenerB = nestedListener;
	}
}
