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

package org.springframework.osgi.extender.internal.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEvent;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextListener;
import org.springframework.osgi.context.event.OsgiBundleContextClosedEvent;
import org.springframework.osgi.context.event.OsgiBundleContextFailedEvent;
import org.springframework.osgi.context.event.OsgiBundleContextRefreshedEvent;
import org.springframework.osgi.extender.internal.activator.ContextLoaderListener;

/**
 * Default application context event logger. Logs (using the
 * {@link ContextLoaderListener} logger, the events received.
 * 
 * @author Costin Leau
 * @author Andy Piper
 */
public class DefaultOsgiBundleApplicationContextListener implements OsgiBundleApplicationContextListener {

	/** logger */
	private static final Log log = LogFactory.getLog(ContextLoaderListener.class);


	public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
		String applicationContextString = event.getApplicationContext().getDisplayName();

		if (event instanceof OsgiBundleContextRefreshedEvent) {
			log.info("Application context successfully refreshed (" + applicationContextString + ")");
		}

		if (event instanceof OsgiBundleContextFailedEvent) {
			OsgiBundleContextFailedEvent failureEvent = (OsgiBundleContextFailedEvent) event;
			log.error("Application context refresh failed (" + applicationContextString + ")",
				failureEvent.getFailureCause());

		}

		if (event instanceof OsgiBundleContextClosedEvent) {
			OsgiBundleContextClosedEvent closedEvent = (OsgiBundleContextClosedEvent) event;
			Throwable error = closedEvent.getFailureCause();

			if (error == null) {
				log.info("Application context succesfully closed (" + applicationContextString + ")");
			}
			else {
				log.error("Application context close failed (" + applicationContextString + ")", error);
			}
		}
	}
}