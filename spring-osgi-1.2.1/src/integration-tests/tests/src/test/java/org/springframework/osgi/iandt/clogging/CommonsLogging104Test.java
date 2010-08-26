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

package org.springframework.osgi.iandt.clogging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.iandt.BaseIntegrationTest;
import org.springframework.util.CollectionUtils;

/**
 * Integration test for commons logging 1.0.4 and its broken logging discovery.
 * 
 * @author Costin Leau
 * 
 */
public abstract class CommonsLogging104Test extends BaseIntegrationTest {

	/** logger */
	private static final Log log = LogFactory.getLog(CommonsLogging104Test.class);


	protected String[] getTestFrameworkBundlesNames() {
		String[] bundles = super.getTestFrameworkBundlesNames();

		// remove slf4j
		Collection bnds = new ArrayList(bundles.length);
		CollectionUtils.mergeArrayIntoCollection(bundles, bnds);

		for (Iterator iterator = bnds.iterator(); iterator.hasNext();) {
			String object = (String) iterator.next();
			// remove slf4j
			if (object.startsWith("org.slf4j"))
				iterator.remove();
		}
		// add commons logging
		bnds.add("org.eclipse.bundles,commons-logging,20070611");

		return (String[]) bnds.toArray(new String[bnds.size()]);
	}

	public void testSimpleLoggingStatement() throws Exception {
		log.info("logging statement");
	}

	protected void preProcessBundleContext(BundleContext platformBundleContext) throws Exception {

		// all below fail
		LogFactory.releaseAll();
		//System.setProperty("org.apache.commons.logging.LogFactory", "org.apache.commons.logging.impl.NoOpLog");
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		//		System.out.println("TCCL is " + cl);
		Thread.currentThread().setContextClassLoader(null);
		super.preProcessBundleContext(platformBundleContext);
	}

}
