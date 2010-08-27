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

public class CustomerSearchAction extends Action {
	public ActionForward perform(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response)
	throws IOException, ServletException {
		CustomerSearchForm customerSearchForm = (CustomerSearchForm) form;

		Customer customer = Customer.loadCustomer(new DefaultCustomerEntityLoader(new Integer(customerSearchForm.getCustomerId())));

		CustomerSearchResultsBean bean = new CustomerSearchResultsBean(customer);
		request.setAttribute("customerbills",bean);
		return (mapping.findForward("success"));
	}

}