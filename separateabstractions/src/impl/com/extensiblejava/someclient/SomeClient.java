package com.extensiblejava.someclient;

import java.math.*;
import com.extensiblejava.applicant.*;
import com.extensiblejava.loan.*;
import com.extensiblejava.calculator.*;
import com.extensiblejava.applicant.*;
import org.springframework.context.*;
import org.springframework.context.support.*;

public class SomeClient {
	public static void main(String args[]) {
		//ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath*:com/extensiblejava/client/AppContext.xml");
		ApplicationContext appContext = new FileSystemXmlApplicationContext("com/extensiblejava/someclient/AppContext.xml");
		Applicant applicant = (Applicant) appContext.getBean("applicant");
		Loan loan = applicant.obtainLoanInformation();
		//Loan loan = (Loan) appContext.getBean("loan");
		//PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		BigDecimal monthlyPayment = loan.getMonthlyPayment();
		System.out.println(monthlyPayment);
	}
}