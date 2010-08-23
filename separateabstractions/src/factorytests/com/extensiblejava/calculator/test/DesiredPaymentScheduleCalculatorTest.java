package com.extensiblejava.calculator.test;

import junit.framework.*;
import junit.textui.*;
import java.math.*;
import com.extensiblejava.loan.impl.*;
import com.extensiblejava.loan.*;
import com.extensiblejava.calculator.*;

public class DesiredPaymentScheduleCalculatorTest extends TestCase
{

	private BigDecimal presentValue;
	private BigDecimal rate;
	private int term;
	private BigDecimal desiredPayment;

	public static void main(String[] args)
	{
		String[] testCaseName = { DesiredPaymentScheduleCalculatorTest.class.getName() };

		junit.textui.TestRunner.main(testCaseName);
	}

	protected void setUp() {
		this.presentValue = new BigDecimal("15000.00");
		this.rate = new BigDecimal("12.0");
		this.desiredPayment = new BigDecimal("500.00");
		this.term = 60;
	}


	public void testMonthlyPayment() {
		LoanCalculator loanCalculator = new DesiredPaymentScheduleCalculator(this.desiredPayment, this.presentValue, this.rate, this.term);
		Loan loan = new LoanImpl(loanCalculator);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		BigDecimal monthlyPayment = loan.getMonthlyPayment();
		assertTrue(monthlyPayment.equals(new BigDecimal("500.00")));
	}

	public void testFinalPayment() {
		LoanCalculator loanCalculator = new DesiredPaymentScheduleCalculator(this.desiredPayment, this.presentValue, this.rate, this.term);
		Loan loan = new LoanImpl(loanCalculator);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		BigDecimal finalPayment = loan.getFinalPayment();
		assertTrue(finalPayment.equals(new BigDecimal("423.09")));
	}
	public void testNumberOfPayments() {
		LoanCalculator loanCalculator = new DesiredPaymentScheduleCalculator(this.desiredPayment, this.presentValue, this.rate, this.term);
		Loan loan = new LoanImpl(loanCalculator);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		Integer numberOfPayments = paymentSchedule.getNumberOfPayments();
		assertTrue(numberOfPayments.intValue() == 36);
	}

	public void testFirstInterestPayment() throws Exception {
		LoanCalculator loanCalculator = new DesiredPaymentScheduleCalculator(this.desiredPayment, this.presentValue, this.rate, this.term);
		Loan loan = new LoanImpl(loanCalculator);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		java.util.Iterator payments = paymentSchedule.getPayments();
		Payment payment = (Payment) payments.next();
		assertTrue(payment.getInterest().equals(new BigDecimal("150.00")));
	}

	public void testFirstPrincipalPayment() throws Exception {
		LoanCalculator loanCalculator = new DesiredPaymentScheduleCalculator(this.desiredPayment, this.presentValue, this.rate, this.term);
		Loan loan = new LoanImpl(loanCalculator);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		java.util.Iterator payments = paymentSchedule.getPayments();
		Payment payment = (Payment) payments.next();
		assertTrue(payment.getPrincipal().equals(new BigDecimal("350.00")));
	}

	public void testCumulativeInterest() {
		LoanCalculator loanCalculator = new DesiredPaymentScheduleCalculator(this.desiredPayment, this.presentValue, this.rate, this.term);
		Loan loan = new LoanImpl(loanCalculator);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		assertTrue(loan.getCumulativeInterest().equals(new BigDecimal("2923.09")));
	}

	public void testVerifyFinalPrincipalPayment() {
		LoanCalculator loanCalculator = new DesiredPaymentScheduleCalculator(this.desiredPayment, this.presentValue, this.rate, this.term);
		Loan loan = new LoanImpl(loanCalculator);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		java.util.Iterator payments = paymentSchedule.getPayments();
		Payment payment = null;
		while (payments.hasNext()) {
			payment = (Payment) payments.next();
		}
		assertTrue(payment.getPrincipal().equals(new BigDecimal("418.90")));
	}

	public void testVerifyFinalInterestPayment() {
		LoanCalculator loanCalculator = new DesiredPaymentScheduleCalculator(this.desiredPayment, this.presentValue, this.rate, this.term);
		Loan loan = new LoanImpl(loanCalculator);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		java.util.Iterator payments = paymentSchedule.getPayments();
		Payment payment = null;
		while (payments.hasNext()) {
			payment = (Payment) payments.next();
		}
		assertTrue(payment.getInterest().equals(new BigDecimal("4.19")));
	}

}
