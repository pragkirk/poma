package com.extensiblejava.customer.client;

import com.extensiblejava.customer.*;
//import com.extensiblejava.customer.impl.DefaultCustomer; //Can't do this because customer.jar doesn't export it.
import com.extensiblejava.order.Order;
import com.extensiblejava.calculator.better.BetterCalculator;
import java.math.BigDecimal;

public class CustomerClient {
	private Customer customer;
	//private DiscountCalculator discounter;
	

	public void setCustomerService(CustomerManager customerManager) {
		customerManager.updateDiscounter(new BetterCalculator());
		this.customer = customerManager.getCustomer();
	}

	public void start() throws Exception {
		//this.customer.setDiscounter(discounter);
	   /*try {
		   DefaultCustomer c = (DefaultCustomer) this.customer;
		} catch (Exception e) {
			System.out.println("Can't cast dude! That class isn't available!");
		}*/
	   System.out.println("HELLO WORLD");
       System.out.println("Hello: " + customer.getName());
	   customer.createNewOrder(2000, new BigDecimal(5000.00));
	   Order[] order = customer.getOrders();
	   for (int i=0;i<order.length;i++) {
	      Order o = order[i];
	      System.out.println("Quantity: " + o.getProductQuantity());	
	      System.out.println("Charge: " + o.getChargeAmount());
	      System.out.println("Discount: " + o.getDiscountAmount());
	   }
	}

	public void stop() throws Exception {
		System.out.println("GoodBye: " + customer.getName());
		// NOTE: The service is automatically released.
    }

}

