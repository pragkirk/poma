package com.kirkk.cust;

import java.util.*;
import java.math.BigDecimal;
import com.kirkk.bill.*;

public class Customer {
	private List bills;

	public List getBills() {
		return this.bills;
	}

	public void createBill(BigDecimal chargeAmount) {
		Bill bill = new Bill(chargeAmount);
		if (bills == null) {
			bills = new ArrayList();
		}
		bills.add(bill);
	}

}
