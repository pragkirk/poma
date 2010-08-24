package com.extensiblejava.hello.service.impl;

import java.util.Properties;
import com.extensiblejava.hello.service.HelloService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;

public class HelloServiceImpl implements HelloService, BundleActivator {

	private ServiceRegistration registration;

	public void start(BundleContext context) {
		Properties props = new Properties();
		props.put("Language", "English");
		registration = context.registerService(HelloService.class.getName(), this, props);
    }

	public void stop(BundleContext context) {

    }

	public String sayHello() {
		return "Hello World!! ";
	}

	public String sayGoodbye() {
		return "Goodbye World!!";
	}
}
