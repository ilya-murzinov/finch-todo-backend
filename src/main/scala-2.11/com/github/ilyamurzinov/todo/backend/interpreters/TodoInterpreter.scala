package com.github.ilyamurzinov.todo.backend.interpreters

import java.util.UUID

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.dsl.storage._
import com.github.ilyamurzinov.todo.backend.dsl.logic._
import com.github.ilyamurzinov.todo.backend.Config

import cats.~>
import cats.free.Free
import io.catbird.util._
import com.twitter.util.Future

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * @author Murzinov Ilya [murz42@gmail.com]
  */
object TodoInterpreter {
  private[this] val logicInterpreter: TodoAction ~> StorageF =
    new (TodoAction ~> StorageF) {
      override def apply[A](action: TodoAction[A]): StorageF[A] =
        action match {
          case logic.GetAllTodos => StorageAction.getTodos
          case logic.GetTodo(id) => StorageAction.getTodo(id)
          case logic.SaveTodo(todo) =>
            for {
              _ <- StorageAction.saveTodo(todo)
            } yield todo
          case logic.PatchTodo(id, f) =>
            for {
              o <- StorageAction.getTodo(id)
              _ <- StorageAction.saveTodo(f(o.getOrElse(null)))
            } yield Some(f(o.getOrElse(null)))
          case logic.DeleteTodo(id) =>
            for {
              t <- StorageAction.getTodo(id)
              _ <- StorageAction.deleteTodo(id)
            } yield t
          case logic.DeleteAllTodos =>
            for {
              l <- StorageAction.getTodos
              _ <- StorageAction.deleteAllTodos
            } yield l
        }
    }

  val interpreter = (logicInterpreter andThen InMemoryStorageInterpreter.interpreter) or LoggingInterpreter.interpreter

  def interpret[T](action: Free[TodoApp, T]): Future[T] =
    action.foldMap(interpreter)
}
