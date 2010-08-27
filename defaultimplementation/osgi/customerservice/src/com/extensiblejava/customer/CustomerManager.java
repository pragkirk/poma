package com.extensiblejava.customer;

import com.extensiblejava.calculator.*;

public interface CustomerManager {

	public Customer getCustomer();
	public void updateDiscounter(DiscountCalculator discountCalculator);
}