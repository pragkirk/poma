package com.extensiblejava.applicant;

import com.extensiblejava.loan.*;

public class Applicant {
	Loan loan;
	PaymentSchedule paymentSchedule;
	
	public Applicant (Loan loan) {
		this.loan = loan;
		this.paymentSchedule = this.loan.calculatePaymentSchedule();
	}
	
	public Loan obtainLoanInformation () {
		return loan;
		//determine if applicant can afford the payments by running a credit check.
	}
}