package com.extensiblejava.customer.impl;

import com.extensiblejava.customer.*;
import com.extensiblejava.calculator.*;
import com.extensiblejava.order.*;
import com.extensiblejava.order.impl.*;

import java.math.BigDecimal;

public class DefaultCustomer implements Customer {
	private String fullName;
	private Order[] orders;
	private OrderBuilder builder;
	private DiscountCalculator calculator;
	
	public DefaultCustomer(String fullName, OrderBuilder builder) {
		this.fullName = fullName;
		this.builder = builder;
		this.calculator = calculator;
	}

	public void setDiscounter(DiscountCalculator discountCalculator) {
		this.calculator = discountCalculator;
	}
	
	public String getName() { return this.fullName; }
	public Order[] getOrders() {
		if (this.orders == null) {
			this.orders = builder.build();
		}
		return this.orders;
	}

	public Order createNewOrder(Integer productQuantity, BigDecimal chargeAmount) {
		int numOrders = this.getOrders().length + 1;
		Order[] newOrders = new Order[numOrders];
		System.arraycopy(this.orders, 0, newOrders, 0, this.orders.length);
		DefaultOrder newOrder = new DefaultOrder(productQuantity, chargeAmount);
		newOrders[numOrders - 1] = newOrder;
		this.orders = newOrders;
		BigDecimal discount = this.calculateDiscount();
		newOrder.setDiscountAmount(discount);
		return this.orders[numOrders - 1];
	}

	private BigDecimal calculateDiscount() {
		return this.calculator.calculateDiscount(this.orders);
	}
}