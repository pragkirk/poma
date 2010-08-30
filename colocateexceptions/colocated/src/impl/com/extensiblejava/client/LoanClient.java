package com.extensiblejava.client;

import java.math.*;
import com.extensiblejava.loan.*;
import com.extensiblejava.calculator.*;
import com.extensiblejava.facade.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import java.io.*;
import java.util.*;

public class LoanClient {
	
	public static void main(String args[]) {
		LoanClient loanClient = new LoanClient();
		try {
			loanClient.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			LoanFacade loanFacade = new LoanFacade();
			BigDecimal payment = loanFacade.getMonthlyPayment(loanAmount, loanRate, loanTerm);
			System.out.println("Payment: " + payment); 
		} else {
			LoanFacade loanFacade = new LoanFacade();
			PaymentSchedule paymentSchedule = loanFacade.calculatePaymentSchedule(loanAmount, loanRate, loanTerm);
			Iterator payments = paymentSchedule.getPayments();
			System.out.println("INTEREST           PRINCIPAL");
			System.out.println("----------------------------");
			while (payments.hasNext()) {
				Payment payment = (Payment) payments.next();
				System.out.println(payment.getInterest() + "          " + payment.getPrincipal());
			}
		}
	}
}