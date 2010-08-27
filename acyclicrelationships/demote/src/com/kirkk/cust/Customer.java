package com.kirkk.cust;

import java.util.*;
import java.math.BigDecimal;
import com.kirkk.bill.*;
import com.kirkk.calc.*;

public class Customer {
	private List bills;

	public List getBills() {
		return this.bills;
	}

	//because of real-time need to calculate discount based on num bills, this is not a very natural approach. This method
	//is needed to ensure the calculator has the right number of bills. Could easily get out of sync.
	public DiscountCalculator getDiscountCalculator() {
		return new DiscountCalculator(new Integer(bills.size()));
	}

	public void createBill(BigDecimal chargeAmount) {
		Bill bill = new Bill(chargeAmount);
		if (bills == null) {
			bills = new ArrayList();
		}
		bills.add(bill);
	}

}
