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

}