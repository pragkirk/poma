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

package org.springframework.osgi.web.deployer.tomcat;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.catalina.Container;
import org.apache.catalina.Loader;

/**
 * OSGi specific Catalina Loader implementation. Just a simple, mainly no-op
 * class as the default one is too specific to Catalina and does too much
 * inside.
 * 
 * @author Costin Leau
 * 
 */
class OsgiCatalinaLoader implements Loader, PropertyChangeListener {

	private Container container;

	private ClassLoader classLoader;


	public void addPropertyChangeListener(PropertyChangeListener listener) {
		throw new UnsupportedOperationException("addPropertyChangeListener");
	}

	public void addRepository(String repository) {
		throw new UnsupportedOperationException("");
	}

	public void backgroundProcess() {
		// do nothing
	}

	public String[] findRepositories() {
		throw new UnsupportedOperationException("findRepositories");
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public Container getContainer() {
		return container;
	}

	public boolean getDelegate() {
		return false;
	}

	public String getInfo() {
		return toString();
	}

	public boolean getReloadable() {
		return false;
	}

	public boolean modified() {
		return false;
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		throw new UnsupportedOperationException("removePropertyChangeListener");
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void setDelegate(boolean delegate) {
		if (delegate)
			throw new UnsupportedOperationException("delegation unsupported");
	}

	public void setReloadable(boolean reloadable) {
		if (reloadable)
			throw new UnsupportedOperationException("reloading unsupported");
	}

	public void propertyChange(PropertyChangeEvent evt) {
		// this is a static class, don't do anything
	}
}
