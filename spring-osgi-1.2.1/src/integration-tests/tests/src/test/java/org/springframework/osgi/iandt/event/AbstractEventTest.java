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

import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEvent;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextListener;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.osgi.util.OsgiServiceUtils;

/**
 * @author Costin Leau
 * 
 */
public abstract class AbstractEventTest extends BaseIntegrationTest {

	protected OsgiBundleApplicationContextListener listener;

	private ServiceRegistration registration;
	/** list of events */
	protected List eventList = Collections.synchronizedList(new ArrayList());;
	/** lock */
	protected final Object lock = new Object();

	/** wait X minutes max */
	protected final long TIME_OUT = 3* 60 * 1000;


	protected void onSetUp() throws Exception {
		eventList.clear();

		listener = new OsgiBundleApplicationContextListener() {

			public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
				eventList.add(event);
				synchronized (lock) {
					lock.notify();
				}
			}
		};
	}

	protected void onTearDown() throws Exception {
		OsgiServiceUtils.unregisterService(registration);
		eventList.clear();
	}

	protected void registerEventListener() {
		// publish listener
		registration = bundleContext.registerService(
			new String[] { OsgiBundleApplicationContextListener.class.getName() }, listener, null);
	}

	/**
	 * Returns true if the wait ended through a notification, false otherwise.
	 * 
	 * @param maxWait
	 * @return
	 * @throws Exception
	 */
	protected boolean waitForEvent(long maxWait) {
		long start = System.currentTimeMillis();
		synchronized (lock) {
			try {
				lock.wait(maxWait);
			}
			catch (Exception ex) {
				return false;
			}
		}
		long stop = System.currentTimeMillis();
		boolean waitSuccessed = (stop - start <= maxWait);
		return waitSuccessed;
	}
}
