package org.springframework.osgi.config;

import java.util.Dictionary;

/**
 * @author Hal Hildebrand
 *         Date: Nov 13, 2006
 *         Time: 12:35:01 PM
 */
public class DummyListenerServiceSignature {
    static int BIND_CALLS = 0;
    static int UNBIND_CALLS = 0;


    public void register(Cloneable service, Dictionary props) {
        BIND_CALLS++;
    }


    public void deregister(Cloneable service, Dictionary props) {
        UNBIND_CALLS++;
    }
}
