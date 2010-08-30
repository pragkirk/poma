package com.extensiblejava.builder.xml;

import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import com.extensiblejava.policy.*;

public class PolicyXMLBuilder implements PolicyBuilder {

	private String xmlString;
	private String firstName;
	private String lastName;
	private String tobaccoUser;
	private Date dateOfBirth;
	private String maritalStatus;

	public PolicyXMLBuilder(String xmlString) {
		this.xmlString = xmlString;
	}

	public Policy build() {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			InputSource source = new InputSource(new StringBufferInputStream(xmlString));
			parser.parse(source, new PolicyDefaultHandler(this));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Policy(this.firstName, this.lastName, this.tobaccoUser, this.dateOfBirth, this.maritalStatus);
	}

	void setFirstName(String firstName) { this.firstName = firstName; }
	void setLastName(String lastName) { this.lastName = lastName; }
	void setTobaccoUser(String tobaccoUser) { this.tobaccoUser = tobaccoUser; }
	void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
	void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
}