package todobackend

import java.util.UUID

import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import shapeless.{::, HNil}

class Endpoints(baseUrl: String) {

  private[this] def toView(item: TodoItem): TodoItemView =
    TodoItemView(item.id, item.title, item.completed, item.order, s"$baseUrl/todos/${item.id}")

  private[this] val getTodosEndpoint: Endpoint[List[TodoItem]] = get("todos") {
    Ok(repo.getAllTodos)
  }

  private[this] val getEndpoint: Endpoint[TodoItem] =
    get("todos" :: path[UUID]) { id: UUID =>
      repo.getTodo(id).toLeft(TodoNotFound(id)).fold(Ok, NotFound)
    }

  private[this] val postedTodo: Endpoint[TodoItem] =
    jsonBody[TodoItem => TodoItem].map { f =>
      f(
        TodoItem(
          id = UUID.randomUUID,
          title = "",
          completed = false,
          order = 1
        )
      )
    }

  private[this] val postTodo: Endpoint[TodoItem] =
    post("todos" :: postedTodo) { t: TodoItem =>
      repo.saveTodo(t)
      Ok(t)
    }

  private[this] val patchedTodo: Endpoint[TodoItem => TodoItem] = jsonBody[TodoItem => TodoItem]

  private[this] val patchTodo: Endpoint[TodoItem] =
    patch("todos" :: path[UUID] :: patchedTodo).mapOutput {
      case id :: pt :: HNil =>
        val patched = repo.getTodo(id).map(pt)
        patched.foreach(repo.saveTodo)
        patched.toLeft(TodoNotFound(id)).fold(Ok, NotFound)
    }

  private[this] val deleteTodo: Endpoint[TodoItem] = delete("todos" :: path[UUID]) { id: UUID =>
    val deleted = repo.getTodo(id)
    deleted.foreach(_ => repo.deleteTodo(id))
    deleted.toLeft(TodoNotFound(id)).fold(Ok, NotFound)
  }

  private[this] val deleteTodos: Endpoint[List[TodoItem]] = delete("todos") {
    val todos = repo.getAllTodos
    repo.deleteAllTodos()
    Ok(todos)
  }

  private[this] val opts: Endpoint[Unit] = options(*) {
    NoContent[Unit].withHeader(("Allow", "POST, GET, OPTIONS, DELETE, PATCH"))
  }

  val apiEndpoint =
    getTodosEndpoint.map(_.map(toView)) :+:
      getEndpoint.map(toView) :+:
        postTodo.map(toView) :+:
          deleteTodo.map(toView) :+:
            deleteTodos.map(_.map(toView)) :+:
              patchTodo.map(toView) :+:
                opts
}
