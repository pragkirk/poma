package com.extensiblejava.loan;
import java.math.*;
public interface PaymentFactory {
	public Loan createLoan(PaymentSchedule paymentSchedule, BigDecimal cumulativeInterest, BigDecimal cumulativePrincipal);
	public PaymentSchedule createPaymentSchedule();
	public Payment createPayment(BigDecimal principal, BigDecimal interest);
}
