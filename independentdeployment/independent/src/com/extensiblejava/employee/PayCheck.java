package com.extensiblejava.employee;

import java.math.BigDecimal;

public class PayCheck {
	private BigDecimal pay;
	public PayCheck(BigDecimal pay) {
		this.pay = pay;
	}

	public BigDecimal getPay() { return this.pay; }
}