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

package org.springframework.osgi.iandt.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.Bundle;
import org.springframework.core.io.Resource;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEvent;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextListener;
import org.springframework.osgi.extender.event.BootstrappingDependencyEvent;
import org.springframework.osgi.service.importer.OsgiServiceDependency;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyEvent;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyWaitEndedEvent;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyWaitStartingEvent;

/**
 * @author Costin Leau
 * 
 */
public abstract class DepedencyEventTest extends AbstractEventTest {

	private List refreshEvents = Collections.synchronizedList(new ArrayList(10));


	protected void onSetUp() throws Exception {
		refreshEvents.clear();

		// override the listener with another implementation that waits until the appCtx are fully started
		listener = new OsgiBundleApplicationContextListener() {

			public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
				System.out.println("receiving event " + event.getClass());
				if (event instanceof BootstrappingDependencyEvent) {
					eventList.add(event);
				}
				else {
					refreshEvents.add(event);
				}
				synchronized (lock) {
					lock.notify();
				}
			}
		};
	}

	public void testEventsForCtxThatWork() throws Exception {
		// publish listener
		registerEventListener();

		assertTrue("should start with an empty list", eventList.isEmpty());

		// install the dependency bundle
		Resource bundle = getLocator().locateArtifact("org.springframework.osgi.iandt", "dependencies",
			getSpringDMVersion());

		Resource dependency1 = getLocator().locateArtifact("org.springframework.osgi.iandt", "simple.service",
			getSpringDMVersion());

		Resource dependency2 = getLocator().locateArtifact("org.springframework.osgi.iandt", "simple.service2",
			getSpringDMVersion());

		Resource dependency3 = getLocator().locateArtifact("org.springframework.osgi.iandt", "simple.service3",
			getSpringDMVersion());

		Bundle bnd = bundleContext.installBundle(bundle.getURL().toExternalForm());

		// install the bundles but don't start them
		Bundle bnd1 = bundleContext.installBundle(dependency1.getURL().toExternalForm());
		Bundle bnd2 = bundleContext.installBundle(dependency2.getURL().toExternalForm());
		Bundle bnd3 = bundleContext.installBundle(dependency3.getURL().toExternalForm());

		try {

			bnd.start();

			// expect at least 3 events
			while (eventList.size() < 3) {
				if (!waitForEvent(TIME_OUT)) {
					fail("not enough events received after " + TIME_OUT + " ms");
				}
			}

			// check the event type and their name (plus the order)

			// simple service 3
			assertEquals("&simpleService3", getDependencyAt(0).getBeanName());
			assertEquals(OsgiServiceDependencyWaitStartingEvent.class, getNestedEventAt(0).getClass());
			// simple service 2
			assertEquals("&simpleService2", getDependencyAt(1).getBeanName());
			assertEquals(OsgiServiceDependencyWaitStartingEvent.class, getNestedEventAt(0).getClass());
			// simple service 1
			assertEquals("&nested", getDependencyAt(2).getBeanName());
			assertEquals(OsgiServiceDependencyWaitStartingEvent.class, getNestedEventAt(0).getClass());

			waitForContextStartEvent(bnd1);
			assertEquals("&nested", getDependencyAt(3).getBeanName());
			assertEquals(OsgiServiceDependencyWaitEndedEvent.class, getNestedEventAt(3).getClass());

			waitForContextStartEvent(bnd3);
			assertEquals("&simpleService3", getDependencyAt(4).getBeanName());
			assertEquals(OsgiServiceDependencyWaitEndedEvent.class, getNestedEventAt(4).getClass());
			// bnd3 context started event

			waitForContextStartEvent(bnd2);
			assertEquals("&simpleService2", getDependencyAt(5).getBeanName());
			assertEquals(OsgiServiceDependencyWaitEndedEvent.class, getNestedEventAt(5).getClass());
			// bnd2 context started event
			// wait until the bundle fully starts
			waitOnContextCreation("org.springframework.osgi.iandt.dependencies");
			// double check context started event

			// bnd1 context started event
			System.out.println("Refresh events received are " + refreshEvents);

			while (eventList.size() < 3) {
				if (!waitForEvent(TIME_OUT)) {
					fail("not enough events received after " + TIME_OUT + " ms");
				}
			}
			// at least 3 events have to be received
			assertTrue(refreshEvents.size() >= 3);

		}
		finally {
			bnd.uninstall();

			bnd1.uninstall();
			bnd2.uninstall();
			bnd3.uninstall();
		}
	}

	private OsgiServiceDependency getDependencyAt(int index) {
		return getNestedEventAt(index).getServiceDependency();
	}

	private OsgiServiceDependencyEvent getNestedEventAt(int index) {
		Object obj = eventList.get(index);
		System.out.println("received object " + obj.getClass() + "|" + obj);
		BootstrappingDependencyEvent event = (BootstrappingDependencyEvent) obj;
		return event.getDependencyEvent();
	}

	private void waitForContextStartEvent(Bundle bundle) throws Exception {
		int eventNumber = eventList.size();
		bundle.start();
		waitOnContextCreation(bundle.getSymbolicName());
		while (eventList.size() < eventNumber + 1)
			waitForEvent(TIME_OUT);
	}
}
