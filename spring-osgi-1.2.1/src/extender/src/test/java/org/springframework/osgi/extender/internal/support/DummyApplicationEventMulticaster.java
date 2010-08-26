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

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;

/**
 * @author Costin Leau
 */
public class DummyApplicationEventMulticaster implements ApplicationEventMulticaster {

	public void addApplicationListener(ApplicationListener arg0) {
	}

	public void multicastEvent(ApplicationEvent arg0) {
	}

	public void removeAllListeners() {
	}

	public void removeApplicationListener(ApplicationListener arg0) {
	}

	public void addApplicationListenerBean(String str) {
	}

	public void removeApplicationListenerBean(String str) {
	}
}
