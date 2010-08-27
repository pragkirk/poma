package com.kirkk.bill;

import java.math.BigDecimal;

public class Bill {

	private BigDecimal chargeAmount;

	public Bill(BigDecimal chargeAmount) {
			this.chargeAmount = chargeAmount;
	}
	public BigDecimal getChargeAmount() {
		return this.chargeAmount;
	}

	public BigDecimal pay(BigDecimal discount) {
		//BigDecimal discount = new BigDecimal(1).subtract(this.customer.getDiscountAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
		BigDecimal paidAmount = this.chargeAmount.multiply(discount).setScale(2);
		//make the payment...
		return paidAmount;
	}

}