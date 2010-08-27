package com.extensiblejava.test;

import junit.framework.TestCase;
import com.extensiblejava.bill.*;
import com.extensiblejava.route.*;
import java.math.*;

public class RouterTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(RouterTest.class);
	}

	public void testARouting() {
		Bill bill = new Bill("A", "1", new BigDecimal("2000.00"), new TypeARouter());
		String location = bill.route();
		assertEquals(location, "A_LOCATION_");
	}

	/*public void testBBillHighPriorityRouting() {
		Bill bill = new Bill("A", "1", new BigDecimal("2000.00"), new TypeBRouter());
		String location = bill.route();
		assertEquals(location, "B_LOCATION_01");
	}

	public void testBBillLowPriorityRouting() {
		Bill bill = new Bill("A", "1", new BigDecimal("20.00"), new TypeBRouter());
		String location = bill.route();
		assertEquals(location, "B_LOCATION_02");
	}

	public void testANonBillRouting() {
		NonBillLoader loader = new NonBillLoader() {
			public NonBill loadNonBill() {
				NonBill nonBill = new NonBill("A", "1", new TypeARouter());
				return nonBill;
			}
		};
		NonBill nonBill = NonBill.getInstance(loader);
		String location = nonBill.route();
		assertEquals(location, "A_LOCATION");
	}

	public void testBNonBillRouting() {
		NonBillLoader loader = new NonBillLoader() {
			public NonBill loadNonBill() {
				NonBill nonBill = new NonBill("A", "1", new TypeBRouter());
				return nonBill;
			}
		};
		NonBill nonBill = NonBill.getInstance(loader);
		String location = nonBill.route();
		assertEquals(location, "B_LOCATION");
	}*/

}
