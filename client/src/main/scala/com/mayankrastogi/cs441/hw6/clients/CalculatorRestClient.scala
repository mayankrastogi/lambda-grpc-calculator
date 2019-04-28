package com.mayankrastogi.cs441.hw6.clients

import com.google.gson.Gson
import com.mayankrastogi.cs441.hw6.protobuf.calculator.{Expression, Response}
import com.typesafe.scalalogging.LazyLogging
import scalaj.http.Http
import scalapb.json4s.JsonFormat
import java.util

/**
  * Client program for invoking the `CalculatorFunctionRest` lambda function on AWS that uses JSON as the data
  * interchange format.
  *
  * @param url URL for the API Gateway that triggers the lambda function.
  */
class CalculatorRestClient(private var url: String) extends CalculatorClient with LazyLogging {

  override def setUrl(url: String): Unit = {
    this.url = url
  }

  override def evaluate(expression: Expression): Double = {
    logger.trace(s"calculate(expression: $expression")

    val expressionJson = JsonFormat.toJsonString(expression)
    val gson = new Gson
    val expressionMap = gson.fromJson(expressionJson, classOf[util.Map[String, Object]])
    expressionMap.putIfAbsent("operation", expression.operation.name)

    // Make POST request to calculator API Gateway
    val request = Http(url)
      .headers(Map(
        "Content-Type" -> "application/json",
        "Accept" -> "application/json"
      ))
      .postData(gson.toJson(expressionMap))

    logger.debug(s"Making HTTP request: $request")
    val response = request.asString
    logger.debug(s"Got response: $response")

    // Parse response from API to protobuf Response object
    val responseMessage = JsonFormat.fromJsonString[Response](response.body)
    logger.debug(s"Response message: $responseMessage")

    // Return the result
    responseMessage.result
  }
}
