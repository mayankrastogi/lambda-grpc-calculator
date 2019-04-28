package com.mayankrastogi.cs441.hw6.lambda

import java.util.Base64

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.mayankrastogi.cs441.hw6.protobuf.calculator.Expression
import com.mayankrastogi.cs441.hw6.service.CalculatorService

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * AWS Lambda function to evaluates a basic math expression using gRPC service.
  *
  * The input is a proxy request event object which must contain the base-64 encoded string representation of the
  * `Expression` protobuf in the `body` of the event. The API Gateway should be configured to pass binary data in the
  * request body as a base-64 encoded string to the lambda function.
  *
  * The output is a proxy response event object that contains the base-64 encoded string reporesentation of the
  * `Response` protobuf in the body of the event. The API Gateway should be configured to convert the base-64 encoded
  * data in the response event's body to binary data while forwarding the response to the client.
  */
class CalculatorFunctionGrpc extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {

  /**
    * Handler for the AWS Lambda function.
    *
    * @param input   Request proxy event that holds base-64 encoded request body representing the `Expression` protobuf.
    * @param context AWS Lambda context.
    * @return Response proxy event that holds base-64 encoded body representing the `Response` protobuf.
    */
  override def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    // Get AWS Lambda Logger
    val logger = context.getLogger
    logger.log("Request Body:\n" + input.toString)

    // Decode base-64 encoded binary data from the request body
    val message = if (input.getIsBase64Encoded) Base64.getDecoder.decode(input.getBody.getBytes) else input.getBody.getBytes
    logger.log(s"message: (${message.mkString(", ")})")

    // Construct the expression from binary data
    val expression = Expression.parseFrom(message)
    logger.log(s"Expression: $expression")

    // Evaluate the expression using gRPC service
    val result = Await.result(CalculatorService.evaluate(expression), 5 seconds)
    logger.log(s"Result: $result")

    // Base-64 encode the response protobuf
    val output = Base64.getEncoder.encodeToString(result.toByteArray)
    logger.log(s"Output: $output")

    // Send the response
    new APIGatewayProxyResponseEvent()
      .withStatusCode(200)
      .withHeaders(Map("Content-Type" -> "application/grpc+proto").asJava)
      .withIsBase64Encoded(true)
      .withBody(output)
  }
}
