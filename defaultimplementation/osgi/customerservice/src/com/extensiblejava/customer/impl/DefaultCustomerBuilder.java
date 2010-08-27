package com.extensiblejava.customer.impl;

import com.extensiblejava.customer.*;
import com.extensiblejava.order.*;
import com.extensiblejava.calculator.*;

public class DefaultCustomerBuilder implements CustomerBuilder {
	private OrderBuilder orderBuilder;
	private DiscountCalculator discountCalculator;
	
	public DefaultCustomerBuilder(OrderBuilder orderBuilder, DiscountCalculator discountCalculator) {
		this.orderBuilder = orderBuilder;
		this.discountCalculator = discountCalculator;
	}
	
	public Customer build() {
		return new DefaultCustomer("John Doe", orderBuilder, discountCalculator);
	}
	
	public void updateDiscounter(DiscountCalculator discountCalculator) {
		this.discountCalculator = discountCalculator;
	}
}