package com.github.ilyamurzinov.todo.backend.interpreters

import java.util.UUID

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.dsl.storage._
import com.github.ilyamurzinov.todo.backend.dsl.logic._
import com.github.ilyamurzinov.todo.backend.Config

import cats.~>
import com.twitter.bijection.twitter_util._
import com.twitter.util.Future

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * @author Murzinov Ilya [murz42@gmail.com]
  */
object TodoInterpreter {
  private[this] val logicInterpreter: TodoAction ~> StorageAction =
    new (TodoAction ~> StorageAction) {
      override def apply[A](action: TodoAction[A]): StorageAction[A] =
        action match {
          case logic.GetAllTodos => storage.GetAllTodos
          case logic.GetTodo(id: UUID) => storage.GetTodo(id)
        }
    }

  val interpreter: TodoAction ~> Future = logicInterpreter andThen InMemoryStorageInterpreter.interpreter
}
