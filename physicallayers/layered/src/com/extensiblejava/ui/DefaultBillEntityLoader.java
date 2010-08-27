package com.extensiblejava.ui;

import com.extensiblejava.bill.data.*;
import com.extensiblejava.bill.*;

public class DefaultBillEntityLoader implements BillEntityLoader {

	private BillDetailForm billForm;

	public DefaultBillEntityLoader(BillDetailForm billForm) {
		this.billForm = billForm;
	}

	public Bill loadBill() {
		BillDataBean billBean = BillDb.getBill(new Integer(this.billForm.getBillId()));
		return new Bill(billBean);
	}
}