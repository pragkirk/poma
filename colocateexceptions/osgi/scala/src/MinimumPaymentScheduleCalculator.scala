package com.extensiblejava.calculator.scala

import com.extensiblejava.loan._
//import MonthlyPaymentCalculator._

class MinimumPaymentScheduleCalculator(paymentFactory:PaymentFactory) extends LoanCalculator {
	def calculateLoan(value:java.math.BigDecimal, rate:java.math.BigDecimal, term:Int): Loan = {
	
		Console.println("---** IN SCALA CALCULATOR **---")
		val mc = new java.math.MathContext(2, java.math.RoundingMode.HALF_UP)
		val presentValue = BigDecimal(value)
		val presentRate = BigDecimal(rate).apply(mc)
		
		var cumulativePrincipal = BigDecimal("0.00")
		var cumulativeInterest = BigDecimal("0.00")
		
		val paymentSchedule = paymentFactory.createPaymentSchedule()
		val adjustedRate = (presentRate / (BigDecimal("1200")).setScale(2, BigDecimal.RoundingMode.HALF_UP))
		val calculator = new MonthlyPaymentCalculator()
		val monthlyPayment = calculator.calculatePayment(presentValue, presentRate, term)
		
		var loanBalance = BigDecimal(presentValue.bigDecimal)
		
		while (loanBalance.toDouble > monthlyPayment.toDouble) {
			val interest = (loanBalance.*(adjustedRate)).setScale(2, BigDecimal.RoundingMode.HALF_UP)
			val principal = (monthlyPayment.-(interest)).setScale(2, BigDecimal.RoundingMode.HALF_UP);
			val payment = paymentFactory.createPayment(principal.bigDecimal, interest.bigDecimal); 
			paymentSchedule.addPayment(payment);

			cumulativeInterest = cumulativeInterest.+(interest).setScale(2, BigDecimal.RoundingMode.HALF_UP);
			cumulativePrincipal = cumulativePrincipal.+(principal).setScale(2, BigDecimal.RoundingMode.HALF_UP);
			loanBalance = loanBalance.-(principal);
		}
		val interest = (loanBalance.*(adjustedRate)).setScale(2, BigDecimal.RoundingMode.HALF_UP)
		val principal = loanBalance.setScale(2, BigDecimal.RoundingMode.HALF_UP)
		cumulativeInterest = cumulativeInterest.+(interest).setScale(2, BigDecimal.RoundingMode.HALF_UP)
		cumulativePrincipal = cumulativePrincipal.+(principal).setScale(2, BigDecimal.RoundingMode.HALF_UP)
		val payment = paymentFactory.createPayment(principal.bigDecimal, interest.bigDecimal)
		paymentSchedule.addPayment(payment)
		
		val loan = paymentFactory.createLoan(paymentSchedule, cumulativeInterest.bigDecimal, cumulativePrincipal.bigDecimal)
		loan
		
	}
}