package org.springframework.osgi.iandt.simpleservice2.impl;

import org.springframework.osgi.iandt.simpleservice2.MyService2;

/**
 * @author Hal Hildebrand
 *         Date: Aug 27, 2007
 *         Time: 9:48:04 AM
 */
public class MyService2Impl implements MyService2 {
    public String stringValue() {
        return "Finklestein.  Bartholomew Finklestein";
    }

    public void voidMethod() {
        System.out.println("void method called");
    }

}
