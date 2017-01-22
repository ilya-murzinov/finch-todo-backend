package todo.backend.core

import java.util.UUID

case class Todo(id: UUID,
                title: String,
                completed: Boolean,
                order: Int,
                url: String)

case class TodoNotFound(id: UUID)
    extends Exception(s"Todo with id '$id' not found")
