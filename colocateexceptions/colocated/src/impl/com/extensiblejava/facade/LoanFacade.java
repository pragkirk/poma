package com.extensiblejava.facade;

import java.math.*;
import com.extensiblejava.loan.*;
import com.extensiblejava.calculator.*;
import org.springframework.context.*;
import org.springframework.context.support.*;

public class LoanFacade {
	
	public PaymentSchedule calculatePaymentSchedule(BigDecimal presentValue, BigDecimal rate, int term) {
		ApplicationContext appContext = new FileSystemXmlApplicationContext("com/extensiblejava/facade/AppContext.xml");
		Loan loan = (Loan) appContext.getBean("loan");
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule(presentValue, rate, term);
		return paymentSchedule;
	}
	
	public BigDecimal getMonthlyPayment(BigDecimal presentValue, BigDecimal rate, int term) {
		ApplicationContext appContext = new FileSystemXmlApplicationContext("com/extensiblejava/facade/AppContext.xml");
		Loan loan = (Loan) appContext.getBean("loan");
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule(presentValue, rate, term);
		BigDecimal monthlyPayment = loan.getMonthlyPayment();
		return monthlyPayment;
	}
}