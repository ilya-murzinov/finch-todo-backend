package com.github.ilyamurzinov.todo.backend

import java.util.UUID

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.interpreters.TodoInterpreter

import cats.data.Xor
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import shapeless.{HNil, ::}

trait Endpoints {

  type TodoEndpoint[T] = Endpoint[TodoAppF[Output[T]]]

  val service: TodoService = new TodoService()

  val getTodosEndpoint: TodoEndpoint[List[Todo]] = get("todos").map { _ =>
    service.getAllTodos.map(Ok)
  }

  val getTodoEndpoint: TodoEndpoint[Todo] =
    get("todos" :: uuid).map { id: UUID =>
      service
        .getTodo(id)
        .map(Xor.fromOption(_, TodoNotFound(id)).fold(NotFound, Ok))
    }

  def postedTodo(baseUrl: String): Endpoint[Todo] =
    body.as[Todo => Todo].map { f =>
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

  def postTodo(externalUrl: String): TodoEndpoint[Todo] =
    post("todos" :: postedTodo(externalUrl)).map { t: Todo =>
      service.saveTodo(t).map(Ok)
    }

  val patchedTodo: Endpoint[Todo => Todo] = body.as[Todo => Todo]

  val patchTodo: TodoEndpoint[Todo] =
    patch("todos" :: uuid :: patchedTodo).map {
      case id :: pt :: HNil =>
        service
          .updateTodo(id, pt)
          .map(Xor.fromOption(_, TodoNotFound(id)).fold(NotFound, Ok))
    }

  val deleteTodo: TodoEndpoint[Todo] = delete("todos" :: uuid).map {
    id: UUID =>
      service
        .deleteTodo(id)
        .map(Xor.fromOption(_, TodoNotFound(id)).fold(NotFound, Ok))
  }

  val deleteTodos: TodoEndpoint[List[Todo]] = delete("todos").map { _ =>
    service.deleteAllTodos.map(Ok)
  }

  val opts: Endpoint[Unit] = options(*) {
    NoContent[Unit].withHeader(("Allow", "POST, GET, OPTIONS, DELETE, PATCH"))
  }

  def endpoint(externalUrl: String) =
    getTodosEndpoint.mapOutputAsync(TodoInterpreter.interpret) :+:
      getTodoEndpoint.mapOutputAsync(TodoInterpreter.interpret) :+:
        postTodo(externalUrl).mapOutputAsync(TodoInterpreter.interpret) :+:
          deleteTodo.mapOutputAsync(TodoInterpreter.interpret) :+:
            deleteTodos.mapOutputAsync(TodoInterpreter.interpret) :+:
              patchTodo.mapOutputAsync(TodoInterpreter.interpret) :+:
                opts
}
