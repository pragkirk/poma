package com.extensiblejava.bill;

import java.util.List;

public interface EntityLoader {
	public Customer loadCustomer();
	public List loadBills();
}