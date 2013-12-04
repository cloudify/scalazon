import io.github.cloudify.scala.aws.kinesis.Client
import io.github.cloudify.scala.aws.kinesis.Client.ImplicitExecution._
import io.github.cloudify.scala.aws.kinesis.KinesisDsl._
import io.github.cloudify.scala.aws.auth.CredentialsProvider.DefaultHomePropertiesFile
import java.nio.ByteBuffer
import scala.concurrent.duration._
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global

object KinesisExample extends App {

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

}
