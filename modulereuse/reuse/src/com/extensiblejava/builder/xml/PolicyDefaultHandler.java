package com.extensiblejava.builder.xml;

import java.util.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import com.extensiblejava.policy.*;

class PolicyDefaultHandler extends DefaultHandler {

	private PolicyXMLBuilder policyXMLBuilder;
	private String attribute;

	public PolicyDefaultHandler(PolicyXMLBuilder policyXMLBuilder) {
		this.policyXMLBuilder = policyXMLBuilder;
	}

	public void characters(char[] ch, int start, int length) {
		String element = new String(ch, start, length);
		this.setPolicyAttribute(element);
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		//System.out.println(uri);
		//System.out.println(localName);
		this.attribute = qName;
	}

	private void setPolicyAttribute(String value) {
		if (this.attribute.equals("firstname")) {
			this.policyXMLBuilder.setFirstName(value);
		} else if (this.attribute.equals("lastname")) {
			this.policyXMLBuilder.setLastName(value);
		} else if (this.attribute.equals("dateofbirth")) {
			Calendar cal = Calendar.getInstance();
			Integer month = new Integer(value.substring(0,2));
			Integer day = new Integer(value.substring(3,5));
			Integer year = new Integer(value.substring(6,10));
			//System.out.println(value.substring(0,2) + "  " + value.substring(3, 5) + "  " + value.substring(6, 10));
			cal.set(year.intValue(), month.intValue() - 1, day.intValue());
			this.policyXMLBuilder.setDateOfBirth(cal.getTime());
		} else if (this.attribute.equals("tobaccouser")) {
			this.policyXMLBuilder.setTobaccoUser(value);
		} else if (this.attribute.equals("maritalstatus")) {
			this.policyXMLBuilder.setMaritalStatus(value);
		}
	}

}