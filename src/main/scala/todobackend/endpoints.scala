package todobackend

import java.util.UUID

import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import io.finch.syntax.scalaFutures._

import scala.concurrent.{ExecutionContext, Future}

class Endpoints(externalUrl: String)(implicit ec: ExecutionContext) {

  private[this] def toView(item: TodoItem): TodoItemView =
    TodoItemView(item.id, item.title, item.completed, item.order, s"$externalUrl/todos/${item.id}")

  private[this] val root = path("todos")

  val getTodosEndpoint: Endpoint[List[TodoItem]] = get(root) {
    repo.getAllTodos.map(Ok)
  }

  val getEndpoint: Endpoint[TodoItem] =
    get(root :: path[UUID]) { id: UUID =>
      repo.getTodo(id).map(_.toLeft(TodoNotFound(id)).fold(Ok, NotFound))
    }

  val postedTodo: Endpoint[TodoItem] =
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

  val postTodo: Endpoint[TodoItem] =
    post(root :: postedTodo) { t: TodoItem =>
      repo.saveTodo(t)
      Ok(t)
    }

  val patchTodo: Endpoint[TodoItem] = {
    val patchedTodo: Endpoint[TodoItem => TodoItem] = jsonBody[TodoItem => TodoItem]

    def handle(id: UUID, pt: TodoItem => TodoItem): Future[Output[TodoItem]] =
      for {
      patched <- repo.getTodo (id).map (_.map (pt) )
      _ <- patched.fold (Future.unit) (repo.saveTodo)
      } yield patched.toLeft (TodoNotFound (id) ).fold(Ok, NotFound)

    patch(root :: path[UUID] :: patchedTodo).apply(handle _)
  }

  val deleteTodo: Endpoint[TodoItem] = delete(root :: path[UUID]) { id: UUID =>
    val deleted = repo.getTodo(id)
    deleted.foreach(_ => repo.deleteTodo(id))
    deleted.map(_.toLeft(TodoNotFound(id)).fold(Ok, NotFound))
  }

  val deleteTodos: Endpoint[List[TodoItem]] = delete(root) {
    for {
      todos <- repo.getAllTodos
      _ <- repo.deleteAllTodos()
    } yield Ok(todos)
  }

  val opts: Endpoint[Unit] = options(*) {
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
