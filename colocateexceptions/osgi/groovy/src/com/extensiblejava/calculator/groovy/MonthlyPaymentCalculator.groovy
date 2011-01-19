package com.extensiblejava.calculator.groovy

class MonthlyPaymentCalculator {

	def calculatePayment(presentValue, rate, term) {

		def dPresentValue = presentValue.doubleValue()
		def dRate = rate.doubleValue() / 1200
	
		def revisedRate = dRate + 1;
		def dTerm = term.doubleValue()
	
		def powRate = Math.pow(revisedRate, dTerm)
	
		def left = powRate * dPresentValue
		def middle = dRate / (powRate - 1)
		def right = 1/(1);
	
		def payment = new BigDecimal(left * middle * right).setScale(2, BigDecimal.ROUND_HALF_UP)
	}
}

