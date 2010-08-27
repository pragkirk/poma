package com.extensiblejava.ui;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.util.MessageResources;
import java.util.*;
import com.extensiblejava.bill.*;
import com.extensiblejava.bill.data.*;

public class BillDetailAction extends Action {
	public ActionForward perform(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response)
	throws IOException, ServletException {
		BillDetailForm billDetailForm = (BillDetailForm) form;
		Bill bill = Bill.loadBill(new DefaultBillEntityLoader(billDetailForm));

		/*Customer customer = Customer.loadCustomer(new EntityLoader() {
			public Customer loadCustomer() {
				return new Customer(new Name("Sue", "Smith"), this);
			}

			public List loadBills() {
				Bill b1 = new Bill(new Integer(1), "ONE");
				Bill b2 = new Bill(new Integer(2), "TWO");
				Bill b3 = new Bill(new Integer(3), "THREE");
				Bill b4 = new Bill(new Integer(4), "FOUR");
				Bill b5 = new Bill(new Integer(5), "FIVE");

				ArrayList bills = new ArrayList();
				bills.add(b1);
				bills.add(b2);
				bills.add(b3);
				bills.add(b4);
				bills.add(b5);

				return bills;
			}
		});*/

		//CustomerSearchResultsBean bean = new CustomerSearchResultsBean(customer);
		request.setAttribute("bill",bill);
		return (mapping.findForward("success"));
	}

}