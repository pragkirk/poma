package com.extensiblejava.bill;

import com.extensiblejava.bill.data.*;
import com.extensiblejava.ui.BillDetailForm;

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