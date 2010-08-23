package com.extensiblejava.main.test;

import junit.framework.*;
import com.extensiblejava.main.*;
import org.springframework.context.*;
import org.springframework.context.support.*;

public class TestAppConfiguration extends TestCase {
	private ApplicationContext ctx;

	public void setUp() {

		//for internal configuration.
		this.ctx = new ClassPathXmlApplicationContext("classpath*:com/extensiblejava/main/AppContext.xml");

		//for external configuration.
		//this.ctx = new ClassPathXmlApplicationContext("AppContext.xml");
	}
	public void testInjection() throws Exception {
		FirstBean bean = (FirstBean) this.ctx.getBean("firstBean");
		int val = bean.perform();
		assertTrue( 1 == val);
	}

	public void testNoSpring() throws Exception {
		FirstBean bean = new FirstBean(new InjectedBean());
		int val = bean.perform();
		assertTrue(1 == val);
	}

}