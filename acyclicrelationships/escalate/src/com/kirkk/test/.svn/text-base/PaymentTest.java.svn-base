package com.kirkk.test;

import junit.framework.TestCase;
import java.math.BigDecimal;
import java.util.*;
import com.kirkk.cust.*;
import com.kirkk.bill.*;
import com.kirkk.mediator.*;

public class PaymentTest extends TestCase {

	public PaymentTest(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
	}

	protected void setUp() throws Exception {

	}

	public void testPayment() {

		Customer customer = new Customer();
		customer.createBill(new BigDecimal(500));

		Iterator bills = customer.getBills().iterator();

		BigDecimal discount = new BigDecimal(1).subtract(new CustomerMediator(customer).getDiscountAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
		while (bills.hasNext()) {
			Bill bill = (Bill) bills.next();
			BigDecimal paidAmount = bill.pay(discount);
			assertEquals("Paid amount not correct.", new BigDecimal(485).setScale(2), paidAmount);
		}
	}

}
