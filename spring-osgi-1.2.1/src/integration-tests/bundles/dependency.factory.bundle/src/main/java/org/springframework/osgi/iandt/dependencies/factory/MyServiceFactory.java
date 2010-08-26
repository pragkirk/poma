package org.springframework.osgi.iandt.dependencies.factory;

import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.osgi.iandt.simpleservice.MyService;

/**
 * @author Hal Hildebrand
 *         Date: Aug 27, 2007
 *         Time: 8:41:19 AM
 */
public class MyServiceFactory implements SmartFactoryBean {

    protected MyService service = new MyService() {
        public String stringValue() {
            return "Hello World";
        }
    };
    private static final String DELAY_PROP = "org.springframework.osgi.iandt.dependencies.factory.delay"; 

    public Object getObject() throws Exception {
        Integer delay = Integer.getInteger(DELAY_PROP, new Integer(0));
        System.getProperties().remove(DELAY_PROP);
        System.out.println("Delaying for:" + delay);
        Thread.sleep(delay.intValue());
        return service;
    }


    public Class getObjectType() {
        return MyService.class;
    }


    public boolean isSingleton() {
        return true;
    }


    public boolean isPrototype() {
        return false;
    }


    public boolean isEagerInit() {
        return true;
    }
}
