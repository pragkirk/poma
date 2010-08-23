package com.extensiblejava.calculator;

import java.math.BigDecimal;

class MonthlyPaymentCalculator {

	public BigDecimal calculatePayment(BigDecimal presentValue, BigDecimal rate, int term) {
		double dPresentValue = presentValue.doubleValue();
		double dRate = rate.doubleValue() / (1200);

		double revisedRate = dRate + 1;
		double dTerm = term;
		double powRate = Math.pow(revisedRate, dTerm);

		double left = powRate * dPresentValue;

		double middle = dRate / (powRate - 1);

		double right = 1 / (1);

		BigDecimal payment = new BigDecimal(left * middle * right);
		return payment.setScale(2, BigDecimal.ROUND_UP);

	}
}