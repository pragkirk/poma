package com.extensiblejava.loan.impl;

import java.math.*;
import java.util.*;
import com.extensiblejava.loan.*;


public class LoanImpl implements Loan {
	private PaymentSchedule paymentSchedule;
	private BigDecimal cumulativeInterest;
	private BigDecimal cumulativePrincipal;

	public LoanImpl(PaymentSchedule paymentSchedule, BigDecimal cumulativeInterest, BigDecimal cumulativePrincipal) {
		//this.loanCalculator = loanCalculator;
		this.paymentSchedule = paymentSchedule;
		this.cumulativeInterest = cumulativeInterest;
		this.cumulativePrincipal = cumulativePrincipal;
	}

	public PaymentSchedule calculatePaymentSchedule() {
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

	public BigDecimal getCumulativeInterest() { return this.cumulativeInterest; }
	public BigDecimal getCumulativePrincipal() { return this.cumulativePrincipal; }
	public BigDecimal getTotalPayments() {
		BigDecimal totalPayments =  this.cumulativePrincipal.add(this.cumulativeInterest);
		totalPayments = totalPayments.setScale(2, BigDecimal.ROUND_HALF_UP);
		return totalPayments;
	}

}