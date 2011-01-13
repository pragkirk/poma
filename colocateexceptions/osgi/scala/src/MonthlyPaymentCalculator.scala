package com.extensiblejava.calculator.scala

import scala.math._

class MonthlyPaymentCalculator {
	def calculatePayment(presentValue:BigDecimal, rate:BigDecimal, term:Int): BigDecimal = {		
		val dPresentValue = presentValue.toDouble
		val dRate = rate.toDouble / 1200
		
		val revisedRate = dRate + 1;
		val dTerm = term.toDouble
		
		val powRate = pow(revisedRate, dTerm)
		
		val left = powRate * dPresentValue
		val middle = dRate / (powRate - 1)
		val right = 1/(1);
		
		val payment = BigDecimal(left * middle * right).setScale(2, BigDecimal.RoundingMode.HALF_UP)
		payment
	}
}