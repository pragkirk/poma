package com.extensiblejava.calculator.groovy

import com.extensiblejava.loan.*

class MinimumPaymentScheduleCalculator implements LoanCalculator {
	def paymentFactory

	MinimumPaymentScheduleCalculator(pFactory) {
		paymentFactory = pFactory
	}
	
	def Loan calculateLoan(BigDecimal presentValue, BigDecimal rate, int term) {
	
		println("---** IN GROOVY CALCULATOR **---")

		def cumulativePrincipal = new BigDecimal("0.00")
		def cumulativeInterest = new BigDecimal("0.00")
		
		def paymentSchedule = paymentFactory.createPaymentSchedule()
		def adjustedRate = (rate / (new BigDecimal("1200")).setScale(2, BigDecimal.ROUND_HALF_UP))
		def calculator = new MonthlyPaymentCalculator()
		def monthlyPayment = calculator.calculatePayment(presentValue, rate, term)
		
		def loanBalance = new BigDecimal(presentValue)
		
		while (loanBalance.doubleValue() > monthlyPayment.doubleValue()) {
			def interest = loanBalance.multiply(adjustedRate).setScale(2, BigDecimal.ROUND_HALF_UP)
			def principal = monthlyPayment.subtract(interest).setScale(2, BigDecimal.ROUND_HALF_UP)
			def payment = paymentFactory.createPayment(principal, interest)
			paymentSchedule.addPayment(payment)

			cumulativeInterest = cumulativeInterest.add(interest).setScale(2, BigDecimal.ROUND_HALF_UP)
			cumulativePrincipal = cumulativePrincipal.add(principal).setScale(2, BigDecimal.ROUND_HALF_UP)
			loanBalance = loanBalance.subtract(principal)
		}
		def interest = loanBalance.multiply(adjustedRate).setScale(2, BigDecimal.ROUND_HALF_UP)
		def principal = loanBalance.setScale(2, BigDecimal.ROUND_HALF_UP)
		cumulativeInterest = cumulativeInterest.add(interest).setScale(2, BigDecimal.ROUND_HALF_UP)
		cumulativePrincipal = cumulativePrincipal.add(principal).setScale(2, BigDecimal.ROUND_HALF_UP)
		def payment = paymentFactory.createPayment(principal, interest)
		paymentSchedule.addPayment(payment)
		
		def loan = paymentFactory.createLoan(paymentSchedule, cumulativeInterest, cumulativePrincipal)
		loan
		
	}
}