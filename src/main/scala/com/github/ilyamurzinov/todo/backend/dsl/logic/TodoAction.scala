package com.github.ilyamurzinov.todo.backend.dsl.logic

import com.github.ilyamurzinov.todo.backend.dsl._

import cats.free.{Free, Inject}
import java.util.UUID

sealed trait TodoAction[T]

case object GetAllTodos extends TodoAction[List[Todo]]
case class GetTodo(id: UUID) extends TodoAction[Option[Todo]]
case class SaveTodo(todo: Todo) extends TodoAction[Todo]
case class PatchTodo(id: UUID, f: Todo => Todo)
    extends TodoAction[Option[Todo]]
case class DeleteTodo(id: UUID) extends TodoAction[Option[Todo]]
case object DeleteAllTodos extends TodoAction[List[Todo]]

class TodoI[F[_]](implicit I: Inject[TodoAction, F]) {
  val getAllTodosI: Free[F, List[Todo]] =
    Free.inject[TodoAction, F](GetAllTodos)

  def getTodoI(id: UUID): Free[F, Option[Todo]] =
    Free.inject[TodoAction, F](GetTodo(id))

  def saveTodoI(todo: Todo): Free[F, Todo] =
    Free.inject[TodoAction, F](SaveTodo(todo))

  def patchTodoI(id: UUID, f: Todo => Todo): Free[F, Option[Todo]] =
    Free.inject[TodoAction, F](PatchTodo(id, f))

  def deleteTodoI(id: UUID): Free[F, Option[Todo]] =
    Free.inject[TodoAction, F](DeleteTodo(id))

  val deleteAllTodosI: Free[F, List[Todo]] =
    Free.inject[TodoAction, F](DeleteAllTodos)
}
