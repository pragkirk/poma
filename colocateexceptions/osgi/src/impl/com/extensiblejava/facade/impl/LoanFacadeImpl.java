package com.extensiblejava.facade.impl;

import java.math.*;
import com.extensiblejava.loan.*;
import com.extensiblejava.facade.*;

public class LoanFacadeImpl implements LoanFacade {
	private LoanCalculator loanCalculator;
	
	public LoanFacadeImpl(LoanCalculator loanCalculator) {
		this.loanCalculator = loanCalculator;
	}
	
	public PaymentSchedule calculatePaymentSchedule(BigDecimal presentValue, BigDecimal rate, int term) {
		//ApplicationContext appContext = new FileSystemXmlApplicationContext("com/extensiblejava/facade/AppContext.xml");
		//Loan loan = (Loan) appContext.getBean("loan");
		Loan loan = this.loanCalculator.calculateLoan(presentValue, rate, term);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		return paymentSchedule;
	}
	
	public BigDecimal getMonthlyPayment(BigDecimal presentValue, BigDecimal rate, int term) {
		//ApplicationContext appContext = new FileSystemXmlApplicationContext("com/extensiblejava/facade/AppContext.xml");
		//Loan loan = (Loan) appContext.getBean("loan");
		Loan loan = this.loanCalculator.calculateLoan(presentValue, rate, term);
		BigDecimal monthlyPayment = loan.getMonthlyPayment();
		return monthlyPayment;
	}
}