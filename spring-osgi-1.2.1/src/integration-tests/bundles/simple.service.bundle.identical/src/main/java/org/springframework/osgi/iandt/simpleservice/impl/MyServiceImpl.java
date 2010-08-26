package org.springframework.osgi.iandt.simpleservice.impl;

import org.springframework.osgi.iandt.simpleservice.MyService;

/**
 * @author Andy Piper
 */
public class MyServiceImpl implements MyService {
    // The counter can be used to check that the class has been freshly loaded, rather than
    // reused by the system
    private static int counter = 1;
    public String stringValue() {
		return "Connery.  Sean Connery #" + counter++;
	}

}
