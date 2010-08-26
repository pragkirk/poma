package org.springframework.osgi.iandt.simpleservice.impl;

import org.springframework.osgi.iandt.simpleservice.MyService;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Hal Hildebrand
 *         Date: Dec 1, 2006
 *         Time: 3:06:01 PM
 */
public class MyServiceImpl implements MyService, InitializingBean {
    public void afterPropertiesSet() throws Exception {
        Integer delay = Integer.getInteger("org.springframework.osgi.iandt.simpleservice.impl.delay", new Integer(0));
        System.getProperties().remove("org.springframework.osgi.iandt.simpleservice.impl.delay");
        System.out.println("Delaying for:" + delay);
        Thread.sleep(delay.intValue());
    }


    public String stringValue() {
		return "Bond.  James Bond.";
	}

}
