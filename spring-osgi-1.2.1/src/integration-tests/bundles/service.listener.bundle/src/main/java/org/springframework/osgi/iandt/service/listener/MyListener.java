package org.springframework.osgi.iandt.service.listener;

import org.springframework.osgi.iandt.simpleservice.MyService;
import java.util.Dictionary;


/**
 * @author Hal Hildebrand
 *         Date: Nov 14, 2006
 *         Time: 8:07:27 AM
 */
public class MyListener {
    public static int BOUND_COUNT = 0;
    public static int UNBOUND_COUNT = 0;


    public void serviceAvailable(MyService simpleService, Dictionary props) {
        BOUND_COUNT++;
    }


    public void serviceUnavailable(MyService simpleService, Dictionary props) {
        UNBOUND_COUNT++;
    }


    public void setSimpleService(MyService simpleService) {
        // Dummy used to force initialization of osgi service reference to simple service
    }
}
