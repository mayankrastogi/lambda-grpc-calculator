## CS 441 - Engineering Distributed Objects for Cloud Computing
## Homework 6 - Lambda gRPC Calculator

---

### Overview

The objective of this homework was to create a gRPC service for performing basic arithmetic operations and deploying it on AWS Lambda.


### API Gateway URLs

The API is deployed using AWS API Gateway at [https://t2s54aygxk.execute-api.us-east-2.amazonaws.com/prod/calculator](https://t2s54aygxk.execute-api.us-east-2.amazonaws.com/prod). It contains two resources:

1. [grpc](https://t2s54aygxk.execute-api.us-east-2.amazonaws.com/prod/calculator/grpc) - For performing calculations using the gRPC client via `POST` method
2. [rest](https://t2s54aygxk.execute-api.us-east-2.amazonaws.com/prod/calculator/rest) - For performing calculations using the REST client via `POST` method

The REST API can also be used via a REST client, such as [Postman](https://www.getpostman.com/).

**Sample Payload**

```json
{
  "operation": "ADD",
  "operands": {
    "number1": 4.5,
    "number2": 2.5
  }
}
```

**Sample Request**

```
curl -X POST \
  https://t2s54aygxk.execute-api.us-east-2.amazonaws.com/prod/calculator/rest \
  -H 'Accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{"operation":"ADD","operands":{"number1": 4.5,"number2": 2.5}}'
```

**Sample Response**

```json
{
    "expression": {
        "operation": "ADD",
        "operands": {
            "number1": 4.5,
            "number2": 2.5
        }
    },
    "result": 7.0
}
```

### Prerequisites to build and run the project

- [SBT](https://www.scala-sbt.org/) installed on your system
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html) installed and configured on your system

### Project Structure

This project uses SBT multi-project build system and consists of the following sub-projects:

1. **root:** The top-level project that aggregates all the other projects but does not contain any source files
2. **protobuflib:** Contains the `calculator.proto` file which defines the calculator gRPC service 
3. **service:** Holds the implementation of the calculator gRPC service. *Depends on `protobuflib`*
4. **lambdaGrpc:** Project for AWS Lambda Function that uses Protobuf as the data-interchange format. *Depends on `service`*
5. **lambdaRest:** Project for AWS Lambda Function that uses JSON as the data-interchange format. *Depends on `service`*
6. **client:** Contains client programs for invoking AWS Lambda functions using gRPC, and also the **main** client program. *Depends on `protobuflib`*

#### The `protobuflib` project

This project contains the `calculator.proto` file which defines the `Calculator` gRPC service, like so:

```proto
syntax = "proto3";

service Calculator {
    rpc Evaluate(Expression) returns (Response);
}

enum Operations {
    ADD = 0;
    SUBTRACT = 1;
    MULTIPLY = 2;
    DIVIDE = 3;
}

message Operands {
    double number1 = 1;
    double number2 = 2;
}

message Expression {
    Operations operation = 1;
    Operands operands = 2;
}

message Response {
    Expression expression = 1;
    double result = 2;
}
```

The project uses [ScalaPB](https://scalapb.github.io/) to generate the stubs for the `Calculator` service and the related protobuf messages. These stubs are generated automatically when the this project or any of the dependent projects are compiled using `sbt <project-name>/compile`.

#### The `service` project

This project depends on the `protobuflib` project to provide the protobuf and gRPC service stubs. It contains the `CalculatorService` scala object which implements the `CalculatorGrpc.Calculator` gRPC service.

#### The `lambdaGrpc` project

This project contains the AWS Lambda function that responds to gRPC calls. It depends on the `service` project for providing the implementation of the calculator gRPC service.

The input to the lambda function is an `APIGatewayProxyRequestEvent` object which must contain the **base-64** encoded string representation of the `Expression` protobuf in the `body` of the event. *The API Gateway must be configured to pass binary data in the request body as a base-64 encoded string to the lambda function* by configuring the **Binary Media Types** settings of the API to `application/grpc+proto`. The client should send `Content-Type: application/grpc+proto` header to tell the API Gateway to encode the binary *body* of the request to base-64 encoded string before passing it on to the lambda function.

The output from the lambda function is an `APIGatewayProxyResponseEvent` object. It will contain the **base-64** encoded string reporesentation of the `Response` protobuf in the body of the event. The API Gateway must be configured to convert the base-64 encoded data in the response event's body to binary data while forwarding the response to the client. This is done by setting the **Content Handling** property of the **Integration Response** to `CONVERT_TO_BINARY`. Additionally, the client should send `Accept: application/grpc+proto` header to tell the API Gateway that it expects a response which is of binary type (as configured in *Binary Media Types*).

The handler itself extracts the base-64 encoded request body from the proxy request event, decodes it to a byte-array and constructs the `Expression` object from it. This object is passed to the `CalculatorService` to evaluate the result. The `Response` object is then serialized to a byte-array, which is then base-64 encoded and passed into the body of the response proxy event object while setting the `isBase64Encoded` flag at the same time.

For deploying this function to AWS Lambda, we just need to issue the command `sbt lambdaGrpc/assembly` to package it into a fat jar and upload it on AWS Lambda, selecting **Java 8** as the **runtime** and `com.mayankrastogi.cs441.hw6.lambda.CalculatorFunctionGrpc::handleRequest` as the **Handler**. 

#### The `lambdaRest` project

This project contains the AWS Lambda function that responds to REST calls. It depends on the `service` project for providing the implementation of the calculator gRPC service.

The input to the lambda function is a `java.util.Map<String, Object>` object which is deserialized from a JSON representation of the `Expression` protobuf. The deserialization is done by AWS Lambda before invoking the handler.

The output is a `java.util.Map<String, Object>` object that mimics keys and values of the `Response` protobuf. AWS lambda serializes it into JSON after receiving the response from the handler.

The handler converts the input map object back to a JSON string using `Gson` and constructs the `Expression` object from it using ScalaPB's `JsonFormat`. The expression is then passed to the `CalculatorService` to evaluate the result. The `Response` object is then serialized to a JSON string using `JsonFormat`, which is then converted to a `java.util.Map<String, Object>` object using `Gson`. The `operation` key is added to this map since `JsonFormat` skips serializing `enum`s. Additionally, `JsonFormat` skips adding the `result` key if it is `0`, so we add it back to the map if it's missing.

For deploying this function to AWS Lambda, we just need to issue the command `sbt lambdaRest/assembly` to package it into a fat jar and upload it on AWS Lambda, selecting **Java 8** as the **runtime** and `com.mayankrastogi.cs441.hw6.lambda.CalculatorFunctionRest::handleRequest` as the **Handler**. 

#### The `client` project

This project contains a scala trait `CalculatorClient` which defines the contract for writing a client for invoking the Lambda functions via API Gateway. `CalculatorGrpcClient` and `CalculatorRestClient` are two implementations of this trait that invoke `CalculatorFunctionGrpc` and `CalculatorFunctionRest` respectively, once they are deployed on AWS using API Gateway.

The `Calculator` scala object is a Scala `App` that provides a user interface for performing calculations using either of the two clients. It takes in two parameters:

1. **API Type:** Can be either `grpc` or `rest` to specify which client to use
2. **API Gateway URL:** The URL of the API Gateway that invokes the lambda function for the specified API Type.

If these arguments are not specified, default values will be picked up from the typesafe config file `reference.conf`.

**Example usage**

```
sbt "client/run grpc"
```

**Example Output**

```text

=============================================================================================================
Calculator GRPC client
=============================================================================================================

Choose operation:

1 - Add
2 - Subtract
3 - Multiply
4 - Divide
0 - Quit
      
1
Enter first number: 3
Enter second number: 2

Result = 5.0

=============================================================================================================
Calculator GRPC client
=============================================================================================================

Choose operation:

1 - Add
2 - Subtract
3 - Multiply
4 - Divide
0 - Quit
      
0

Process finished with exit code 0
```

### Deploying the serverless functions on AWS Lambda

Follow the below instructions to deploy the lambda functions on AWS. The instructions below are for the **gRPC** lambda function. For deploying the **REST** lambda function, replace `Grpc` with `Rest` in the commands/names.

1. Create a fat jar of the function using `sbt assembly`

    ```
    sbt lambdaGrpc/assembly
    ```

2. Log in to your [AWS Console](https://aws.amazon.com)
3. From **Services**, search for **Lambda** and select it
4. Select **Create function**
5. In the next screen, select **Author from scratch**, and under the basic information section, specify the following and click **Create function**:
    - Function name: `CalculatorGrpc`
    - Runtime: `Java 8`
6. Under the **Function code** section, specify the following and click on **Save**:
    - Code entry type: `Upload a .zip or .jar file`
    - Runtime: `Java 8`
    - Handler: `com.mayankrastogi.cs441.hw6.lambda.CalculatorFunctionGrpc::handleRequest`
    - Function package: Click on **Upload** and browse to `<project-dir>/lambda-grpc/target/scala-2.12/lambdaGrpc-assembly-0.1.0-SNAPSHOT.jar`

The lambda function is now deployed on AWS. 

### Exposing the API for accessing lambda functions using AWS API Gateway

1. Ensure that AWS CLI is installed and configured on your system. Follow this [guide](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html) to know how to do so
2. Ensure that the user configured with your AWS CLI is having the proper IAM roles and permissions configured for modifying AWS API Gateway
3. Log in to your [AWS Console](https://aws.amazon.com)
4. From **Services**, search for **API Gateway** and select it
5. Click on **Create API**
6. In the next screen, choose the following options and click on **Create API**:
    - Protocol: `REST`
    - API Name: `Calculator`
7. From the **Actions** dropdown, select **Create Resource**, set **Resource Name** and **Resource Path** to `calculator`, and click **Create Resource** button
8. From the **Actions** dropdown, select **Create Resource**, set **Resource Name** and **Resource Path** to `grpc`, and click **Create Resource** button
9. From the **Actions** dropdown, select **Create Method**, set the method as `POST`, modify the following options, and click **Save**
    - Integration Type: `Lambda Function`
    - Use Lambda Proxy Integration: Checked
    - Lambda Function: `CalculatorGrpc` 
10. From the **Actions** dropdown, select **Create Resource**, set **Resource Name** and **Resource Path** to `rest`, and click **Create Resource** button
11. From the **Actions** dropdown, select **Create Method**, set the method as `POST`, modify the following options, and click **Save**
    - Integration Type: `Lambda Function`
    - Use Lambda Proxy Integration: Leave unchecked
    - Lambda Function: `CalculatorRest`
12. From the left sidebar, under the API `Calculator`, go to **Settings**
13. Under the **Binary Media Types** section, add `application/grpc+proto` as a binary media type and click **Save Changes**
14. Go back to **Resources** from the left sidebar and select `/grpc`
15. Make a note of the IDs displayed in the *grey* breadcrumbs bar - The one within parentheses beside **Calculator** is the **REST API ID** and the one within parentheses beside **/calculator/grpc** is the **Resource ID**
16. Open command prompt (if on Windows) or terminal (if on Mac/Linux)
17. Issue the following command to tell API Gateway that it should convert the base-64 encoded response body of the `CalculatorGrpc` lambda function to binary before forwarding the response to the client. Replace `<rest-api-id>` and `<resource-id>` with the IDs noted in step 15 

    ```
    aws apigateway update-integration-response --rest-api-id <rest-api-id> --resource-id <resource-id> --http-method POST --status-code 200 --patch-operations '[{"op" : "replace", "path" : "/contentHandling", "value" : "CONVERT_TO_BINARY"}]'
    ```

18. In your browser, from the **Actions** dropdown, select **Deploy API**
19. Choose **Deployment stage** as `[New Stage]` and **Stage Name** as `prod` and click on **Deploy** button
20. Your API is now deployed at the URL mentioned in **prod Stage Editor** page. 

You can now invoke the APIs using the `Calculator` client program like so:

**For gRPC client**

```
sbt "client/run grpc <your-api-gateway-url>/calculator/grpc" 
```

**For REST client**

```
sbt "client/run rest <your-api-gateway-url>/calculator/rest" 
```
