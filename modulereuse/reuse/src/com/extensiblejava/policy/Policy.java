package com.extensiblejava.policy;

import java.util.*;

public class Policy {
	private String firstName;
	private String lastName;
	private String tobaccoUser;
	private Date dateOfBirth;
	private String maritalStatus;

	public static Policy buildPolicy(PolicyBuilder policyBuilder) {
		return policyBuilder.build();
	}

	public Policy(String firstName, String lastName, String tobaccoUser, Date dateOfBirth, String maritalStatus) {
		this.firstName = firstName;
		this.lastName= lastName;
		this.tobaccoUser = tobaccoUser;
		this.dateOfBirth = dateOfBirth;
		this.maritalStatus = maritalStatus;
	}

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