package com.extensiblejava.loan.impl;

import java.math.*;
import java.util.*;
import com.extensiblejava.loan.*;


public class LoanImpl implements Loan {
	private LoanCalculator loanCalculator;
	private PaymentSchedule paymentSchedule;

	public LoanImpl(LoanCalculator loanCalculator) {
		this.loanCalculator = loanCalculator;
	}

	public PaymentSchedule calculatePaymentSchedule(BigDecimal presentValue, BigDecimal rate, int term) {
		if (this.paymentSchedule == null) {
			this.paymentSchedule = loanCalculator.calculatePaymentSchedule(presentValue, rate, term);
		}
		return this.paymentSchedule;

	}

	public BigDecimal getMonthlyPayment() {
		/*if (this.paymentSchedule == null) {
			this.paymentSchedule = loanCalculator.calculatePaymentSchedule();
		}*/
		Iterator payments = this.paymentSchedule.getPayments();
		BigDecimal monthlyPayment = null;
		if (payments.hasNext()) {
			Payment payment = (Payment) payments.next();
			monthlyPayment = payment.getPrincipal().add(payment.getInterest());
			monthlyPayment = monthlyPayment.setScale(2, BigDecimal.ROUND_HALF_UP);
		}

		return monthlyPayment;
	}

	public BigDecimal getFinalPayment() {
		/*if (this.paymentSchedule == null) {
			this.paymentSchedule = loanCalculator.calculatePaymentSchedule();
		}*/
		Iterator payments = this.paymentSchedule.getPayments();
		Payment payment = null;
		while (payments.hasNext()) {
			payment = (Payment) payments.next();
		}
		BigDecimal finalPayment = payment.getPrincipal().add(payment.getInterest());;
		finalPayment = finalPayment.setScale(2, BigDecimal.ROUND_HALF_UP);
		return finalPayment;

	}

	public BigDecimal getCumulativeInterest() { return this.loanCalculator.getCumulativeInterest(); }
	public BigDecimal getCumulativePrincipal() { return this.loanCalculator.getCumulativePrincipal(); }
	public BigDecimal getTotalPayments() {
		BigDecimal totalPayments =  this.loanCalculator.getCumulativePrincipal().add(this.loanCalculator.getCumulativeInterest());
		totalPayments = totalPayments.setScale(2, BigDecimal.ROUND_HALF_UP);
		return totalPayments;
	}

}