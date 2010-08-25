package com.extensiblejava.customer;

import com.extensiblejava.order.*;
import java.math.BigDecimal;

public interface Customer {

	public String getName();
	public Order[] getOrders();
	public Order createNewOrder(Integer productQuantity, BigDecimal chargeAmount);
}