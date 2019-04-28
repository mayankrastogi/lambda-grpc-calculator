name := "mayank_k_rastogi_hw6"

version := "0.1"

scalaVersion := "2.12.8"


// ---------------------------------------------------------------------------------------------------------------------
// Dependency definitions
// ---------------------------------------------------------------------------------------------------------------------

// Merge strategy to avoid deduplicate errors
lazy val assemblySettings = 
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
  }

// Typesafe Configuration Library
lazy val typesafeConfig = "com.typesafe" % "config" % "1.3.2"

// Logback logging framework
lazy val logback = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.gnieh" % "logback-config" % "0.3.1"
)

// Scalatest testing framework
lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"

// ScalaPB gRPC runtime
lazy val grpcRuntime = "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion

// ScalaPB JSON to Protobuf convertor
lazy val json4s = "com.thesamet.scalapb" %% "scalapb-json4s" % "0.7.0"

// AWS Lambda SDK
lazy val awsCore = "com.amazonaws" % "aws-lambda-java-core" % "1.2.0"
lazy val awsEvents = "com.amazonaws" % "aws-lambda-java-events" % "2.2.6"

// ScalaJ HTTP Library
lazy val scalaJHTTP = "org.scalaj" %% "scalaj-http" % "2.4.1"


// ---------------------------------------------------------------------------------------------------------------------
// Project definitions
// ---------------------------------------------------------------------------------------------------------------------

// The root project
lazy val root = (project in file("."))
  .aggregate(protobuflib, service, lambdaGrpc, lambdaRest, client)

// Project that contains the *.proto files
lazy val protobuflib = (project in file("protobuflib"))
  .settings(
    // ScalaPB configuration
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(grpcRuntime)
  )

// Project that implements the Calculator gRPC service
lazy val service = (project in file("service"))
  .settings(
    libraryDependencies += scalatest
  )
  .dependsOn(protobuflib)

// Project containing client programs for invoking AWS Lambda functions using gRPC, and also the "main" client program
lazy val client = (project in file("client"))
  .settings(
    libraryDependencies ++= Seq(
      typesafeConfig,
      json4s,
      scalaJHTTP,
      scalatest,
    ) ++ logback
  )
  .dependsOn(protobuflib)

// Project for AWS Lambda Function that uses JSON as the data-interchange format
lazy val lambdaRest = (project in file("lambda-rest"))
  .settings(
    assemblySettings,
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
    libraryDependencies ++= Seq(
      awsCore,
      json4s
    )
  )
  .dependsOn(service)

// Project for AWS Lambda Function that uses Protobuf as the data-interchange format
lazy val lambdaGrpc = (project in file("lambda-grpc"))
  .settings(
    assemblySettings,
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
    libraryDependencies ++= Seq(
      awsCore,
      awsEvents
    )
  )
  .dependsOn(service)
