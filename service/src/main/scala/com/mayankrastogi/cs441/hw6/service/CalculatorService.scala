package com.mayankrastogi.cs441.hw6.service

import com.mayankrastogi.cs441.hw6.protobuf.calculator.{CalculatorGrpc, Expression, Operations, Response}

import scala.concurrent.Future

/**
  * Implements the Calculator gRPC service.
  */
object CalculatorService extends CalculatorGrpc.Calculator {

  /**
    * Evaluates the specified `Expression`.
    *
    * @param request The `Expression` to evaluate
    * @return A future where the result of the evaluation will be returned.
    */
  override def evaluate(request: Expression): Future[Response] = {
    // Get the operands
    val operands = request.operands.get

    // Evaluate the result based on the operation
    val result = request.operation match {
      case Operations.ADD => operands.number1 + operands.number2
      case Operations.SUBTRACT => operands.number1 - operands.number2
      case Operations.MULTIPLY => operands.number1 * operands.number2
      case Operations.DIVIDE => operands.number1 / operands.number2
      case Operations.Unrecognized(value) => throw new Exception(s"Unrecognized operation: $value")
    }

    // Send the result as response
    Future.successful(Response(expression = Some(request), result = result))
  }
}
