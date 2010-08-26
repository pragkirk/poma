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

package org.springframework.osgi.context.event;

import org.osgi.framework.Bundle;
import org.springframework.context.ApplicationContext;

/**
 * Event raised when an <code>ApplicationContext</code> failed.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiBundleContextFailedEvent extends OsgiBundleApplicationContextEvent {

	private final Throwable cause;


	/**
	 * Constructs a new <code>OsgiBundleContextFailedEvent</code> instance.
	 * 
	 * @param source the <code>ApplicationContext</code> that has failed (must
	 * not be <code>null</code>)
	 * @param bundle the OSGi bundle associated with the source application
	 * context
	 * @param cause optional <code>Throwable</code> indicating the cause of
	 * the failure
	 */
	public OsgiBundleContextFailedEvent(ApplicationContext source, Bundle bundle, Throwable cause) {
		super(source, bundle);
		this.cause = cause;
	}

	/**
	 * Returns the <code>Throwable</code> that caused the application context
	 * to fail.
	 * 
	 * @return the cause of the failure.
	 */
	public final Throwable getFailureCause() {
		return cause;
	}
}
