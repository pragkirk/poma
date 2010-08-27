package com.extensiblejava.ui;

import java.util.*;
import com.extensiblejava.bill.*;

public class CustomerSearchResultsBean {
	private String name;
	private List bills;
	public CustomerSearchResultsBean(Customer customer) {
		this.name = customer.getName().getFullName();
		this.bills = customer.getBills();
	}

	public String getName() { return this.name; }
	public List getBills() { return this.bills; }
}