package com.extensiblejava.dumbclient.impl;

import java.math.*;
import com.extensiblejava.dumbclient.*;
import com.extensiblejava.loan.*;
import com.extensiblejava.facade.*;
import java.io.*;
import java.util.*;

public class DumbClientImpl implements LoanClient {
	private LoanFacade loanFacade;

	public void setLoanFacade(LoanFacade loanFacade) {
		this.loanFacade = loanFacade;
	}
	
	public void run() throws Exception {
		//LoanFacade loanFacade = new LoanFacade();
		BigDecimal payment = this.loanFacade.getMonthlyPayment(new BigDecimal("15000"), new BigDecimal("12"), 60);
		System.out.println("Payment: " + payment); 
	}
	
	public void stop() throws Exception {
		System.out.println("GOODBYE LOAN!");
	}
}