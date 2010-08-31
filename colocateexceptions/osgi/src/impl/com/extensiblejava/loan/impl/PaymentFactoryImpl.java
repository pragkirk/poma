package com.extensiblejava.loan.impl;
import java.math.*;
import com.extensiblejava.loan.*;

public class PaymentFactoryImpl implements PaymentFactory {
	public Loan createLoan(PaymentSchedule paymentSchedule, BigDecimal cumulativeInterest, BigDecimal cumulativePrincipal) { 
		return new LoanImpl(paymentSchedule, cumulativeInterest, cumulativePrincipal); 
	}
	public PaymentSchedule createPaymentSchedule() { return new PaymentScheduleImpl(); }
	public Payment createPayment(BigDecimal principal, BigDecimal interest) { return new PaymentImpl(principal, interest); }
}
