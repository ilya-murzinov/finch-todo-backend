package com.github.ilyamurzinov.todo.backend.interpreters

import java.util.UUID

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.dsl.logging._

import cats.{~>, Id}
import com.twitter.bijection.twitter_util._
import com.twitter.util.Future

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * @author Murzinov Ilya [murz42@gmail.com]
  */
object LoggingInterpreter {
  val interpreter: LogAction ~> Future =
    new (LogAction ~> Future) {
      override def apply[A](action: LogAction[A]): Future[A] =
        action match {
          case Info(msg: String) =>
            println(msg)
            Future.value(())
        }
    }
}
