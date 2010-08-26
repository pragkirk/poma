package com.extensiblejava.calculator.better;

import com.extensiblejava.calculator.*;
import com.extensiblejava.order.*;
import java.math.BigDecimal;

public class BetterCalculator implements DiscountCalculator {

	public BigDecimal calculateDiscount(Order[] orders) {
		int totalQuantity = 0;
		for (int i = 0; i < orders.length; i++) {
			totalQuantity += orders[i].getProductQuantity().intValue();
		}
		if (totalQuantity < 10) {
			return new BigDecimal("0.15");
		} else if ( (totalQuantity >= 10) && (totalQuantity < 100) ) {
			return new BigDecimal("0.20");
		} else if (totalQuantity >= 100) {
			return new BigDecimal("0.50");
		}
		return new BigDecimal("0.00");
	}
}