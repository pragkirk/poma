package com.extensiblejava.loan;
import java.math.*;
public interface Payment {
	public BigDecimal getPrincipal();
	public BigDecimal getInterest();
}
