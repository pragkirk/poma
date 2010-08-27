package com.extensiblejava.facade;

import com.extensiblejava.employee.*;
import com.extensiblejava.payroll.*;
import java.math.BigDecimal;

public class PayFacade implements PayrollRunner {

	public BigDecimal runPayroll(BigDecimal salary) {
		Payroll payroll = new Payroll(salary);
		return payroll.run();
	}
}