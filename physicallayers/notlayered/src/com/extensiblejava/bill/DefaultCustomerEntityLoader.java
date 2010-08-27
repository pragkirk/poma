package com.extensiblejava.bill;

import java.util.*;
import com.extensiblejava.bill.data.*;

public class DefaultCustomerEntityLoader implements CustomerEntityLoader {
	private Integer custId;

	public DefaultCustomerEntityLoader(Integer custId) {
		this.custId = custId;
	}
	public Customer loadCustomer() {
		CustomerDataBean customer = BillDb.getCustomer(custId);
		return new Customer(this.custId, new Name(customer.getFirstName(), customer.getLastName()), this);
	}

	public List loadBills() {
		Iterator billBeans = BillDb.getBills(this.custId).iterator();

		ArrayList bills = new ArrayList();
		while (billBeans.hasNext()) {
			BillDataBean billBean = (BillDataBean) billBeans.next();
			Bill b = new Bill(billBean);
			bills.add(b);
		}
		return bills;
	}
}