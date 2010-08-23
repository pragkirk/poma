package com.extensiblejava.main;

import java.util.*;

public class FirstBean {
	private InjectedInterface injectedBean;

	public FirstBean(InjectedInterface injectedBean) {
		//ResourceBundle bundle = ResourceBundle.getBundle("com.extensiblejava.main.Resource");
		//String uid = bundle.getString("app.uid");
		//String pwd = bundle.getString("app.pwd");
		//injectedBean.setUid(uid);
		//injectedBean.setPwd(pwd);
		this.injectedBean = injectedBean;
	}

	public int perform() {
		this.injectedBean.injection();
		return 1;
	}
}