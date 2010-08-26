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

package org.springframework.osgi.util.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

/**
 * Utility class for commons actions used within PrivilegedBlocks.
 * 
 * @author Costin Leau
 * 
 */
public abstract class PrivilegedUtils {

	private static class GetTCCLAction implements PrivilegedAction {

		public Object run() {
			return Thread.currentThread().getContextClassLoader();
		}

		public ClassLoader getTCCL() {
			return (ClassLoader) AccessController.doPrivileged(this);
		}
	}

	public interface UnprivilegedThrowableExecution {

		public Object run() throws Throwable;
	}

	public interface UnprivilegedExecution {

		public Object run();
	}


	private static final GetTCCLAction getTCCLAction = new GetTCCLAction();


	public static ClassLoader getTCCL() {
		return getTCCLAction.getTCCL();
	}

	/**
	 * Temporarily changes the TCCL to the given one for the duration of the
	 * given execution. All actions except the execution are executed with
	 * privileged access.
	 * 
	 * Consider checking if there is a security manager in place before calling
	 * this method.
	 * 
	 * @param customClassLoader
	 * @param execution
	 * @return
	 */
	public static Object executeWithCustomTCCL(final ClassLoader customClassLoader,
			final UnprivilegedExecution execution) {
		final Thread currentThread = Thread.currentThread();
		final ClassLoader oldTCCL = getTCCLAction.getTCCL();

		try {
			AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					currentThread.setContextClassLoader(customClassLoader);
					return null;
				}
			});
			return execution.run();
		}
		finally {
			AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					currentThread.setContextClassLoader(oldTCCL);
					return null;
				}
			});
		}
	}

	/**
	 * Temporarily changes the TCCL to the given one for the duration of the
	 * given execution. All actions except the execution are executed with
	 * privileged access.
	 * 
	 * Consider checking if there is a security manager in place before calling
	 * this method.
	 * 
	 * @param customClassLoader
	 * @param execution
	 * @return
	 * @throws Throwable
	 */
	public static Object executeWithCustomTCCL(final ClassLoader customClassLoader,
			final UnprivilegedThrowableExecution execution) throws Throwable {
		final Thread currentThread = Thread.currentThread();
		final ClassLoader oldTCCL = getTCCLAction.getTCCL();

		try {
			AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					currentThread.setContextClassLoader(customClassLoader);
					return null;
				}
			});
			return execution.run();
		}
		catch (PrivilegedActionException pae) {
			throw pae.getCause();
		}
		finally {
			AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					currentThread.setContextClassLoader(oldTCCL);
					return null;
				}
			});
		}
	}
}
