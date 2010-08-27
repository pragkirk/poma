package com.extensiblejava.bill;

import java.math.*;
import com.extensiblejava.route.*;

public class Bill {
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
	public String route() { return this.router.route(this); }
}