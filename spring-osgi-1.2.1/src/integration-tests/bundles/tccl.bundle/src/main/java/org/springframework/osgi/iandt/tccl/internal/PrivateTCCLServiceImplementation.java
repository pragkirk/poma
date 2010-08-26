package org.springframework.osgi.iandt.tccl.internal;

import org.springframework.osgi.iandt.tccl.TCCLService;

/**
 * Private class that should not be exposed.
 * 
 * @author Costin Leau
 */
public class PrivateTCCLServiceImplementation implements TCCLService {

	public ClassLoader getTCCL() {
		return Thread.currentThread().getContextClassLoader();
	}

	public String toString() {
		return PrivateTCCLServiceImplementation.class.getName() + "#" + System.identityHashCode(this);
	}
}
