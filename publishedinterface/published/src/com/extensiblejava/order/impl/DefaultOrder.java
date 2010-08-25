package com.extensiblejava.order.impl;

import java.math.BigDecimal;
import com.extensiblejava.order.*;

public class DefaultOrder implements Order {

	private Integer productQuantity;
	private BigDecimal chargeAmount;
	public BigDecimal discountAmount;

	public DefaultOrder(Integer productQuantity, BigDecimal chargeAmount) {
		this.productQuantity = productQuantity;
		this.chargeAmount = chargeAmount;
	}

	public Integer getProductQuantity() { return this.productQuantity; }
	public BigDecimal getChargeAmount() { return this.chargeAmount; }
	public BigDecimal getDiscountAmount() { return this.discountAmount; }

	public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
}