package io.github.cloudify.scala.aws.kinesis

object KinesisDsl
  extends StreamsDsl
  {

  val TrimHorizon = Types.TrimHorizon
  val AfterSequenceNumber = Types.AfterSequenceNumber
  val AtSequenceNumber = Types.AtSequenceNumber
  val Latest = Types.Latest

}
