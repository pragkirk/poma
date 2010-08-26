package org.springframework.osgi.iandt.configuration;

import java.util.*;

/**
 * @author Hal Hildebrand
 *         Date: Jun 14, 2007
 *         Time: 5:30:05 PM
 */
public class ManagedServiceListener {
    public final static List updates = new ArrayList();

    public static final String SERVICE_FACTORY_PID = "test.service.pid";


    public void updateService(Dictionary properties) {
        if (properties.isEmpty()) {
            return;
        }
        Dictionary copy = new Hashtable();
        for (Enumeration keys = properties.keys(); keys.hasMoreElements();) {
            Object key = keys.nextElement();
            copy.put(key, properties.get(key));
        }
        updates.add(copy);
    }


    public void updateServiceMap(Map properties) {
        if (properties.isEmpty()) {
            return;
        }
        Dictionary copy = new Hashtable();
        for (Iterator keys = properties.keySet().iterator(); keys.hasNext();) {
            Object key = keys.next();
            copy.put(key, properties.get(key));
        }
        updates.add(copy);
    }
}
