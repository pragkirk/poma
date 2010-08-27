package com.extensiblejava.payroll;

import java.math.BigDecimal;

public class Payroll {

	private BigDecimal salary;
	public Payroll(BigDecimal salary) {
		this.salary = salary;
	}

	public BigDecimal run() {
		//calculate the pay...
		return new BigDecimal("20000.00");
	}
}