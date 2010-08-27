package com.extensiblejava.employee;

import java.math.*;

public class Employee {
	private Name name;
	private BigDecimal salary;
	public Employee(Name name, BigDecimal salary) {
		this.name = name;
		this.salary = salary;
	}

	public PayCheck pay(PayrollRunner runner) {
		return new PayCheck(runner.runPayroll(this.salary));
	}
}