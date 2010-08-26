package org.springframework.osgi.context.event;

import org.springframework.context.ApplicationContext;
import org.osgi.framework.Bundle;

/**
 * Event raised when an <tt>ApplicationContext#close()</tt> method executes
 * inside an OSGi bundle.
 *
 * @author Andy Piper
 */
public class OsgiBundleContextClosedEvent extends OsgiBundleApplicationContextEvent {

	private Throwable cause;

	/**
	 * Constructs a new <code>OsgiBundleContextClosedEvent</code> instance.
	 *
	 * @param source the <code>ApplicationContext</code> that has been closed (must
	 * not be <code>null</code>)
	 * @param bundle the OSGi bundle associated with the source application
	 * context
	 * @param cause optional <code>Throwable</code> indicating the cause of
	 * the failure
	 */
	public OsgiBundleContextClosedEvent(ApplicationContext source, Bundle bundle, Throwable cause) {
		super(source, bundle);
		this.cause = cause;
	}

    /**
     * Constructs a new <code>OsgiBundleContextClosedEvent</code> instance.
     *
     * @param source event source
     * @param bundle associated OSGi bundle
     */
    public OsgiBundleContextClosedEvent(ApplicationContext source, Bundle bundle) {
        super(source, bundle);
    }

	/**
	 * Returns the <code>Throwable</code> that caused the application context
	 * closure to fail.
	 *
	 * @return the cause of the failure.
	 */
	public final Throwable getFailureCause() {
		return cause;
	}	
}
