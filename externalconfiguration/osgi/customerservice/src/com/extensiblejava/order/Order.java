package com.extensiblejava.order;

import java.math.BigDecimal;

public interface Order {

	public Integer getProductQuantity();
	public BigDecimal getChargeAmount();
	public BigDecimal getDiscountAmount();
}