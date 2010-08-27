package com.extensiblejava.bill;

public class Name {
	private String first;
	private String last;
	public Name(String first, String last) {
		this.first = first;
		this.last = last;
	}

	public String getFullName() { return this.first + " " + this.last; }
}