package com.kirkk.bill;

import com.kirkk.cust.*;

import java.math.BigDecimal;

public class Bill {

	private BigDecimal chargeAmount;
	private Customer customer;

	public Bill(Customer customer, BigDecimal chargeAmount) {
			this.customer = customer;
			this.chargeAmount = chargeAmount;
	}
	public BigDecimal getChargeAmount() {
		return this.chargeAmount;
	}

	public BigDecimal pay() {
		BigDecimal discount = new BigDecimal(1).subtract(this.customer.getDiscountAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
		BigDecimal paidAmount = this.chargeAmount.multiply(discount).setScale(2);
		//make the payment...
		return paidAmount;
	}

}