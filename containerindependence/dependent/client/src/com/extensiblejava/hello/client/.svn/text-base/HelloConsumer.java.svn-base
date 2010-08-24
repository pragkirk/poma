package com.extensiblejava.hello.client;

import com.extensiblejava.hello.service.HelloService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class HelloConsumer implements BundleActivator {

	private ServiceTracker helloWorldTracker;
	private HelloService helloService;

	public void setService(HelloService helloService) {
		this.helloService = helloService;
	}

	public void removeService() {
		this.helloService = null;
	}

	public void start(BundleContext context) throws Exception {
		helloWorldTracker = new ServiceTracker(context, HelloService.class.getName(), null);
		helloWorldTracker.open();
		HelloService hello = (HelloService) helloWorldTracker.getService();

		if (hello == null) {
			System.out.println("Hello service unavailable on HelloConsumer start");
		} else {
			System.out.println(hello.sayHello());
		}
	}

	public void stop(BundleContext context) {
		HelloService hello = (HelloService) helloWorldTracker.getService();
		if (hello == null) {
			System.out.println("Hello service unavailable on HelloConsumer stop");
		} else {
			System.out.println(hello.sayGoodbye());
		}

		helloWorldTracker.close();
    }

}
