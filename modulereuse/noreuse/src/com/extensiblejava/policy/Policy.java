package com.extensiblejava.policy;

import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;

public class Policy {
	private String firstName;
	private String lastName;
	private String tobaccoUser;
	private Date dateOfBirth;
	private String maritalStatus;

	public Policy(String xmlString) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			InputSource source = new InputSource(new StringBufferInputStream(xmlString));
			parser.parse(source, new PolicyDefaultHandler(this));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void setFirstName(String firstName) { this.firstName = firstName; }
	void setLastName(String lastName) { this.lastName = lastName; }
	void setTobaccoUser(String tobaccoUser) { this.tobaccoUser = tobaccoUser; }
	void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
	void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

	public String getFirstName() { return this.firstName; }
	public Date getDateOfBirth() { return this.dateOfBirth; }
	public String getLastName() { return this.lastName; }
	public String getMaritalStatus() { return this.maritalStatus; }
	public String getTobaccoUser() { return this.tobaccoUser; }

	public void validate() {
		//validate the data.
	}

	public void save() {
		//save the data.
	}
}