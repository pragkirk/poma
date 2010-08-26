
package org.springframework.osgi.service.util.internal.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.osgi.util.internal.PrivilegedUtils;
import org.springframework.util.ObjectUtils;

/**
 * Simple interceptor for dealing with ThreadContextClassLoader(TCCL)
 * management.
 * 
 * @author Hal Hildebrand
 * @author Costin Leau
 */
public class ServiceTCCLInterceptor implements MethodInterceptor {

	private static final int hashCode = ServiceTCCLInterceptor.class.hashCode() * 13;

	/** classloader to set the TCCL during invocation */
	private final ClassLoader loader;


	/**
	 * Constructs a new <code>OsgiServiceTCCLInterceptor</code> instance.
	 * 
	 * @param loader classloader to use for TCCL during invocation. Can be null.
	 */
	public ServiceTCCLInterceptor(ClassLoader loader) {
		this.loader = loader;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {

		if (System.getSecurityManager() != null) {
			return invokePrivileged(invocation);
		}
		else {
			return invokeUnprivileged(invocation);
		}
	}

	private Object invokePrivileged(final MethodInvocation invocation) throws Throwable {
		return PrivilegedUtils.executeWithCustomTCCL(loader, new PrivilegedUtils.UnprivilegedThrowableExecution() {

			public Object run() throws Throwable {
				return invocation.proceed();
			}
		});
	}

	private Object invokeUnprivileged(MethodInvocation invocation) throws Throwable {
		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(loader);
			return invocation.proceed();
		}
		finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof ServiceTCCLInterceptor) {
			ServiceTCCLInterceptor oth = (ServiceTCCLInterceptor) other;
			return (ObjectUtils.nullSafeEquals(loader, oth.loader));
		}
		return false;
	}

	public int hashCode() {
		return hashCode;
	}
}