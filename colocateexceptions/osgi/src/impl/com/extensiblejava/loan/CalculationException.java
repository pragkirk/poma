package com.extensiblejava.loan;

import java.math.*;

public class CalculationException extends RuntimeException {
	public CalculationException(Throwable t) {
		super(t);
	}

}