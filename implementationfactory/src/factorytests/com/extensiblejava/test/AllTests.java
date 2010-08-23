package com.extensiblejava.test;

import junit.framework.*;
import junit.textui.*;
import com.extensiblejava.calculator.test.*;

public class AllTests extends TestCase {

	public AllTests(String name) {
		super(name);
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(AllTests.suite());
	}

	public static Test suite() {
		TestSuite packageTests = new TestSuite(AllTests.class.getName());
		packageTests.addTestSuite(com.extensiblejava.applicant.test.ApplicantTest.class);
		packageTests.addTestSuite(com.extensiblejava.calculator.test.DesiredPaymentScheduleCalculatorTest.class);
		packageTests.addTestSuite(com.extensiblejava.calculator.test.MinimumPaymentScheduleCalculatorTest.class);

		return packageTests;

	}

}