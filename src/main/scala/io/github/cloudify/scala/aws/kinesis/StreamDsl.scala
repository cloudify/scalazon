package io.github.cloudify.scala.aws.kinesis

import scala.language.implicitConversions
import com.amazonaws.services.kinesis.model
import scala.collection.JavaConverters._
import java.nio.ByteBuffer
import java.lang.Boolean

/**
 * DSL for dealing with Streams
 */
trait StreamsDsl {

  /**
   * Sugar for dealing with the streams collection
   */
  lazy val streams = new {

    /**
     * Provides a StreamReq that lists the available Streams
     */
    def list = Requests.ListStreams()

    /**
     * Creates a new stream and returns the `Stream` definition associated
     * to it.
     *
     * If the call to the Kinesis API fails with a ResourceInUseException
     * due to the fact that the stream already exists, this method will
     * fail silently and just return the `Stream` object.
     */
    def create(name: String) = Requests.CreateStream(stream(name))

  }

  /**
   * Declare a stream by name, exposing the methods available to steams.
   *
   * Example:
   * {{{
   *    stream("myStream").describe
   * }}}
   *
   */
  def stream(name: String) = Definitions.Stream(name)

  /**
   * Implicitly converts a String to a Stream.
   *
   * Example:
   * {{{
   *    "myStream".describe
   * }}}
   *
   */
  implicit def stringToStreamDefinition(name: String) = stream(name)

}

/**
 * Provides methods related to a stream
 */
trait StreamDsl {

  def delete: Requests.DeleteStream

  /**
   * Gives access to the shards associated to a stream
   */
  def shards: Definitions.StreamShards

  /**
   * Makes a request to fetch the description of a stream
   */
  def describe: Requests.TryDescribeStream

  /**
   * Waits until the stream becomes active
   */
  def waitActive: Requests.WaitStreamActive

  /**
   * Puts some data in the stream
   */
  def put(data: ByteBuffer, partitionKey: String): Requests.PutRecord

}

trait StreamDescriptionDsl {
  def name: String
  def arn: String
  def isMoreDataAvailable: Boolean
  def status: String
  def isActive: Boolean
  def isCreating: Boolean
  def isUpdating: Boolean
  def isDeleting: Boolean
}

trait ShardsDsl {
  def list: Requests.ListStreamShards
}

trait ShardDsl {
  def iterator: Requests.ShardIterator
}

trait ShardIteratorDsl {
  def nextRecords: Requests.NextRecords
}

trait NextRecordsDsl {
  def records: Iterable[Definitions.Record]
  def nextIterator: Definitions.ShardIterator
}

trait RecordDsl {
  def sequenceNumber: String
  def data: ByteBuffer
  def partitionKey: String
}

trait CreateStreamDsl[A] {
  def withSize(size: Int): A
}

trait BlockableRequestDsl[T] {
  def blocking: T
}

trait BlockingRequestDsl[A] {
  def retrying(retries: Int): A
  def sleeping(sleep: Long): A
}

trait ShardIteratorRequestDsl[A] {
  def withType(iteratorType: Types.ShardIteratorType): A
}

trait NextRecordsRequestDsl[A] {
  def withLimit(limit: Int): A
}

trait PutRecordDsl[A] {
  def withExclusiveMinimumSequenceNumber(seqNumber: String): A
  def withoutExclusiveMinimumSequenceNumber: A
}

trait PutResultDsl {
  def shardId: String
  def sequenceNumber: String
}

object Definitions {

  /**
   * Encapsulate a stream definition.
   */
  case class Stream(name: String) extends StreamDsl {

    def delete: Requests.DeleteStream = Requests.DeleteStream(this)

    def shards: StreamShards = StreamShards(this)
  
    def describe: Requests.TryDescribeStream = Requests.TryDescribeStream(this)
  
    def waitActive: Requests.WaitStreamActive = Requests.WaitStreamActive(this)

    def put(data: ByteBuffer, partitionKey: String): Requests.PutRecord = Requests.PutRecord(this, data, partitionKey)

  }

