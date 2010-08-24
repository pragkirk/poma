package com.extensiblejava.hello.client;

import com.extensiblejava.hello.service.HelloService;

public class HelloConsumer {
	private HelloService hello;

	public void setHelloService(HelloService hello) {
		this.hello = hello;
	}

	public void start() throws Exception {
       System.out.println(hello.sayHello());
	}

	public void stop() throws Exception {
		System.out.println(hello.sayGoodbye());
		// NOTE: The service is automatically released.
    }

}

