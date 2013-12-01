package io.github.cloudify.scala.aws.kinesis

import scala.concurrent.{Promise, ExecutionContext, Future, blocking}
import ExecutionContext.Implicits.global
import scala.util.Success

object Implicits {

  implicit class FutureCompanionOps(val f: Future.type ) extends AnyVal {

    def delay(t: Long): Future[Unit] = Future {
      if(t > 0) blocking {
        Thread.sleep(t)
      }
    }

    def retry[T](noTimes: Int, wait: Long = 0)(block: => Future[T]): Future[T] = {
      val ns: Iterator[Int] = (1 to noTimes).iterator
      val attempts: Iterator[() => Future[T]] = ns.map(_ => () => block) // iterator of calls to block
      val failed: Future[T] = Future.failed(new Exception)

      attempts.foldLeft(failed) { (a, block) =>
        val p = Promise[T]()
        a.onComplete {
          case Success(d) => p.success(d)
          case _ => p.completeWith(delay(wait).flatMap(_ => block()))
        }
        p.future
      }
    }

  }

}
