package com.extensiblejava.loan;

import java.math.*;

public interface LoanCalculator {
	public PaymentSchedule calculatePaymentSchedule(BigDecimal presentValue, BigDecimal rate, int term) throws CalculationException;
	public BigDecimal getCumulativeInterest();
	public BigDecimal getCumulativePrincipal();

}