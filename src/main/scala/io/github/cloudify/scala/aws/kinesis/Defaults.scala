package io.github.cloudify.scala.aws.kinesis

object Defaults {

  /**
   * Default shards for a new stream
   */
  val StreamSize = 1

  /**
   * Default number of retries when waiting for changes to happen.
   * (e.g. when blocking while waiting for a new stream to become active)
   */
  val Retries = 3

  /**
   * Default sleep time between retries when waiting for changes to happen.
   * (e.g. when blocking while waiting for a new stream to become active)
   */
  val Sleep = 1000


  /**
   * How many records to fetch for each call to the shard iterator
   */
  val IteratorLimit = 1000

}
