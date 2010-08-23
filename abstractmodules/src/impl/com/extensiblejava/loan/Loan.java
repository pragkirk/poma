package com.extensiblejava.loan;

import java.math.*;

public interface Loan {
	public PaymentSchedule calculatePaymentSchedule();
	public BigDecimal getMonthlyPayment();
	public BigDecimal getFinalPayment();
	public BigDecimal getCumulativeInterest();
	public BigDecimal getCumulativePrincipal();
	public BigDecimal getTotalPayments();
}