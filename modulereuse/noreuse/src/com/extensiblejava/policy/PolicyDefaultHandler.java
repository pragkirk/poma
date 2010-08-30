package com.extensiblejava.policy;

import java.util.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;

class PolicyDefaultHandler extends DefaultHandler {

	private Policy policy;
	private String attribute;

	public PolicyDefaultHandler(Policy policy) {
		this.policy = policy;
	}

	public void characters(char[] ch, int start, int length) {
		String element = new String(ch, start, length);
		this.setPolicyAttribute(element);
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		this.attribute = qName;
	}

	private void setPolicyAttribute(String value) {
		if (this.attribute.equals("firstname")) {
			policy.setFirstName(value);
		} else if (this.attribute.equals("lastname")) {
			policy.setLastName(value);
		} else if (this.attribute.equals("dateofbirth")) {
			Calendar cal = Calendar.getInstance();
			Integer month = new Integer(value.substring(0,2));
			Integer day = new Integer(value.substring(3,5));
			Integer year = new Integer(value.substring(6,10));
			//System.out.println(value.substring(0,2) + "  " + value.substring(3, 5) + "  " + value.substring(6, 10));
			cal.set(year.intValue(), month.intValue() - 1, day.intValue());
			policy.setDateOfBirth(cal.getTime());
		} else if (this.attribute.equals("tobaccouser")) {
			policy.setTobaccoUser(value);
		} else if (this.attribute.equals("maritalstatus")) {
			policy.setMaritalStatus(value);
		}
	}

}