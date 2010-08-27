package com.extensiblejava.bill.data;

import java.math.*;

public class BillDataBean {
	private Integer billId;
	private Integer custId;
	private String name;
	private BigDecimal amount;
	private BigDecimal auditedAmount;
	private BigDecimal paidAmount;

	public BillDataBean(Integer billId, Integer custId, String name, BigDecimal amount,
						BigDecimal auditedAmount, BigDecimal paidAmount) {
		this.billId = billId;
		this.custId = custId;
		this.name = name;
		this.amount = amount;
		this.auditedAmount = auditedAmount;
		this.paidAmount = paidAmount;
	}

	public Integer getBillId() { return this.billId; }
	public String getName() { return this.name; }
	public BigDecimal getAmount() { return this.amount; }
	public BigDecimal getAuditedAmount() { return this.auditedAmount; }
	public BigDecimal getPaidAmount() { return this.paidAmount; }

	public void setName(String name) {this.name = name;}
	public void setAmount(BigDecimal amount) {this.amount = amount;}
	public void setAuditedAmount(BigDecimal auditedAmount) { this.auditedAmount = auditedAmount; }
	public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
}