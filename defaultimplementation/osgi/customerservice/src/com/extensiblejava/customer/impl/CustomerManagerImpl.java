package com.extensiblejava.customer.impl;

import com.extensiblejava.customer.*;
import com.extensiblejava.calculator.*;

public class CustomerManagerImpl implements CustomerManager {
	private CustomerBuilder customerBuilder;	
	
	public CustomerManagerImpl(CustomerBuilder customerBuilder) {
		this.customerBuilder = customerBuilder;
	}

	public Customer getCustomer() {
		return customerBuilder.build();		
	}
	
	public void updateDiscounter(DiscountCalculator discountCalculator) {
		this.customerBuilder.updateDiscounter(discountCalculator);
	}
}