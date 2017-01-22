package com.github.ilyamurzinov.todo.backend.interpreters

import com.github.ilyamurzinov.todo.backend.dsl.logging._
import cats.~>
import com.twitter.util.Future

/**
  * @author Murzinov Ilya [murz42@gmail.com]
  */
class LoggingInterpreter extends (LogAction ~> Future) {
  override def apply[A](action: LogAction[A]): Future[A] =
    action match {
      case Info(msg: String) =>
        println(msg)
        Future.value(())
    }
}
