package todo.backend.minimal

import java.util.UUID

import todo.backend.core._

object TodoRepository {
  private[this] var storage: Map[UUID, Todo] = Map()

  def getAllTodos: List[Todo] = storage.values.toList
  def getTodo(id: UUID): Option[Todo] = storage.get(id)
  def saveTodo(todo: Todo): Unit = storage = storage + (todo.id -> todo)
  def deleteTodo(id: UUID): Unit = storage = storage - id
  def deleteAllTodos(): Unit = storage = Map()
}