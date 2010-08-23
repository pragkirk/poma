package com.extensiblejava.applicant.test;

import junit.framework.*;
import junit.textui.*;
import java.math.*;
import com.extensiblejava.loan.*;
import com.extensiblejava.calculator.*;
import com.extensiblejava.applicant.*;
import org.springframework.context.*;
import org.springframework.context.support.*;

public class ApplicantTest extends TestCase
{

	//private BigDecimal presentValue;
	//private BigDecimal rate;
	//private int term;

	public static void main(String[] args)
	{
		String[] testCaseName = { ApplicantTest.class.getName() };

		junit.textui.TestRunner.main(testCaseName);
	}

	protected void setUp() {
		//this.presentValue = new BigDecimal("15000.00");
		//this.rate = new BigDecimal("12.0");
		//this.term = 60;
	}
	
	public void testApplicant() {
			//LoanCalculator loanCalculator = new MinimumPaymentScheduleCalculator(this.presentValue, this.rate, this.term);
			//Loan loan = new LoanImpl(loanCalculator);
			ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath*:com/extensiblejava/applicant/test/TestContext.xml");
			Applicant applicant = (Applicant) appContext.getBean("applicant");
			Loan loan = applicant.obtainLoanInformation();
			//Loan loan = (Loan) appContext.getBean("loan");
			//PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
			BigDecimal monthlyPayment = loan.getMonthlyPayment();
			assertTrue("Expected 333.67 but got " + monthlyPayment.toString(), monthlyPayment.equals(new BigDecimal("333.67")));
	}

}
