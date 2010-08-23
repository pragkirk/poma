package com.extensiblejava.loan;

public class LoanException extends RuntimeException {

	public LoanException() {
			this(null, null);
	}

	public LoanException(String message) {
		this(message, null);
	}

	public LoanException(Throwable throwable) {
		this(null, throwable);
	}

	public LoanException(String message, Throwable throwable) {
		super(message, throwable);
	}

}