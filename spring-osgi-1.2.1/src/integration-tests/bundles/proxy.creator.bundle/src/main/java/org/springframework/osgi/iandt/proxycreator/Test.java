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

package org.springframework.osgi.iandt.proxycreator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple test that touches proxies created through spring. While this test
 * doesn't do much by itself, it should always work even when the bundle it runs
 * in, is updated. Failure to do so implies incorrect usage of the appropriate
 * class loader.
 * 
 * Thanks to Alexei Akimov for the proof of concept.
 * 
 * @author Costin Leau
 * 
 */
public class Test {

	/** logger */
	private static final Log log = LogFactory.getLog(Test.class);

	private SomeInterface jdkProxy;
	private SomeInterface cglibProxy;


	/**
	 * Sets echo JDK proxy
	 * 
	 * @param echoJdkProxy
	 */
	public void setJdkProxy(SomeInterface echoJdkProxy) {
		this.jdkProxy = echoJdkProxy;
	}

	/**
	 * Sets echo CGLIB proxy
	 * 
	 * @param echoCglibProxy
	 */
	public void setCglibProxy(SomeInterface echoCglibProxy) {
		this.cglibProxy = echoCglibProxy;
	}

	public void test() {
		testInterface(jdkProxy);
		testInterface(cglibProxy);
	}

	// interact with the proxy to make sure the weaving process is successful
	private void testInterface(SomeInterface intfs) {
		try {
			log.info("testing proxy interception...");
			intfs.doSmth("hangar 18");
		}
		catch (Throwable th) {
			log.error("caught exception", th);
		}
	}
}
