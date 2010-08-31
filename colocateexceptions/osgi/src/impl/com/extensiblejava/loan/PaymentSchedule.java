package com.extensiblejava.loan;
import java.util.*;
public interface PaymentSchedule {
	public void addPayment(Payment payment);
	public Iterator getPayments();
	public Integer getNumberOfPayments();
}