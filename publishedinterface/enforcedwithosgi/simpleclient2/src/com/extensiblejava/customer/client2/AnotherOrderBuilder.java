package com.extensiblejava.customer.client2;

import java.math.BigDecimal;
import com.extensiblejava.order.*;

public class AnotherOrderBuilder implements OrderBuilder {
	public Order[] build() {
		DefaultOrder[] orders = new DefaultOrder[3];
		orders[0] = new DefaultOrder(new Integer(3), new BigDecimal("2000.00"));
		orders[0].setDiscountAmount(new BigDecimal("0.15"));
		orders[1] = new DefaultOrder(new Integer(50), new BigDecimal("1000.00"));
		orders[1].setDiscountAmount(new BigDecimal("0.20"));
		orders[2] = new DefaultOrder(new Integer(45), new BigDecimal("10000.00"));
		orders[2].setDiscountAmount(new BigDecimal("0.30"));
		return orders;
	}
}