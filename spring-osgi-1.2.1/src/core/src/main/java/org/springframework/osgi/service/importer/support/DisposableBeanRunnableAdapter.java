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

package org.springframework.osgi.service.importer.support;

import org.springframework.beans.factory.DisposableBean;

/**
 * Simple adapter around a Spring disposable bean.
 * 
 * @author Costin Leau
 * 
 */
class DisposableBeanRunnableAdapter implements Runnable {

	private final DisposableBean bean;


	/**
	 * Constructs a new <code>DisposableBeanRunnableAdapter</code> instance.
	 * 
	 * @param bean
	 */
	public DisposableBeanRunnableAdapter(DisposableBean bean) {
		this.bean = bean;
	}

	public void run() {
		try {
			bean.destroy();
		}
		catch (Exception ex) {
			if (ex instanceof RuntimeException)
				throw (RuntimeException) ex;
			else
				throw new RuntimeException(ex);
		}
	}
}
