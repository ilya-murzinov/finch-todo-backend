package com.github.ilyamurzinov.todo.backend.interpreters

import java.util.UUID

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.dsl.storage._
import com.github.ilyamurzinov.todo.backend.Config

import cats.~>
import io.catbird.util._
import com.twitter.bijection.twitter_util._
import com.twitter.util.Future

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * @author Murzinov Ilya [murz42@gmail.com]
  */
object InMemoryStorageInterpreter {
  private[this] var storage: Map[UUID, Todo] = Map()

  private[this] val storageInterpreter: StorageAction ~> Future =
    new (StorageAction ~> Future) {
      override def apply[A](action: StorageAction[A]): Future[A] =
        action match {
          case GetAllTodos => Future.value(storage.values.toList)
          case GetTodo(id) => Future.value(storage.get(id))
          case SaveTodo(todo: Todo) => {
            storage + (todo.id -> todo)
            Future.value(())
          }
          case DeleteTodo(id) => {
            storage - id
            Future.value(())
          }
          case DeleteAllTodos => {
            storage = Map()
            Future.value(())
          }
        }
    }

  val interpreter: StorageF ~> Future = new (StorageF ~> Future) {
    override def apply[A](action: StorageF[A]): Future[A] =
      action.foldMap(storageInterpreter)
  }
}
