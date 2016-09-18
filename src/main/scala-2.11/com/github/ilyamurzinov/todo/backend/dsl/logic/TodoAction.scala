package com.github.ilyamurzinov.todo.backend.dsl.logic

import com.github.ilyamurzinov.todo.backend.dsl._

import cats.free.Free.liftF
import java.util.UUID

sealed trait TodoAction[T]

case object GetAllTodos extends TodoAction[List[Todo]]
case class GetTodo(id: UUID) extends TodoAction[Option[Todo]]
case class SaveTodo(todo: Todo) extends TodoAction[Todo]

object TodoAction {
  val getAllTodos: TodoF[List[Todo]] = liftF(GetAllTodos)
  def getTodo(id: UUID): TodoF[Option[Todo]] = liftF(GetTodo(id))
  def saveTodo(todo: Todo): TodoF[Todo] = liftF(SaveTodo(todo))
}
