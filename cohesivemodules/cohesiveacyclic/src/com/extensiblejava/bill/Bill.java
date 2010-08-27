package com.extensiblejava.bill;

import java.math.*;
import com.extensiblejava.route.*;

public class Bill implements Routable {
	public static final String HIGH = "01";
	public static final String LOW = "02";

	private String type;
	private String location;
	private BigDecimal amount;
	private Router router;

	public Bill(String type, String location, BigDecimal amount, Router router) {
		this.type = type;
		this.location = location;
		this.amount = amount;
		this.router = router;
	}

	public String getType() {return this.type;}
	public String getLocation() {return this.location;}
	public BigDecimal getAmount() {return this.amount;}
	public String getPriority() {
		if (this.amount.compareTo(new BigDecimal("1000.00")) == -1) {
			return LOW;
		} else {
			return HIGH;
		}
	}
	public String route() { return this.router.route(this); }

	/*public Integer getRouteId() { return new Integer(0);}
	public String getRouteType() { return this.type; }*/


}