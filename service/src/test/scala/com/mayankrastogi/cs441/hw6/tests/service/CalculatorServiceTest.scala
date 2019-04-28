package com.mayankrastogi.cs441.hw6.tests.service

import com.mayankrastogi.cs441.hw6.protobuf.calculator.{Expression, Operands, Operations}
import com.mayankrastogi.cs441.hw6.service.CalculatorService
import org.scalatest.{AsyncFunSuite, Matchers}

/**
  * Test Cases for [[com.mayankrastogi.cs441.hw6.service.CalculatorService]] that implements
  * [[com.mayankrastogi.cs441.hw6.protobuf.calculator.CalculatorGrpc.Calculator]] gRPC service
  */
class CalculatorServiceTest extends AsyncFunSuite with Matchers {

  test("evaluate should add two numbers correctly") {
    val expression = Expression(Operations.ADD, Some(Operands(2.5, 3.5)))
    CalculatorService.evaluate(expression).map { response =>
      response.result shouldBe 6.0
    }
  }

  test("evaluate should subtract two numbers correctly") {
    val expression = Expression(Operations.SUBTRACT, Some(Operands(2.5, 3.5)))
    CalculatorService.evaluate(expression).map { response =>
      response.result shouldBe -1.0
    }
  }

  test("evaluate should multiply two numbers correctly") {
    val expression = Expression(Operations.MULTIPLY, Some(Operands(2.5, 3.5)))
    CalculatorService.evaluate(expression).map { response =>
      response.result shouldBe 8.75
    }
  }

  test("evaluate should divide two numbers correctly") {
    val expression = Expression(Operations.DIVIDE, Some(Operands(2.5, 3.5)))
    CalculatorService.evaluate(expression).map { response =>
      response.result shouldBe 0.714 +- 0.001
    }
  }

  test("evaluate should return Infinity when a positive number is divided by 0") {
    val expression = Expression(Operations.DIVIDE, Some(Operands(2.5, 0)))
    CalculatorService.evaluate(expression).map { response =>
      response.result shouldBe Double.PositiveInfinity
    }
  }

  test("evaluate should return -Infinity when a negative number is divided by 0") {
    val expression = Expression(Operations.DIVIDE, Some(Operands(-2.5, 0)))
    CalculatorService.evaluate(expression).map { response =>
      response.result shouldBe Double.NegativeInfinity
    }
  }

  test("evaluate should return Nan when a 0 is divided by 0") {
    val expression = Expression(Operations.DIVIDE, Some(Operands(0, 0)))
    CalculatorService.evaluate(expression).map { response =>
      response.result.isNaN shouldBe true
    }
  }
}
