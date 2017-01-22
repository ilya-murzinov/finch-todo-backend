package com.github.ilyamurzinov.todo.backend

import java.util.UUID

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.dsl.logic._
import com.github.ilyamurzinov.todo.backend.dsl.logging._

import cats.Show

import io.circe.generic.auto._

class TodoService {
  val s: Show[Todo] = implicitly
  val L: LogI[TodoApp] = implicitly
  val T: TodoI[TodoApp] = implicitly
  import L._
  import T._

  val getAllTodos: TodoAppF[List[Todo]] =
    for {
      _ <- infoI("Getting all Todos")
      list <- getAllTodosI
    } yield list

  def getTodo(id: UUID): TodoAppF[Option[Todo]] =
    for {
      _ <- infoI(s"Getting Todo by id '$id'")
      t <- getTodoI(id)
    } yield t

  def saveTodo(todo: Todo): TodoAppF[Todo] =
    for {
      _ <- infoI(s"Saving new Todo: ${s.show(todo)}")
      t <- saveTodoI(todo)
    } yield t

  def updateTodo(id: UUID, pt: Todo => Todo): TodoAppF[Option[Todo]] =
    for {
      _ <- infoI(s"Patching Todo with id $id")
      o <- patchTodoI(id, pt)
      _ <- infoI(
        o.map { t =>
            s"Patched Todo: ${s.show(t)}"
          }
          .getOrElse(s"Not patched Todo with id $id")
      )
    } yield o

  def deleteTodo(id: UUID): TodoAppF[Option[Todo]] =
    for {
      _ <- infoI(s"Deleting Todo with id $id")
      t <- deleteTodoI(id)
    } yield t

  val deleteAllTodos: TodoAppF[List[Todo]] =
    for {
      _ <- infoI("Deleting all Todos")
      list <- deleteAllTodosI
    } yield list
}
