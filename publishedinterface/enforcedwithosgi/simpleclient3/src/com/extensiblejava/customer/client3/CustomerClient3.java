package com.extensiblejava.customer.client3;

import com.extensiblejava.customer.Customer;
//import com.extensiblejava.customer.impl.DefaultCustomer; //Can't do this because customer.jar doesn't export it.
import com.extensiblejava.order.Order;

public class CustomerClient3 {
	private Customer customer;

	public void setCustomerService3(Customer customer ) {
		this.customer = customer;
	}

	public void start() throws Exception {
	   /*try {
		   DefaultCustomer c = (DefaultCustomer) this.customer;
		} catch (Exception e) {
			System.out.println("Can't cast dude! That class isn't available!");
		}*/
       System.out.println("Hi Ya: " + customer.getName());
	   Order[] order = customer.getOrders();
	   for (int i=0;i<order.length;i++) {
	      Order o = order[i];
	      System.out.println("Quantity: " + o.getProductQuantity());	
	      System.out.println("Charge: " + o.getChargeAmount());
	      System.out.println("Discount: " + o.getDiscountAmount());
	   }
	}

	public void stop() throws Exception {
		System.out.println("See Ya: " + customer.getName());
		// NOTE: The service is automatically released.
    }

}

