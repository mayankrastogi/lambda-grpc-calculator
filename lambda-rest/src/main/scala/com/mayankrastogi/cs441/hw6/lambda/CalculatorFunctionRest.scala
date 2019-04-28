package com.mayankrastogi.cs441.hw6.lambda

import java.util

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.google.gson.Gson
import com.mayankrastogi.cs441.hw6.protobuf.calculator.Expression
import com.mayankrastogi.cs441.hw6.service.CalculatorService
import scalapb.json4s.JsonFormat

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * AWS Lambda function to evaluates a basic math expression using gRPC service.
  *
  * The input is a Java Map unmarshalled from a JSON representation of the protobuf `Expression`. The unmarshalling is
  * done by AWS Lambda before invoking the handler.
  *
  * The output is a Java Map marshalled from a JSON representation of the protobuf `Response`. The marshalling is done
  * by AWS Lambda after receiving the response from the lambda function.
  */
class CalculatorFunctionRest extends RequestHandler[util.Map[String, Object], util.Map[String, Object]] {

  /**
    * Handler for the AWS Lambda function.
    *
    * @param input   Java nested Map representing parameters and values in the protobuf `Expression`.
    * @param context AWS Lambda context.
    * @return Java nested Map representing parameters and values in the protobuf `Response`.
    */
  override def handleRequest(input: util.Map[String, Object], context: Context): util.Map[String, Object] = {

    // Get AWS Lambda Logger
    val logger = context.getLogger
    logger.log("Request Body:\n" + input.asScala)

    // Create instance of Gson for (de)serializing Java Map to JSON string
    val gson = new Gson

    // Convert Java Map to JSON string and use ScalaPB to construct the `Expression`
    val expression = JsonFormat.fromJsonString[Expression](gson.toJson(input))
    logger.log(s"expression: $expression")

    // Evaluate the expression using gRPC service
    val result = Await.result(CalculatorService.evaluate(expression), 5 seconds)
    logger.log(s"Result: $result")

    // Use ScalaPB to convert `Response` protobuf to JSON and then convert it to Java Map using Gson
    val response = gson.fromJson(JsonFormat.toJsonString(result), classOf[util.Map[String, Object]])

    // Add the operation key to expression since `JsonFormat.toJsonString` skips enums for some reason
    response.get("expression").asInstanceOf[util.Map[String, Object]].put("operation", expression.operation.name)
    // Zero values are also getting removed, so put result as 0 if the key is missing
    response.putIfAbsent("result", 0.0.asInstanceOf[Object])

    // Send the response
    response
  }
}
