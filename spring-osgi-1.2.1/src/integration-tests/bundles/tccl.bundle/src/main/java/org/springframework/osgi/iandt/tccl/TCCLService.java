package org.springframework.osgi.iandt.tccl;

/**
 * Simple service that loads classes and resources using the context
 * classloader.
 * 
 * @author Costin Leau
 */
public interface TCCLService {

	/**
	 * Return the TCCL class loader used during the service invocation.
	 * 
	 * @return TCCL Class Loader
	 */
	public ClassLoader getTCCL();

}
