package com.mayankrastogi.cs441.hw6.clients

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

/**
  * Maps and holds configuration read from config files for easy access.
  *
  * @param config The { @link Config} object that holds parameters read from a config file
  */
class Settings(config: Config) {

  // Validate provided config file against the reference (default) config file.
  // Exceptions are thrown if validation fails
  config.checkValid(ConfigFactory.defaultReference(), Settings.CONFIG_NAMESPACE)

  // Application settings

  val apiGatewayUrlGrpc: String = getString("api-gateway-url.grpc")
  val apiGatewayUrlRest: String = getString("api-gateway-url.rest")
  val defaultAPIType: String = getString("default-api-type")

  // ===================================================================================================================
  // Private Helpers
  // ===================================================================================================================

  private def getBoolean(path: String): Boolean = config.getBoolean(configPath(path))

  private def getDouble(path: String): Double = config.getDouble(configPath(path))

  private def getInt(path: String): Int = config.getInt(configPath(path))

  private def getIntList(path: String): List[Int] = config.getIntList(configPath(path)).asScala.toList.asInstanceOf[List[Int]]

  private def getLong(path: String): Long = config.getLong(configPath(path))

  /** Prefixes root key to the specified path to avoid typing it each time a parameter is fetched. */
  private def configPath(path: String): String = s"${Settings.CONFIG_NAMESPACE}.$path"

  private def getString(path: String): String = config.getString(configPath(path))

  private def getStringList(path: String): List[String] = config.getStringList(configPath(path)).asScala.toList
}

/** Companion object to define "static" members for Settings class */
object Settings {

  /** Root key for configuration */
  val CONFIG_NAMESPACE: String = "calculator"

}