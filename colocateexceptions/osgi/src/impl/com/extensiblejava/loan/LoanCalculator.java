package com.extensiblejava.loan;

import java.math.*;

public interface LoanCalculator {
	public Loan calculateLoan(BigDecimal presentValue, BigDecimal rate, int term);
	/*public BigDecimal getCumulativeInterest();
	public BigDecimal getCumulativePrincipal();*/

}