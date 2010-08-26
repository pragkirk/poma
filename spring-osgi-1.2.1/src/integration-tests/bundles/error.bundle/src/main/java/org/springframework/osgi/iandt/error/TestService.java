package org.springframework.osgi.iandt.error;

import org.springframework.osgi.iandt.simpleservice.MyService;

public class TestService implements MyService {
    public String stringValue() {
        return "Bond.  James Bond.";
    }

}
