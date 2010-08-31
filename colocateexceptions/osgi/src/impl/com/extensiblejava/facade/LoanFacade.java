package com.extensiblejava.facade;

import java.math.*;
import com.extensiblejava.loan.*;

public interface LoanFacade {
	
	public PaymentSchedule calculatePaymentSchedule(BigDecimal presentValue, BigDecimal rate, int term);
	public BigDecimal getMonthlyPayment(BigDecimal presentValue, BigDecimal rate, int term);
}