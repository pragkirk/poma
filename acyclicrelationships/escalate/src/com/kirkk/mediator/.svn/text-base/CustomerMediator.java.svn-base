package com.kirkk.mediator;

import java.math.*;
import java.util.*;
import com.kirkk.cust.*;

public class CustomerMediator {
	private Customer customer;

	public CustomerMediator(Customer customer) {
		this.customer = customer;
	}

	public BigDecimal getDiscountAmount() {
		if (this.customer.getBills().size() > 5) {
			return new BigDecimal(0.1);
		} else {
			return new BigDecimal(0.03);
		}
	}
}