package com.extensiblejava.ui;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class BillDetailForm extends ActionForm {

	private String billId;

	public void setBillId(String billId) {
		this.billId = billId;
	}

	public String getBillId() {
		return this.billId;
	}

}