package todo.backend.free.interpreters

import todo.backend.free.dsl.logging._
import cats.~>
import com.twitter.util.Future

class LoggingInterpreter extends (LogAction ~> Future) {
  override def apply[A](action: LogAction[A]): Future[A] =
    action match {
      case Info(msg: String) =>
        println(msg)
        Future.value(())
    }
}
