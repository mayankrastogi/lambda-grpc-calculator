package com.mayankrastogi.cs441.hw6.tests.clients

import com.mayankrastogi.cs441.hw6.clients.CalculatorClient
import com.mayankrastogi.cs441.hw6.protobuf.calculator.{Expression, Operands, Operations}
import org.scalatest.{FunSuite, Matchers}

/**
  * Defines test cases for testing a client extending the [[com.mayankrastogi.cs441.hw6.clients.CalculatorClient]]
  * trait.
  */
trait CalculatorClientBehaviours {
  this: FunSuite with Matchers =>

  /**
    * Runs tests for [[CalculatorClient]].
    *
    * @param createClient Instance of [[CalculatorClient]].
    * @param clientName   Name of the client to be prefixed in test names.
    */
  def calculatorClient(createClient: => CalculatorClient, clientName: String) {
    val client = createClient

    test(s"$clientName: evaluate should add two numbers correctly") {
      val expression = Expression(Operations.ADD, Some(Operands(2.5, 3.5)))
      val result = client.evaluate(expression)

      result shouldBe 6.0
    }

    test(s"$clientName: evaluate should subtract two numbers correctly") {
      val expression = Expression(Operations.SUBTRACT, Some(Operands(2.5, 3.5)))
      val result = client.evaluate(expression)

      result shouldBe -1.0
    }

    test(s"$clientName: evaluate should multiply two numbers correctly") {
      val expression = Expression(Operations.MULTIPLY, Some(Operands(2.5, 3.5)))
      val result = client.evaluate(expression)

      result shouldBe 8.75
    }

    test(s"$clientName: evaluate should divide two numbers correctly") {
      val expression = Expression(Operations.DIVIDE, Some(Operands(2.5, 3.5)))
      val result = client.evaluate(expression)

      result shouldBe 0.714 +- 0.001
    }

    test(s"$clientName: evaluate should return Infinity when a positive number is divided by 0") {
      val expression = Expression(Operations.DIVIDE, Some(Operands(2.5, 0)))
      val result = client.evaluate(expression)

      result shouldBe Double.PositiveInfinity
    }

    test(s"$clientName: evaluate should return -Infinity when a negative number is divided by 0") {
      val expression = Expression(Operations.DIVIDE, Some(Operands(-2.5, 0)))
      val result = client.evaluate(expression)

      result shouldBe Double.NegativeInfinity
    }

    test(s"$clientName: evaluate should return Nan when a 0 is divided by 0") {
      val expression = Expression(Operations.DIVIDE, Some(Operands(0, 0)))
      val result = client.evaluate(expression)

      result.isNaN shouldBe true
    }
  }
}
