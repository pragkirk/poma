package com.extensiblejava.main.test;

import com.extensiblejava.main.*;

public class MockInjection implements InjectedInterface {
	private String uid;
	private String pwd;

	public void injection() { System.out.println("MockInjection --> uid: "  + uid + " pwd: " + pwd); }
	public void setUid(String uid) { this.uid = uid; }
	public void setPwd(String pwd) { this.pwd = pwd; }
}