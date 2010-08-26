package org.springframework.osgi.iandt.simpleservice2.impl;

import org.springframework.osgi.iandt.simpleservice2.MyService2;

/**
 * @author Hal Hildebrand
 *         Date: Dec 1, 2006
 *         Time: 3:06:01 PM
 */
public class MyService2Impl implements MyService2 { 
    public String stringValue() {
        return "Finklestein.  Bartholomew Finklestein";
    }

    public void voidMethod() {
        System.out.println("void method called"); 
    }

}
