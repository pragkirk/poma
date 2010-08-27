package com.extensiblejava.bill.test;

import java.util.*;
import junit.framework.*;
import junit.textui.*;
import com.extensiblejava.bill.data.*;

public class BillDbTest extends TestCase
{
	public static void main(String[] args)
	{
		String[] testCaseName = { BillDbTest.class.getName() };

		junit.textui.TestRunner.main(testCaseName);
	}

	public void testCustomerLoad() {
		CustomerDataBean cust = BillDb.getCustomer(new Integer(1));
		assertEquals(cust.getId(), new Integer(1));
	}

	public void testBillsLoad() {
		Iterator billBeans = BillDb.getBills(new Integer(1)).iterator();

		int i = 1;
		while (billBeans.hasNext()) {
			BillDataBean billBean = (BillDataBean) billBeans.next();
			assertEquals(billBean.getBillId(), new Integer(i));
			i++;
		}
	}
}
