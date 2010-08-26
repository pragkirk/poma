package org.springframework.osgi.iandt.dependencies;

import org.springframework.osgi.iandt.simpleservice2.MyService2;
import org.springframework.osgi.iandt.simpleservice.MyService;

/**
 * @author Hal Hildebrand
 *         Date: Dec 1, 2006
 *         Time: 3:39:40 PM
 */
public class DependentImpl implements Dependent {
    private MyService service1;
    private MyService2 service2;
    private MyService2 service3;


    public void setService1(MyService service1) {
        this.service1 = service1;
    }

    public void setService2(MyService2 service2) {
        this.service2 = service2;
    }


    public void setService3(MyService2 service3) {
        this.service3 = service3;
    }


    public boolean isResolved() {
        return service2 != null && service3 != null && service1 != null;
    }
}
