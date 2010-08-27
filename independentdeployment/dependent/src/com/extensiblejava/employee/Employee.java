package com.extensiblejava.employee;

import com.extensiblejava.payroll.Payroll;
import java.math.*;

public class Employee {
	private Name name;
	private BigDecimal salary;
	public Employee(Name name, BigDecimal salary) {
		this.name = name;
		this.salary = salary;
	}

	public PayCheck pay() {
		Payroll payroll = new Payroll(salary);
		return new PayCheck(payroll.run());
	}
}