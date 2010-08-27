package com.kirkk.bill;

import java.math.BigDecimal;

public class Bill {

	private BigDecimal chargeAmount;
	private DiscountCalculator discounter;

	public Bill(DiscountCalculator discounter, BigDecimal chargeAmount) {
			this.discounter = discounter;
			this.chargeAmount = chargeAmount;
	}
	public BigDecimal getChargeAmount() {
		return this.chargeAmount;
	}

	public BigDecimal pay() {
		BigDecimal discount = new BigDecimal(1).subtract(this.discounter.getDiscountAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
		BigDecimal paidAmount = this.chargeAmount.multiply(discount).setScale(2);
		//make the payment...
		return paidAmount;
	}

}