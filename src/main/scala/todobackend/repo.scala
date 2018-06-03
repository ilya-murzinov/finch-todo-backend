package todobackend

import java.util.UUID

import scala.concurrent.Future

object repo {
  private[this] var storage: Map[UUID, TodoItem] = Map()

  def getAllTodos: Future[List[TodoItem]] = Future.successful(storage.values.toList)

  def getTodo(id: UUID): Future[Option[TodoItem]] = Future.successful(storage.get(id))

  def saveTodo(todo: TodoItem): Future[Unit] = {
    storage = storage + (todo.id -> todo)
    Future.unit
  }

  def deleteTodo(id: UUID): Future[Unit] = {
    storage = storage - id
    Future.unit
  }

  def deleteAllTodos(): Future[Unit] = {
    storage = Map()
    Future.unit
  }
}