package com.mayankrastogi.cs441.hw6.clients

import com.mayankrastogi.cs441.hw6.protobuf.calculator.Expression

/**
  * Client program for evaluating an expression by making a gRPC call to AWS Lambda function.
  */
trait CalculatorClient {

  /**
    * Sets the URL for the API Gateway that triggers the lambda function.
    *
    * @param url The URL.
    */
  def setUrl(url: String): Unit

  /**
    * Evaluates the given expression by making a gRPC call to AWS Lambda function.
    *
    * @param expression The expression to evaluate.
    * @return The result of evaluating the expression.
    */
  def evaluate(expression: Expression): Double
}
