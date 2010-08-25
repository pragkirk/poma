package com.extensiblejava.customer.impl;

import com.extensiblejava.customer.*;
import com.extensiblejava.order.*;
import com.extensiblejava.order.impl.*;

public class DefaultCustomerBuilder implements CustomerBuilder {
	public Customer build() {
		return new DefaultCustomer("John Doe", new DefaultOrderBuilder());
	}
}