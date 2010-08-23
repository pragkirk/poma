package com.extensiblejava.calculator;

import java.math.*;
import com.extensiblejava.loan.*;
import com.extensiblejava.loan.impl.*;

public class MinimumPaymentScheduleCalculator implements LoanCalculator {
	private BigDecimal presentValue;
	private BigDecimal rate;
	private int term;
	private BigDecimal cumulativePrincipal = new BigDecimal("0");
	private BigDecimal cumulativeInterest = new BigDecimal("0");

	public MinimumPaymentScheduleCalculator(BigDecimal presentValue, BigDecimal rate, int term) {
		this.presentValue = presentValue;
		this.rate = rate;
		this.term = term;
	}

	public PaymentSchedule calculatePaymentSchedule() {
		PaymentSchedule paymentSchedule = new PaymentScheduleImpl();
		BigDecimal adjustedRate = rate.divide(new BigDecimal("1200"), 2, BigDecimal.ROUND_UNNECESSARY);
		MonthlyPaymentCalculator paymentCalculator = new MonthlyPaymentCalculator();
		BigDecimal monthlyPayment = paymentCalculator.calculatePayment(this.presentValue, this.rate, this.term);
		BigDecimal loanBalance = new BigDecimal(this.presentValue.toString());
		while (loanBalance.doubleValue() > monthlyPayment.doubleValue()) {
			BigDecimal interest = loanBalance.multiply(adjustedRate);
			interest = interest.setScale(2, BigDecimal.ROUND_HALF_UP);
			BigDecimal principal = monthlyPayment.subtract(interest);
			principal = principal.setScale(2, BigDecimal.ROUND_HALF_UP);
			Payment payment = new PaymentImpl(principal, interest);
			paymentSchedule.addPayment(payment);

			this.cumulativeInterest = this.cumulativeInterest.add(interest).setScale(2, BigDecimal.ROUND_HALF_UP);
			this.cumulativePrincipal = this.cumulativePrincipal.add(principal).setScale(2, BigDecimal.ROUND_HALF_UP);
			loanBalance = loanBalance.subtract(principal);
		}

		BigDecimal interest = loanBalance.multiply(adjustedRate).setScale(2, BigDecimal.ROUND_HALF_UP);
		BigDecimal principal = loanBalance.setScale(2, BigDecimal.ROUND_HALF_UP);
		this.cumulativeInterest = this.cumulativeInterest.add(interest).setScale(2, BigDecimal.ROUND_HALF_UP);
		this.cumulativePrincipal = this.cumulativePrincipal.add(principal).setScale(2, BigDecimal.ROUND_HALF_UP);
		Payment payment = new PaymentImpl(principal, interest);
		paymentSchedule.addPayment(payment);
		return paymentSchedule;
	}

	public BigDecimal getCumulativeInterest() { return this.cumulativeInterest; }
	public BigDecimal getCumulativePrincipal() { return this.cumulativePrincipal; }
}