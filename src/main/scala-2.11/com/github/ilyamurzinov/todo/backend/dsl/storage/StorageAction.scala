package com.github.ilyamurzinov.todo.backend.dsl.storage

import java.util.UUID

import com.github.ilyamurzinov.todo.backend.dsl._

import cats.free.Free.liftF

sealed trait StorageAction[T]

case object GetAllTodos extends StorageAction[List[Todo]]
case class GetTodo(id: UUID) extends StorageAction[Option[Todo]]
case class SaveTodo(todo: Todo) extends StorageAction[Unit]
case class DeleteTodo(id: UUID) extends StorageAction[Unit]
case object DeleteAllTodos extends StorageAction[Unit]

object StorageAction {
  val getTodos: StorageF[List[Todo]] = liftF(GetAllTodos)
  def getTodo(id: UUID): StorageF[Option[Todo]] = liftF(GetTodo(id))
  def saveTodo(todo: Todo): StorageF[Unit] = liftF(SaveTodo(todo))
  def deleteTodo(id: UUID): StorageF[Unit] = liftF(DeleteTodo(id))
  val deleteAllTodos: StorageF[Unit] = liftF(DeleteAllTodos)
}