  case class StreamDescription(streamDef: Stream, result: model.DescribeStreamResult) extends StreamDescriptionDsl {
    private def description = result.getStreamDescription
    def name: String = description.getStreamName
    def arn: String = description.getStreamARN
    def isMoreDataAvailable: Boolean = description.isMoreDataAvailable
    def status: String = description.getStreamStatus
    def isActive: Boolean = status.equals("ACTIVE")
    def isCreating: Boolean = status.equals("CREATING")
    def isUpdating: Boolean = status.equals("UPDATING")
    def isDeleting: Boolean = status.equals("DELETING")
  }
  
  case class StreamShards(streamDef: Stream) extends ShardsDsl {
    def list: Requests.ListStreamShards = Requests.ListStreamShards(streamDef)
  }
  
  case class Shard(streamDef: Stream, shard: model.Shard) extends ShardDsl {
    def iterator: Requests.ShardIterator = Requests.ShardIterator(this)
  }
  
  case class ShardIterator(name: String, shardDef: Shard) extends ShardIteratorDsl {
    def nextRecords: Requests.NextRecords = Requests.NextRecords(this)
  }
  
  case class NextRecords(iteratorDef: ShardIterator, result: model.GetNextRecordsResult) extends NextRecordsDsl {
    def records: Iterable[Record] = result.getRecords.asScala.map(Record)
    def nextIterator: ShardIterator = iteratorDef.copy(name = result.getNextShardIterator)
  }
  
  case class Record(record: model.Record) extends RecordDsl {
    def sequenceNumber: String = record.getSequenceNumber
    def data: ByteBuffer = record.getData
    def partitionKey: String = record.getPartitionKey
  }

  case class PutResult(result: model.PutRecordResult) extends PutResultDsl {
    def shardId: String = result.getShardId
    def sequenceNumber: String = result.getSequenceNumber
  }

}

object Requests {

  case class CreateStream(streamDef: Definitions.Stream, size: Int = Defaults.StreamSize) extends CreateStreamDsl[CreateStream] {
    def withSize(size: Int): CreateStream = this.copy(size = size)
  }

  case class DeleteStream(streamDef: Definitions.Stream)

  case class TryDescribeStream(streamDef: Definitions.Stream) extends BlockableRequestDsl[DescribeStream] {
    def blocking: DescribeStream = DescribeStream(streamDef)
  }

  case class DescribeStream(streamDef: Definitions.Stream, retries: Int = Defaults.Retries, sleep: Long = Defaults.Sleep) extends BlockingRequestDsl[DescribeStream] {
    def retrying(retries: Int) = this.copy(retries = retries)
    def sleeping(sleep: Long) = this.copy(sleep = sleep)
  }

  case class WaitStreamActive(streamDef: Definitions.Stream, retries: Int = Defaults.Retries, sleep: Long = Defaults.Sleep) extends BlockingRequestDsl[WaitStreamActive] {
    def retrying(retries: Int) = this.copy(retries = retries)
    def sleeping(sleep: Long) = this.copy(sleep = sleep)
  }

  case class ListStreams()

  case class ListStreamShards(streamDef: Definitions.Stream)

  case class PutRecord(streamDef: Definitions.Stream, data: ByteBuffer, partitionKey: String, minSeqNumber: Option[String] = None) extends PutRecordDsl[PutRecord] {
    def withExclusiveMinimumSequenceNumber(seqNumber: String) = this.copy(minSeqNumber = Some(seqNumber))
    def withoutExclusiveMinimumSequenceNumber = this.copy(minSeqNumber = None)
  }

  case class ShardIterator(
    shardDef: Definitions.Shard,
    iteratorType: Types.ShardIteratorType = Types.TrimHorizon
  ) extends ShardIteratorRequestDsl[ShardIterator] {
    def withType(iteratorType: Types.ShardIteratorType) = this.copy(iteratorType = iteratorType)
  }

  case class NextRecords(iteratorDef: Definitions.ShardIterator, limit: Int = Defaults.IteratorLimit) {
    def withLimit(limit: Int) = this.copy(limit = limit)
  }

}

object Types {

  sealed trait ShardIteratorType {
    val value: String
  }

  object AtSequenceNumber extends ShardIteratorType {
    val value = "AT_SEQUENCE_NUMBER"
  }

  object AfterSequenceNumber extends ShardIteratorType {
    val value = "AFTER_SEQUENCE_NUMBER"
  }

  object TrimHorizon extends ShardIteratorType {
    val value = "TRIM_HORIZON"
  }

  object Latest extends ShardIteratorType {
    val value = "LATEST"
  }

}
