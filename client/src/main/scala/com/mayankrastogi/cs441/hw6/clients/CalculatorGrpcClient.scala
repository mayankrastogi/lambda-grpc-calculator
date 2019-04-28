package com.mayankrastogi.cs441.hw6.clients

import com.mayankrastogi.cs441.hw6.protobuf.calculator.{Expression, Response}
import com.typesafe.scalalogging.LazyLogging
import scalaj.http.Http

/**
  * Client program for invoking the `CalculatorFunctionGrpc` lambda function on AWS that uses Protobuf as the data
  * interchange format.
  *
  * @param url URL for the API Gateway that triggers the lambda function.
  */
class CalculatorGrpcClient(private var url: String) extends CalculatorClient with LazyLogging {

  override def setUrl(url: String): Unit = {
    this.url = url
  }

  override def evaluate(expression: Expression): Double = {
    logger.trace(s"calculate(expression: $expression")

    // Make POST request to calculator API Gateway
    val request = Http(url)
      .headers(Map(
        "Content-Type" -> "application/grpc+proto",
        "Accept" -> "application/grpc+proto"
      ))
      .postData(expression.toByteArray)

    logger.debug(s"Making HTTP request: $request")
    val response = request.asBytes
    logger.debug(s"Got response: $response")

    // Parse response from API to protobuf Response object
    val responseMessage = Response.parseFrom(response.body)
    logger.debug(s"Response message: $responseMessage")

    // Return the result
    responseMessage.result
  }
}
