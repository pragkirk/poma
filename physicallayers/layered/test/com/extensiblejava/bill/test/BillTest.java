package com.extensiblejava.bill.test;

import java.util.*;
import junit.framework.*;
import junit.textui.*;
import com.extensiblejava.bill.*;
import com.extensiblejava.bill.data.*;
import java.math.*;

public class BillTest extends TestCase
{
	public static void main(String[] args)
	{
		String[] testCaseName = { BillTest.class.getName() };

		junit.textui.TestRunner.main(testCaseName);
	}

	protected void setUp() {

	}

	public void testCustomerLoader() {
		Customer cust = Customer.loadCustomer(new DefaultCustomerEntityLoader(new Integer(1)));
		assertNotNull(cust.getName());

		Iterator bills = cust.getBills().iterator();
		while (bills.hasNext()) {
			assertNotNull(bills.next());
		}
	}

	public void testBillLoader() {
		Bill bill = Bill.loadBill(new BillEntityLoader() {
			public Bill loadBill() {
				return new Bill(new BillDataBean(new Integer(1), new Integer(1), "ONE", new BigDecimal("25.00"), null, null));
			}
		});
		assertNotNull(bill);
	}

	public void testAudit() {
		Bill bill = Bill.loadBill(new BillEntityLoader() {
			public Bill loadBill() {
				return new Bill(new BillDataBean(new Integer(1), new Integer(1), "ONE", new BigDecimal("25.00"), null, null));
			}
		});
		bill.audit();
		BigDecimal auditedAmount = bill.getAuditedAmount();
		assertEquals(new BigDecimal("18.75"),auditedAmount);
	}

	public void testPay() {
		Bill bill = Bill.loadBill(new BillEntityLoader() {
			public Bill loadBill() {
				return new Bill(new BillDataBean(new Integer(1), new Integer(1), "ONE", new BigDecimal("25.00"), null, null));
			}
		});
		bill.pay();
		assertEquals(bill.getPaidAmount(), bill.getAmount());
	}

	public void testAuditAfterPay() {
		Bill bill = Bill.loadBill(new BillEntityLoader() {
			public Bill loadBill() {
				return new Bill(new BillDataBean(new Integer(1), new Integer(1), "ONE", new BigDecimal("25.00"), null, null));
			}
		});
		bill.pay();
		BigDecimal paidAmount = bill.getPaidAmount();
		bill.audit();
		BigDecimal paidAmountAfter = bill.getPaidAmount();
		assertEquals(paidAmount, paidAmountAfter);
	}
}
