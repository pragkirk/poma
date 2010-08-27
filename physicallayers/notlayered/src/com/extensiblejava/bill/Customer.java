package com.extensiblejava.bill;

import java.util.*;

public class Customer {
	private CustomerEntityLoader loader;
	private Integer custId;
	private Name name;
	private List bills;

	public static Customer loadCustomer(CustomerEntityLoader loader) {
		return loader.loadCustomer();
	}

	public Customer(Integer custId, Name name, CustomerEntityLoader loader) {
		this.custId = custId;
		this.name = name;
		this.loader = loader;
	}

	public List getBills() {
		if (this.bills == null) {
			this.bills = loader.loadBills();
		}
		return this.bills;
	}

	public Name getName() { return this.name; }
}