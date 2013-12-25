package io.github.cloudify.scala.aws.kinesis

import KinesisDsl._
import org.scalatest.{ShouldMatchers, OptionValues, WordSpec}

class StreamDslSpec extends WordSpec with ShouldMatchers with OptionValues {

  "The streams object" should {

    "Generate a request for listing all streams" in {
      Kinesis.streams.list should equal(Requests.ListStreams)
    }

  }

}
