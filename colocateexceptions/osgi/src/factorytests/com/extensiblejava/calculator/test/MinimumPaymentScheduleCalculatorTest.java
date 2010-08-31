package com.extensiblejava.calculator.test;

import junit.framework.*;
import junit.textui.*;
import java.math.*;
import com.extensiblejava.loan.*;
import com.extensiblejava.calculator.*;
import org.springframework.context.*;
import org.springframework.context.support.*;

public class MinimumPaymentScheduleCalculatorTest extends TestCase
{

	private BigDecimal presentValue;
	private BigDecimal rate;
	private int term;

	public static void main(String[] args)
	{
		String[] testCaseName = { MinimumPaymentScheduleCalculatorTest.class.getName() };

		junit.textui.TestRunner.main(testCaseName);
	}

	protected void setUp() {
		this.presentValue = new BigDecimal("15000.00");
		this.rate = new BigDecimal("12.0");
		this.term = 60;
	}

	public void testMonthlyPayment() {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath*:com/extensiblejava/calculator/test/TestContext.xml");
		MinimumPaymentScheduleCalculator loanCalculator = (MinimumPaymentScheduleCalculator) appContext.getBean("loanCalculator");
		//Loan loan = new LoanMock(loanCalculator);
		Loan loan = loanCalculator.calculateLoan(this.presentValue, this.rate, this.term);
		BigDecimal monthlyPayment = loan.getMonthlyPayment();
		assertTrue(monthlyPayment.equals(new BigDecimal("333.67")));
	}

	public void testFinalPayment() {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath*:com/extensiblejava/calculator/test/TestContext.xml");
		MinimumPaymentScheduleCalculator loanCalculator = (MinimumPaymentScheduleCalculator) appContext.getBean("loanCalculator");
		Loan loan = loanCalculator.calculateLoan(this.presentValue, this.rate, this.term);
		BigDecimal finalPayment = loan.getFinalPayment();
		assertTrue(finalPayment.equals(new BigDecimal("333.40")));
	}

	public void testNumberOfPayments() {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath*:com/extensiblejava/calculator/test/TestContext.xml");
		MinimumPaymentScheduleCalculator loanCalculator = (MinimumPaymentScheduleCalculator) appContext.getBean("loanCalculator");
		Loan loan = loanCalculator.calculateLoan(this.presentValue, this.rate, this.term);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		Integer numberOfPayments = paymentSchedule.getNumberOfPayments();
		assertTrue(numberOfPayments.intValue() == 60);
	}

	public void testFirstInterestPayment() throws Exception {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath*:com/extensiblejava/calculator/test/TestContext.xml");
		MinimumPaymentScheduleCalculator loanCalculator = (MinimumPaymentScheduleCalculator) appContext.getBean("loanCalculator");
		Loan loan = loanCalculator.calculateLoan(this.presentValue, this.rate, this.term);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		java.util.Iterator payments = paymentSchedule.getPayments();
		Payment payment = (Payment) payments.next();
		assertTrue(payment.getInterest().equals(new BigDecimal("150.00")));
	}

	public void testFirstPrincipalPayment() throws Exception {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath*:com/extensiblejava/calculator/test/TestContext.xml");
		MinimumPaymentScheduleCalculator loanCalculator = (MinimumPaymentScheduleCalculator) appContext.getBean("loanCalculator");
		Loan loan = loanCalculator.calculateLoan(this.presentValue, this.rate, this.term);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		java.util.Iterator payments = paymentSchedule.getPayments();
		Payment payment = (Payment) payments.next();
		assertTrue(payment.getPrincipal().equals(new BigDecimal("183.67")));
	}

	public void testCumulativeInterest() throws Exception {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath*:com/extensiblejava/calculator/test/TestContext.xml");
		MinimumPaymentScheduleCalculator loanCalculator = (MinimumPaymentScheduleCalculator) appContext.getBean("loanCalculator");
		Loan loan = loanCalculator.calculateLoan(this.presentValue, this.rate, this.term);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		assertTrue(loan.getCumulativeInterest().equals(new BigDecimal("5019.93")));
	}

	public void testVerifyFinalPrincipalPayment() throws Exception {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath*:com/extensiblejava/calculator/test/TestContext.xml");
		MinimumPaymentScheduleCalculator loanCalculator = (MinimumPaymentScheduleCalculator) appContext.getBean("loanCalculator");
		Loan loan = loanCalculator.calculateLoan(this.presentValue, this.rate, this.term);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		java.util.Iterator payments = paymentSchedule.getPayments();
		Payment payment = null;
		while (payments.hasNext()) {
			payment = (Payment) payments.next();
		}
		assertTrue(payment.getPrincipal().equals(new BigDecimal("330.10")));
	}

	public void testVerifyFinalInterestPayment() throws Exception {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath*:com/extensiblejava/calculator/test/TestContext.xml");
		MinimumPaymentScheduleCalculator loanCalculator = (MinimumPaymentScheduleCalculator) appContext.getBean("loanCalculator");
		Loan loan = loanCalculator.calculateLoan(this.presentValue, this.rate, this.term);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		java.util.Iterator payments = paymentSchedule.getPayments();
		Payment payment = null;
		while (payments.hasNext()) {
			payment = (Payment) payments.next();
		}
		assertTrue(payment.getInterest().equals(new BigDecimal("3.30")));
	}

}
