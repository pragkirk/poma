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

package org.springframework.osgi.iandt.syntheticEvents;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.util.OsgiServiceUtils;

/**
 * Integration test for synthetic events delivery of service listeners during
 * startup/shutdown.
 * 
 * @author Costin Leau
 * 
 */
public class ServiceListenerSyntheticEvents extends BaseIntegrationTest {

	private Shape area, rectangle, polygon;

	private ServiceRegistration areaReg, rectangleReg, polygonReg;

	private OsgiBundleXmlApplicationContext appCtx;

	private static List referenceBindServices, referenceUnbindServices;

	private static List collectionBindServices, collectionUnbindServices;


	public static class ReferenceListener {

		public void bind(Object service, Map properties) {
			referenceBindServices.add(service.toString());
		};

		public void unbind(Object service, Map properties) {
			referenceUnbindServices.add(service.toString());
		};
	}

	public static class CollectionListener {

		public void bind(Object service, Map properties) {
			collectionBindServices.add(service.toString());
		};

		public void unbind(Object service, Map properties) {
			collectionUnbindServices.add(service.toString());
		};
	}


	// register multiple services of the same type inside OSGi space
	private void registerMultipleServices() {
		area = new Area();
		rectangle = new Rectangle();
		polygon = new Polygon();

		Dictionary polygonProp = new Properties();
		polygonProp.put(Constants.SERVICE_RANKING, new Integer(1));
		// first register polygon
		polygonReg = bundleContext.registerService(Shape.class.getName(), polygon, polygonProp);

		// then rectangle
		Dictionary rectangleProp = new Properties();
		rectangleProp.put(Constants.SERVICE_RANKING, new Integer(10));
		rectangleReg = bundleContext.registerService(Shape.class.getName(), rectangle, rectangleProp);

		// then area
		Dictionary areaProp = new Properties();
		areaProp.put(Constants.SERVICE_RANKING, new Integer(100));
		areaReg = bundleContext.registerService(Shape.class.getName(), area, areaProp);

	}

	protected void onSetUp() {
		referenceBindServices = new ArrayList();
		referenceUnbindServices = new ArrayList();
		collectionBindServices = new ArrayList();
		collectionUnbindServices = new ArrayList();
	}

	protected void onTearDown() {
		OsgiServiceUtils.unregisterService(areaReg);
		OsgiServiceUtils.unregisterService(rectangleReg);
		OsgiServiceUtils.unregisterService(polygonReg);
		try {
			if (appCtx != null)
				appCtx.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		referenceBindServices = null;
		referenceUnbindServices = null;
		collectionBindServices = null;
		collectionUnbindServices = null;
	}

	private void createAppCtx() {
		appCtx = new OsgiBundleXmlApplicationContext(
			new String[] { "/org/springframework/osgi/iandt/syntheticEvents/importers.xml" });
		appCtx.setBundleContext(bundleContext);
		appCtx.refresh();
	}

	// create appCtx each time since we depend we test startup/shutdown behaviour
	// and cannot have shared states
	public void testServiceReferenceEventsOnStartupWithMultipleServicesPresent() throws Exception {
		registerMultipleServices();
		createAppCtx();

		assertEquals("only one service bound at startup", 1, referenceBindServices.size());
		assertEquals("wrong service bound", area.toString(), referenceBindServices.get(0).toString());
	}

	public void testServiceReferenceEventsDuringLifetimeWithMultipleServicesPresent() throws Exception {
		createAppCtx();
		registerMultipleServices();

		assertEquals("multiple services should have been bound during runtime", 3, referenceBindServices.size());
		assertEquals("wrong 1st service bound", polygon.toString(), referenceBindServices.get(0).toString());
		assertEquals("wrong 2nd service bound", rectangle.toString(), referenceBindServices.get(1).toString());
		assertEquals("wrong 3rd service bound", area.toString(), referenceBindServices.get(2).toString());
	}

	public void testServiceReferenceEventsOnShutdownWithMultipleServicesPresent() throws Exception {
		createAppCtx();
		registerMultipleServices();
		appCtx.close();

		assertEquals("only one service unbound at shutdown", 1, referenceUnbindServices.size());
		assertEquals("wrong unbind at shutdown", area.toString(), referenceUnbindServices.get(0).toString());
		appCtx = null;
	}

	public void testServiceCollectionEventsOnStartupWithMultipleServicesPresent() throws Exception {
		registerMultipleServices();
		createAppCtx();

		assertEquals("all services should have been bound at startup", 3, collectionBindServices.size());
		assertEquals("wrong service bound", polygon.toString(), collectionBindServices.get(0).toString());
		assertEquals("wrong service bound", rectangle.toString(), collectionBindServices.get(1).toString());
		assertEquals("wrong service bound", area.toString(), collectionBindServices.get(2).toString());

	}

	public void testServiceCollectionEventsDuringLifetimeWithMultipleServicesPresent() throws Exception {
		createAppCtx();
		registerMultipleServices();

		assertEquals("multiple services should have been bound during runtime", 3, referenceBindServices.size());
		assertEquals("wrong 1st service bound", polygon.toString(), collectionBindServices.get(0).toString());
		assertEquals("wrong 2nd service bound", rectangle.toString(), collectionBindServices.get(1).toString());
		assertEquals("wrong 3rd service bound", area.toString(), collectionBindServices.get(2).toString());
	}

	public void testServiceCollectionEventsOnShutdownWithMultipleServicesPresent() throws Exception {
		createAppCtx();
		registerMultipleServices();
		appCtx.close();

		assertEquals("all services should have been bound at startup", 3, collectionUnbindServices.size());
		assertEquals("wrong 1st service bound", polygon.toString(), collectionUnbindServices.get(0).toString());
		assertEquals("wrong 2nd service bound", rectangle.toString(), collectionUnbindServices.get(1).toString());
		assertEquals("wrong 3rd service bound", area.toString(), collectionUnbindServices.get(2).toString());
		appCtx = null;
	}

}
