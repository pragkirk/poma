package com.extensiblejava.customer.test;

import junit.framework.*;
import junit.textui.*;
import com.extensiblejava.customer.*;
import com.extensiblejava.order.*;
import java.math.BigDecimal;

public class CustomerTest extends TestCase
{

	public static void main(String[] args)
	{
		String[] testCaseName = { CustomerTest.class.getName() };

		junit.textui.TestRunner.main(testCaseName);
	}

	protected void setUp() {
	}

	public void testCustomerLoad() throws Exception {
		CustomerBuilder builder = new DefaultCustomerBuilder();
		Customer customer = builder.build();
		assertNotNull(customer);
		assertEquals("John Doe", customer.getName());
	}

	public void testCustomerOrderLoad() throws Exception {
		CustomerBuilder builder = new DefaultCustomerBuilder();
		Customer customer = builder.build();
		Order[] orders = customer.getOrders();
		assertEquals(orders.length, 3);
	}

	public void testPlaceOrder() {
		CustomerBuilder builder = new DefaultCustomerBuilder();
		Customer customer = builder.build();
		Order order = customer.createNewOrder(new Integer(3), new BigDecimal("50.00"));
		order.setDiscountAmount(new BigDecimal("0.50")); //I don't want to allow this.
		assertEquals(order.getDiscountAmount(), new BigDecimal("0.50"));
	}
}
