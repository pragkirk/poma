package com.extensiblejava.loan.impl;
import java.math.*;
import com.extensiblejava.loan.*;

public class PaymentFactoryImpl implements PaymentFactory {
	public PaymentSchedule createPaymentSchedule() { return new PaymentScheduleImpl(); }
	public Payment createPayment(BigDecimal principal, BigDecimal interest) { return new PaymentImpl(principal, interest); }
}
