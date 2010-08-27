package com.extensiblejava.customer.impl;

import com.extensiblejava.customer.*;

public class CustomerManagerImpl implements CustomerManager {
	private CustomerBuilder customerBuilder;	
	
	public CustomerManagerImpl(CustomerBuilder customerBuilder) {
		this.customerBuilder = customerBuilder;
	}

	public Customer getCustomer() {
		return customerBuilder.build();		
	}
}