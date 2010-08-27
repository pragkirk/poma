package com.extensiblejava.customer;

import com.extensiblejava.calculator.*;

public interface CustomerBuilder {
	public Customer build();
	public void updateDiscounter(DiscountCalculator discountCalculator);
}