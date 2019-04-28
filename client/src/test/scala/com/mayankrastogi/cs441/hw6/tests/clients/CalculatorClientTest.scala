package com.mayankrastogi.cs441.hw6.tests.clients

import com.mayankrastogi.cs441.hw6.clients.{CalculatorGrpcClient, CalculatorRestClient, Settings}
import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSuite, Matchers}

/**
  * Tests both the client implementations of [[com.mayankrastogi.cs441.hw6.clients.CalculatorClient]] using lambda
  * functions via API Gateway.
  *
  * NOTE: Requires the default API Gateway URLs, defined in config, to be up and running for the tests to pass.
  */
class CalculatorClientTest extends FunSuite with CalculatorClientBehaviours with Matchers {

  val settings = new Settings(ConfigFactory.load())

  val grpcClient = new CalculatorGrpcClient(settings.apiGatewayUrlGrpc)
  val restClient = new CalculatorRestClient(settings.apiGatewayUrlRest)

  testsFor(calculatorClient(grpcClient, "gRPC client"))
  testsFor(calculatorClient(restClient, "REST client"))
}
