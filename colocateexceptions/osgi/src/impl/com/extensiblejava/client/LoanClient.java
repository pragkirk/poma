package com.extensiblejava.client;

import java.math.*;
import com.extensiblejava.loan.*;
import com.extensiblejava.facade.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import java.io.*;
import java.util.*;

public class LoanClient {
	private LoanFacade loanFacade;
	
	public static void main(String args[]) {
		LoanClient loanClient = new LoanClient();
		try {
			//ApplicationContext appContext = new FileSystemXmlApplicationContext("com/extensiblejava/facade/AppContext.xml");
			String[] configs = new String[3];
			configs[0] = "classpath*:META-INF/spring/loanfacade.xml";
			configs[1] = "classpath*:META-INF/spring/calculator.xml";
			configs[2] = "classpath*:META-INF/spring/loan.xml";
				configs[0] = "classpath*:META-INF/spring/loanfacade.xml";
				configs[1] = "classpath*:META-INF/spring/calculator.xml";
				configs[2] = "classpath*:META-INF/spring/loan.xml";
			ApplicationContext appContext = new ClassPathXmlApplicationContext(configs);
			LoanFacade loanFacade = (LoanFacade) appContext.getBean("loanFacade");
			loanClient.setLoanFacade(loanFacade);
			loanClient.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 

	public void setLoanFacade(LoanFacade loanFacade) {
		this.loanFacade = loanFacade;
	}
	
	public void run() throws Exception {
		InputStreamReader streamReader = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(streamReader);

		System.out.print("Enter loan amount (required): ");
		String amount = reader.readLine();
		BigDecimal loanAmount = new BigDecimal(amount);

		System.out.print("Enter interest rate (required): ");
		String rate = reader.readLine();
		BigDecimal loanRate = new BigDecimal(rate);

		System.out.print("Enter term in months (required): ");
		String term = reader.readLine();
		Integer loanTerm = new Integer(term);
		
		System.out.print("Payment (p) or Schedule (s): ");
		String display = reader.readLine();
		if (display.toLowerCase().startsWith("p")) {
			//LoanFacade loanFacade = new LoanFacade();
			BigDecimal payment = this.loanFacade.getMonthlyPayment(loanAmount, loanRate, loanTerm);
			System.out.println("Payment: " + payment); 
		} else {
			//LoanFacade loanFacade = new LoanFacade();
			PaymentSchedule paymentSchedule = this.loanFacade.calculatePaymentSchedule(loanAmount, loanRate, loanTerm);
			Iterator payments = paymentSchedule.getPayments();
			System.out.println("INTEREST           PRINCIPAL");
			System.out.println("----------------------------");
			while (payments.hasNext()) {
				Payment payment = (Payment) payments.next();
				System.out.println(payment.getInterest() + "          " + payment.getPrincipal());
			}
		}
	}
	
	public void stop() throws Exception {
		System.out.println("GOODBYE LOAN!");
	}
}