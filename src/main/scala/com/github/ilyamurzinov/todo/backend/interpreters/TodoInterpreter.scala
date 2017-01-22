package com.github.ilyamurzinov.todo.backend.interpreters

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.dsl.storage._
import com.github.ilyamurzinov.todo.backend.dsl.logic._

import cats.~>
import cats.free.Free
import io.catbird.util._
import com.twitter.util.Future

/**
  * @author Murzinov Ilya [murz42@gmail.com]
  */
class TodoInterpreter {
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
              _ <- o
                .map(t => StorageAction.saveTodo(f(t)))
                .getOrElse(StorageAction.noAction)
            } yield o.map(f(_))
          case logic.DeleteTodo(id) =>
            for {
              o <- StorageAction.getTodo(id)
              _ <- o
                .map(t => StorageAction.deleteTodo(id))
                .getOrElse(StorageAction.noAction)
            } yield o
          case logic.DeleteAllTodos =>
            for {
              l <- StorageAction.getTodos
              _ <- StorageAction.deleteAllTodos
            } yield l
        }
    }

  val interpreter = (logicInterpreter andThen new InMemoryStorageInterpreter()) or new LoggingInterpreter()

  def interpret[T](action: Free[TodoApp, T]): Future[T] =
    action.foldMap(interpreter)
}
