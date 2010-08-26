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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection related utility class. Similar to that found inside main Spring
 * distribution but with customizations particular to this framework.
 * 
 * @see org.springframework.util.ReflectionUtils
 * 
 * @author Costin Leau
 * 
 */
public abstract class ReflectionUtils {

	/**
	 * Runtime exception wrapper for checked exceptions thrown by invoked
	 * methods.
	 * 
	 * Provides a cast free method to get the actual exception.
	 * 
	 * @author Costin Leau
	 * 
	 */
	public static class InvocationCheckedExceptionWrapper extends RuntimeException {

		private static final long serialVersionUID = 5496580030934775697L;


		public InvocationCheckedExceptionWrapper(Exception cause) {
			super(cause);
		}

		/**
		 * Return the target exception. It will return a (checked) exception
		 * rather then a {@link Throwable}.
		 * 
		 * @return target exception
		 */
		public Exception getTargetException() {
			return (Exception) getCause();
		}
	}


	/**
	 * Invoke the specified {@link Method} against the supplied target object
	 * with no arguments. The target object can be <code>null</code> when
	 * invoking a static {@link Method}.
	 * 
	 * <p/> This method is identical to
	 * {@link org.springframework.util.ReflectionUtils#invokeMethod(Method, Object)}
	 * except that if the target method throws a checked exception, the method
	 * will throw a InvocationCheckedException.
	 * 
	 * <p>
	 * Thrown exceptions are handled via a call to
	 * {@link #handleReflectionException}.
	 * 
	 * @param method the method to invoke
	 * @param target the target object to invoke the method on
	 * @return the invocation result, if any
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
	public static Object invokeMethod(Method method, Object target) {
		return invokeMethod(method, target, null);
	}

	/**
	 * Invoke the specified {@link Method} against the supplied target object
	 * with the supplied arguments. The target object can be <code>null</code>
	 * when invoking a static {@link Method}.
	 * 
	 * <p/> This method is identical to
	 * {@link org.springframework.util.ReflectionUtils#invokeMethod(Method, Object)}
	 * except that if the target method throws a checked exception, the method
	 * will throw a InvocationCheckedException.
	 * 
	 * <p>
	 * Thrown exceptions are handled via a call to
	 * {@link #handleReflectionException}.
	 * 
	 * @param method the method to invoke
	 * @param target the target object to invoke the method on
	 * @param args the invocation arguments (may be <code>null</code>)
	 * @return the invocation result, if any
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
	public static Object invokeMethod(Method method, Object target, Object[] args) {
		try {
			return method.invoke(target, args);
		}
		catch (IllegalAccessException ex) {
			org.springframework.util.ReflectionUtils.handleReflectionException(ex);
			throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": "
					+ ex.getMessage());
		}
		catch (InvocationTargetException ex) {
			handleInvocationTargetException(ex);
			// the line above will not execute as the method above always throws
			// an exception
			return null;
		}
	}

	/**
	 * Handle the given invocation target exception. Should only be called if no
	 * checked exception is expected to be thrown by the target method.
	 * <p>
	 * Throws the underlying RuntimeException or Error in case of such a root
	 * cause. Throws an InvocationCheckedException else (the main difference
	 * from
	 * {@link org.springframework.util.ReflectionUtils#handleInvocationTargetException(InvocationTargetException)}.
	 * 
	 * @param ex the invocation target exception to handle
	 */
	public static void handleInvocationTargetException(InvocationTargetException ex) {
		Throwable cause = ex.getTargetException();
		if (cause instanceof RuntimeException) {
			throw (RuntimeException) cause;
		}
		if (cause instanceof Error) {
			throw (Error) cause;
		}

		throw new InvocationCheckedExceptionWrapper((Exception) cause);
	}

	/**
	 * Analyze the given exception and, if it's of type
	 * {@link InvocationCheckedExceptionWrapper} then will return the actual
	 * cause, otherwise return the original exception given.
	 * 
	 * @param exception invocation exception
	 * @return actual target exception, in case any wrapping took place
	 */
	public static Exception getInvocationException(Exception exception) {
		return (exception instanceof InvocationCheckedExceptionWrapper ? ((InvocationCheckedExceptionWrapper) exception).getTargetException()
				: exception);
	}
}
