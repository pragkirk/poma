package com.extensiblejava.loan;
import java.math.*;
public interface PaymentFactory {
	public PaymentSchedule createPaymentSchedule();
	public Payment createPayment(BigDecimal principal, BigDecimal interest);
}
