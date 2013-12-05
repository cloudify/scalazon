# scalazon

Idiomatic, opinionated Scala library for AWS.

## Status

This is very much a work in progress.

Services covered:

### Kinesis

* Create a stream
* Delete a stream
* List available streams
* List shards of a stream
* Put records
* Get records through a shard iterator

__Note__: for Kinesis you'll need to have the following JARs in your project `lib` directory:

* `KinesisClientLibrary.jar`
* `aws-java-sdk-1.6.4.jar`
* `aws-java-sdk-flow-build-tools-1.6.4.jar`

The above libraries are part of the Kinesis SDK and not publicly available yet.

## Installation

The library is currently hosted on Bintray, so you'll need to have the `sbt-bintray` plugin configured in your project. For instance you can add the following to `project/plugins.sbt`:

```scala
resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")
```

Then in your `build.sbt` add:

```scala
seq(bintrayResolverSettings:_*)

libraryDependencies ++= Seq("io.github.cloudify" %% "scalazon" % "0.3")
```


## Usage

### CredentialsProvider

The `io.github.cloudify.scala.aws.auth.CredentialsProvider` object defines a few helpful instances of `AWSCredentialsProvider`:

* The case class `HomePropertiesFile`: implements a `AWSCredentialsProvider` that reads AWS credentials from a properties file stored in the user's home directory. 
* `DefaultHomePropertiesFile`: an instance of `HomePropertiesFile` that looks for `.aws.properties` in the user home.
* `InstanceProfile`: an instance of `InstanceProfileCredentialsProvider`
* `DefaultClasspathPropertiesFile`: an instance of `ClasspathPropertiesFileCredentialsProvider` that looks for `AwsCredentials.properties` in the class path.
* `DefaultHyerarchical`: returns the first valid provider of `DefaultHomePropertiesFile`, `InstanceProfile`, `DefaultClasspathPropertiesFile`

Plus an implicit value class that adds an `orElse` method to `AWSCredentialsProvider`, and a `firstOf` method that takes a list of `AWSCredentialsProvider`s and returns the first valid one. 

Example:

```scala

import io.github.cloudify.scala.aws.auth.CredentialsProvider.DefaultHyerarchical

val kinesisClient = Client.fromCredentials(DefaultHyerarchical)

```

### Kinesis

Example:

```scala
  import io.github.cloudify.scala.aws.kinesis.Client
  import io.github.cloudify.scala.aws.kinesis.Client.ImplicitExecution._
  import io.github.cloudify.scala.aws.kinesis.KinesisDsl._
  import io.github.cloudify.scala.aws.auth.CredentialsProvider.DefaultHomePropertiesFile
  import java.nio.ByteBuffer
  import scala.concurrent.duration._
  import scala.concurrent.{Future, Await}
  import scala.concurrent.ExecutionContext.Implicits.global

  // Declare an implicit Kinesis `Client` that will be used to make API calls.
  implicit val kinesisClient = Client.fromCredentials(DefaultHomePropertiesFile)

  // First we create the stream.
  val createStream = for {
    s <- Kinesis.streams.create("myStream")
  } yield s

  val s = Await.result(createStream, 60.seconds)
  println("stream created")

  // Stream creation takes some time, we must wait the stream to become active
  // before using it.
  // In this example we're going to wait for up to 60 seconds for the stream
  // to become active.
  val waitActive = Await.result(s.waitActive.retrying(60), 60.seconds)
  println("stream active")

  // Now that the stream is active we can fetch the stream description.
  val description = Await.result(s.describe, 10.seconds)
  println(description.status)
  println(description.isActive)

  // Then we put some data in it.
  // The `put` method expects a ByteBuffer of data and a partition key.
  val putData = for {
    _ <- s.put(ByteBuffer.wrap("hello".getBytes), "k1")
    _ <- s.put(ByteBuffer.wrap("how".getBytes), "k1")
    _ <- s.put(ByteBuffer.wrap("are you?".getBytes), "k2")
  } yield ()
  Await.result(putData, 30.seconds)
  println("data stored")

  // Then we can attempt to fetch the data we just stored.
  // To fetch the data we must iterate through the shards associated to the
  // stream and get records from each shard iterator.
  val getRecords = for {
    shards <- s.shards.list
    iterators <- Future.sequence(shards.map {
      shard =>
        implicitExecute(shard.iterator)
    })
    records <- Future.sequence(iterators.map {
      iterator =>
        implicitExecute(iterator.nextRecords)
    })
  } yield records
  val records = Await.result(getRecords, 30.seconds)
  println("data retrieved")

  // Then we delete the stream.
  val deleteStream = for {
    _ <- s.delete
  } yield ()
  Await.result(deleteStream, 30.seconds)
  println("stream deleted")

```