package todobackend

import java.util.UUID

case class TodoItem(id: UUID, title: String, completed: Boolean, order: Int)
case class TodoItemView(id: UUID, title: String, completed: Boolean, order: Int, url: String)

case class TodoNotFound(id: UUID) extends Exception(s"Todo with id '$id' not found")
