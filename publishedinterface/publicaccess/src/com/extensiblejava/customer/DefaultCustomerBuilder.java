package com.extensiblejava.customer;

import com.extensiblejava.order.*;

public class DefaultCustomerBuilder implements CustomerBuilder {
	public Customer build() {
		return new Customer("John Doe", new DefaultOrderBuilder());
	}
}