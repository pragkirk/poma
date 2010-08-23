package com.extensiblejava.loan;

import java.math.*;

public interface LoanCalculator {
	public PaymentSchedule calculatePaymentSchedule();
	public BigDecimal getCumulativeInterest();
	public BigDecimal getCumulativePrincipal();

}