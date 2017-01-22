package todo.backend.minimal

import java.util.UUID

import todo.backend.core._

import cats.syntax.either._
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import scala.Either
import shapeless.{HNil, ::}

trait Endpoints {

  val getTodosEndpoint: Endpoint[List[Todo]] = get("todos") {
    Ok(TodoRepository.getAllTodos)
  }

  val getEndpoint: Endpoint[Todo] =
    get("todos" :: uuid) { id: UUID =>
      Either
        .fromOption(TodoRepository.getTodo(id), TodoNotFound(id))
        .fold(NotFound, Ok)
    }

  def postedTodo(baseUrl: String): Endpoint[Todo] =
    jsonBody[Todo => Todo].map { f =>
      val id = UUID.randomUUID
      f(
        Todo(
          id = id,
          title = "",
          completed = false,
          order = 1,
          url = s"$baseUrl/todos/$id"
        )
      )
    }

  def postTodo(externalUrl: String): Endpoint[Todo] =
    post("todos" :: postedTodo(externalUrl)) { t: Todo =>
      TodoRepository.saveTodo(t)
      Ok(t)
    }

  val patchedTodo: Endpoint[Todo => Todo] = jsonBody[Todo => Todo]

  val patchTodo: Endpoint[Todo] =
    patch("todos" :: uuid :: patchedTodo).mapOutput {
      case id :: pt :: HNil =>
        val patched = TodoRepository
          .getTodo(id)
          .map(pt)
        patched.map(TodoRepository.saveTodo(_))
        Either.fromOption(patched, TodoNotFound(id)).fold(NotFound, Ok)
    }

  val deleteTodo: Endpoint[Todo] = delete("todos" :: uuid) { id: UUID =>
    val deleted = TodoRepository.getTodo(id)
    deleted.map(_ => TodoRepository.deleteTodo(id))
    Either.fromOption(deleted, TodoNotFound(id)).fold(NotFound, Ok)
  }

  val deleteTodos: Endpoint[List[Todo]] = delete("todos") {
    val todos = TodoRepository.getAllTodos
    TodoRepository.deleteAllTodos
    Ok(todos)
  }

  val opts: Endpoint[Unit] = options(*) {
    NoContent[Unit].withHeader(("Allow", "POST, GET, OPTIONS, DELETE, PATCH"))
  }

  def endpoint(externalUrl: String) =
    getTodosEndpoint :+:
      getEndpoint :+:
        postTodo(externalUrl) :+:
          deleteTodo :+:
            deleteTodos :+:
              patchTodo :+:
                opts
}
