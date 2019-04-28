package com.mayankrastogi.cs441.hw6.clients

import com.mayankrastogi.cs441.hw6.protobuf.calculator.{Expression, Operands, Operations}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

/**
  * Main Calculator client program that takes inputs from the user and evaluates the result by invoking AWS Lambda
  * functions.
  *
  * The program accepts the below two arguments in that order:
  * (0) api-type:         Either "grpc" or "rest" to specify which type of API to use for making gRPC calls.
  * (1) api-gateway-url:  The URL of the API Gateway that invokes the lambda function for the specified `api-type`.
  *
  * If these arguments are not specified, default values will be picked up from the typesafe config files.
  */
object Calculator extends App {

  // Load application settings
  val settings = new Settings(ConfigFactory.load())

  // Initialize API-type from the specified arguments or from the settings
  val apiType =
    if (args.nonEmpty) {
      if (Seq("grpc", "rest").contains(args(0).toLowerCase)) {
        args(0)
      }
      else {
        println("Invalid API type. Must be one of ('grpc','rest')")
        sys.exit(-1)
      }
    }
    else {
      settings.defaultAPIType
    }

  // Initialize API Gateway URL from the specified arguments or from the settings
  val url =
    if (args.length > 1)
      args(1)
    else if (apiType.equalsIgnoreCase("grpc"))
      settings.apiGatewayUrlGrpc
    else
      settings.apiGatewayUrlRest

  // Instantiate CalculatorClient based on the apiType
  val client =
    if (apiType.equalsIgnoreCase("grpc"))
      new CalculatorGrpcClient(url)
    else
      new CalculatorRestClient(url)

  // Print menu and wait for user input
  while (true) {
    println(
      s"""
         |=============================================================================================================
         |Calculator ${apiType.toUpperCase} client
         |=============================================================================================================
         |
        |Choose operation:
         |
        |1 - Add
         |2 - Subtract
         |3 - Multiply
         |4 - Divide
         |0 - Quit
      """.stripMargin)

    // Let the user select an option
    val operation = StdIn.readInt()

    if (operation == 0) {
      sys.exit(0)
    }
    else if (operation > 4) {
      println("Invalid option. Enter an option between (0-4).")
    }
    else {
      // Read the two numbers
      print("Enter first number: ")
      val number1 = StdIn.readDouble()
      print("Enter second number: ")
      val number2 = StdIn.readDouble()

      // Make an Expression protobuf and evaluate the result
      val result = client.evaluate(
        Expression(
          Operations.fromValue(operation - 1),
          Some(Operands(
            number1 = number1,
            number2 = number2
          ))
        )
      )
      println(s"\nResult = $result")
    }
  }
}
