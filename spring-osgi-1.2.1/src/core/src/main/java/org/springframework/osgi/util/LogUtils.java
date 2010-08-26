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

package org.springframework.osgi.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class used for creating 'degradable' loggers for critical parts of
 * the applications. In the future, this class might be used across the entire
 * product.
 * 
 * @author Costin Leau
 * 
 */
class LogUtils {

	/**
	 * Set the TCCL of the bundle before creating the logger. This helps if
	 * commons-logging is used since it looks at the existing TCCL before
	 * associating a LogFactory with it and since the TCCL can be the
	 * BundleDelegatingClassLoader, loading a LogFactory using the
	 * BundleDelegatingClassLoader will result in an infinite cycle or chained
	 * failures that would be swallowed.
	 * 
	 * <p/> Create the logger using LogFactory but use a simple implementation
	 * if something goes wrong.
	 * 
	 * @param logName log name
	 * @return logger implementation
	 */
	public static Log createLogger(Class logName) {
		Log logger;
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		// push the logger class classloader (useful when dealing with commons-logging 1.0.x
		Thread.currentThread().setContextClassLoader(logName.getClassLoader());
		try {
			logger = LogFactory.getLog(logName);
		}
		catch (Throwable th) {
			logger = new SimpleLogger();
			logger.fatal(
				"logger infrastructure not properly set up. If commons-logging jar is used try switching to slf4j (see the FAQ for more info).",
				th);
		}
		finally {
			Thread.currentThread().setContextClassLoader(ccl);
		}
		return logger;
	}
}
