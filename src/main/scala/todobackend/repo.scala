package todobackend

import java.util.UUID

object repo {
  private[this] var storage: Map[UUID, TodoItem] = Map()

  def getAllTodos: List[TodoItem] = storage.values.toList
  def getTodo(id: UUID): Option[TodoItem] = storage.get(id)
  def saveTodo(todo: TodoItem): Unit = storage = storage + (todo.id -> todo)
  def deleteTodo(id: UUID): Unit = storage = storage - id
  def deleteAllTodos(): Unit = storage = Map()
}