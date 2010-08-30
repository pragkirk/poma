package com.extensiblejava.policy.test;

import junit.framework.*;
import junit.textui.*;
import com.extensiblejava.policy.*;
import java.util.*;
import java.io.*;

public class PolicyXMLTest extends TestCase
{

	public static void main(String[] args)
	{
		String[] testCaseName = { PolicyXMLTest.class.getName() };

		junit.textui.TestRunner.main(testCaseName);
	}

	protected void setUp() {
	}

	public void testPolicy() throws Exception {

		String policyXML = "<policy>"+
							"<firstname>Jane</firstname>"+
							"<lastname>Doe</lastname>"+
							"<maritalstatus>M</maritalstatus>"+
							"<dateofbirth>01/10/1967</dateofbirth>"+
							"<tobaccouser>N</tobaccouser>"+
							"</policy>";
		Policy policy = new Policy(policyXML);
		assertEquals("Jane", policy.getFirstName());
		assertEquals("Doe", policy.getLastName());
		assertEquals("M", policy.getMaritalStatus());
		assertEquals("N", policy.getTobaccoUser());

		Calendar cal = Calendar.getInstance();
		cal.setTime(policy.getDateOfBirth());
		assertEquals(10, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(1967, cal.get(Calendar.YEAR));
		assertEquals(1, cal.get(Calendar.MONTH) + 1);
	}
}
