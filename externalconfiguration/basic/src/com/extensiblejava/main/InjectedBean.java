package com.extensiblejava.main;

public class InjectedBean implements InjectedInterface {
	private String uid;
	private String pwd;

	public void setUid(String uid) { this.uid = uid; }
	public void setPwd(String pwd) { this.pwd = pwd; }
	public void injection() { System.out.println("uid: "  + uid + " pwd: " + pwd); }
}