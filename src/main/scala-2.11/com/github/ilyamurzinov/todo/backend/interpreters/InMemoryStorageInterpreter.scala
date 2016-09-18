package com.github.ilyamurzinov.todo.backend.interpreters

import java.util.UUID

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.dsl.storage._
import com.github.ilyamurzinov.todo.backend.Config

import cats.~>
import com.twitter.bijection.twitter_util._
import com.twitter.util.Future

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * @author Murzinov Ilya [murz42@gmail.com]
  */
object InMemoryStorageInterpreter {
  private[this] val storage: Map[UUID, Todo] = Map()

  val interpreter: StorageAction ~> Future = new (StorageAction ~> Future) {
    override def apply[A](action: StorageAction[A]): Future[A] =
      action match {
        case GetAllTodos => Future.value(storage.values.toList)
        case GetTodo(id: UUID) => Future.value(storage.get(id))
      }
  }
}
