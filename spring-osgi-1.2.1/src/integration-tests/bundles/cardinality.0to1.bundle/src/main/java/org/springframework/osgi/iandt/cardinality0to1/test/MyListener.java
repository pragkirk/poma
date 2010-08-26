package org.springframework.osgi.iandt.cardinality0to1.test;

import org.springframework.osgi.iandt.simpleservice2.MyService2;
import java.util.Dictionary;

/**
 * @author Hal Hildebrand
 *         Date: Dec 6, 2006
 *         Time: 6:17:21 PM
 */
public class MyListener {
    public static int BOUND_COUNT = 0;
    public static int UNBOUND_COUNT = 0;


    public void serviceAvailable(MyService2 simpleService, Dictionary properties) {
        BOUND_COUNT++;
    }


    public void serviceUnavailable(MyService2 simpleService, Dictionary properties) {
        UNBOUND_COUNT++;
    }
}

