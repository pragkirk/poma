package com.kirkk.base;

import java.math.BigDecimal;

public interface Bill {
	public BigDecimal getChargeAmount();
	public BigDecimal pay();

}