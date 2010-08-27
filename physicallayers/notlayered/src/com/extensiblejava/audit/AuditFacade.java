package com.extensiblejava.audit;

import com.extensiblejava.bill.*;
import java.math.*;

public class AuditFacade {
	public BigDecimal audit(Bill bill) {
		BigDecimal amount = bill.getAmount();
		BigDecimal auditedAmount = amount.multiply(new BigDecimal("0.75"));
		return auditedAmount.setScale(2);
	}
}