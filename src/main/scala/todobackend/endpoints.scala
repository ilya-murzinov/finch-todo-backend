package todobackend

import java.util.UUID
import java.util.UUID.randomUUID

import cats.effect.{ContextShift, IO}
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

case class TodoItemResponse(id: UUID, title: String, completed: Boolean, order: Int, url: String)
case class CreateTodoItemRequest(title: String, completed: Option[Boolean], order: Option[Int])

class Endpoints(externalUrl: String, repo: Repo)(
  implicit S: ContextShift[IO]
) extends Endpoint.Module[IO] {

  private[this] val root = path("todos")

  val getTodosEndpoint: Endpoint[IO, List[TodoItem]] = get(root) {
    repo.getAll.map(Ok)
  }

  val getTodo: Endpoint[IO, TodoItem] =
    get(root :: path[UUID]) { id: UUID =>
      repo.get(id).map(_.fold(NotFound, Ok))
    }

  val postTodo: Endpoint[IO, TodoItem] =
    post(root :: jsonBody[CreateTodoItemRequest]) { t: CreateTodoItemRequest =>
      repo.save(
        TodoItem(
          randomUUID(),
          t.title,
          t.completed.getOrElse(false),
          t.order.getOrElse(0)
        )
      ).map(Ok)
    }

  val patchTodo: Endpoint[IO, TodoItem] =
    patch(root :: path[UUID] :: jsonBody[TodoItem => TodoItem]) { (id: UUID, updateFn: TodoItem => TodoItem) =>
      repo.update(id, updateFn).map(_.fold(NotFound, Ok))
    }

  val deleteTodo: Endpoint[IO, TodoItem] = delete(root :: path[UUID]) { id: UUID =>
    repo.delete(id).map(_.fold(NotFound, Ok))
  }

  val deleteTodos: Endpoint[IO, List[TodoItem]] = delete(root) {
    repo.deleteAll().map(Ok)
  }

  val opts: Endpoint[IO, Unit] = options(empty) {
    NoContent[Unit].withHeader(("Allow", "POST, GET, OPTIONS, DELETE, PATCH"))
  }

  val apiEndpoint =
    getTodosEndpoint.map(_.map(toResponse)) :+:
      getTodo.map(toResponse) :+:
      postTodo.map(toResponse) :+:
      deleteTodo.map(toResponse) :+:
      deleteTodos.map(_.map(toResponse)) :+:
      patchTodo.map(toResponse) :+:
      opts

  private[this] def toResponse(item: TodoItem): TodoItemResponse =
    TodoItemResponse(item.id, item.title, item.completed, item.order, s"$externalUrl/todos/${item.id}")
}
