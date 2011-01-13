package com.extensiblejava.calculator;

import java.math.*;
import com.extensiblejava.loan.*;
//import com.extensiblejava.loan.impl.*;

public class MinimumPaymentScheduleCalculator implements LoanCalculator {
	/* private BigDecimal presentValue;
	private BigDecimal rate;
	private int term; */
	private PaymentFactory paymentFactory;
	//private BigDecimal cumulativePrincipal = new BigDecimal("0");
	//private BigDecimal cumulativeInterest = new BigDecimal("0");

	public MinimumPaymentScheduleCalculator(PaymentFactory paymentFactory) {
		this.paymentFactory = paymentFactory;
	}

	public Loan calculateLoan(BigDecimal presentValue, BigDecimal rate, int term) throws CalculationException {
		BigDecimal cumulativePrincipal = new BigDecimal("0");
		BigDecimal cumulativeInterest = new BigDecimal("0");
		try {
			PaymentSchedule paymentSchedule = this.paymentFactory.createPaymentSchedule();
			BigDecimal adjustedRate = rate.divide(new BigDecimal("1200"), 2, BigDecimal.ROUND_HALF_UP);
			MonthlyPaymentCalculator paymentCalculator = new MonthlyPaymentCalculator();
			BigDecimal monthlyPayment = paymentCalculator.calculatePayment(presentValue, rate, term);
			BigDecimal loanBalance = new BigDecimal(presentValue.toString());
			while (loanBalance.doubleValue() > monthlyPayment.doubleValue()) {
				BigDecimal interest = loanBalance.multiply(adjustedRate);
				interest = interest.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal principal = monthlyPayment.subtract(interest);
				principal = principal.setScale(2, BigDecimal.ROUND_HALF_UP);
				//Payment payment = new PaymentImpl(principal, interest);
				Payment payment = this.paymentFactory.createPayment(principal, interest); 
				paymentSchedule.addPayment(payment);

				cumulativeInterest = cumulativeInterest.add(interest).setScale(2, BigDecimal.ROUND_HALF_UP);
				cumulativePrincipal = cumulativePrincipal.add(principal).setScale(2, BigDecimal.ROUND_HALF_UP);
				loanBalance = loanBalance.subtract(principal);
			}

			BigDecimal interest = loanBalance.multiply(adjustedRate).setScale(2, BigDecimal.ROUND_HALF_UP);
			BigDecimal principal = loanBalance.setScale(2, BigDecimal.ROUND_HALF_UP);
			cumulativeInterest = cumulativeInterest.add(interest).setScale(2, BigDecimal.ROUND_HALF_UP);
			cumulativePrincipal = cumulativePrincipal.add(principal).setScale(2, BigDecimal.ROUND_HALF_UP);
			//Payment payment = new PaymentImpl(principal, interest);
			Payment payment = this.paymentFactory.createPayment(principal, interest);
			paymentSchedule.addPayment(payment);
			//return paymentSchedule;
			return this.paymentFactory.createLoan(paymentSchedule, cumulativeInterest, cumulativePrincipal);
		} catch (Exception e) {
			throw new CalculationException(e);
		}
		//PaymentSchedule paymentSchedule = new PaymentScheduleImpl();
		
	}

	//public BigDecimal getCumulativeInterest() { return this.cumulativeInterest; }
	//public BigDecimal getCumulativePrincipal() { return this.cumulativePrincipal; }
}