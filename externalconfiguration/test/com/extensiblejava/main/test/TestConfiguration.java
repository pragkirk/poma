package com.extensiblejava.main.test;

import junit.framework.*;
import com.extensiblejava.main.*;
import org.springframework.context.*;
import org.springframework.context.support.*;

public class TestConfiguration extends TestCase {
	private ApplicationContext ctx;

	public void setUp() {
		this.ctx = new ClassPathXmlApplicationContext("classpath*:com/extensiblejava/main/test/TestContext.xml");
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