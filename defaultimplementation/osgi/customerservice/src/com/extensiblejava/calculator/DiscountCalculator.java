package com.extensiblejava.calculator;

import com.extensiblejava.order.*;
import java.math.BigDecimal;

public interface DiscountCalculator {

	public BigDecimal calculateDiscount(Order[] orders);
	
}