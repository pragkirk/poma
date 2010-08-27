package com.extensiblejava.test;

import junit.framework.TestCase;
import com.extensiblejava.employee.*;
import java.math.BigDecimal;

public class EmployeeTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(EmployeeTest.class);
	}

	public void testEmployeePay() {

		Employee employee = new Employee(new Name(), new BigDecimal("20000.00"));
		PayCheck payCheck = employee.pay();

		assertNotNull(payCheck);

	}
}
