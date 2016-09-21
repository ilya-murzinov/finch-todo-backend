package com.github.ilyamurzinov.todo.backend

import java.util.UUID

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.dsl.logic._
import com.github.ilyamurzinov.todo.backend.dsl.logging._
import com.github.ilyamurzinov.todo.backend.interpreters.TodoInterpreter

import cats.data.{Coproduct, Xor}
import cats.free.Free
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import shapeless.{HNil, ::}

trait Endpoints {
  type TodoEndpoint[T] = Endpoint[Free[TodoApp, Output[T]]]

  val L: LogI[TodoApp] = implicitly
  val T: TodoI[TodoApp] = implicitly
  import L._
  import T._

  val getTodosEndpoint: TodoEndpoint[List[Todo]] = get("todos").map { _ =>
    for {
      _ <- infoI("Getting all todos")
      list <- getAllTodosI
    } yield Ok(list)
  }

  val getTodoEndpoint: TodoEndpoint[Todo] =
    get("todos" :: uuid).map { id: UUID =>
      for {
        _ <- infoI(s"Getting todo by id '$id'")
        t <- getTodoI(id)
      } yield Xor.fromOption(t, TodoNotFound(id)).fold(BadRequest, Ok)
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
        ))
    }

  def postTodo(externalUrl: String): TodoEndpoint[Todo] =
    post("todos" :: postedTodo(externalUrl)).map { t: Todo =>
      for {
        t <- saveTodoI(t)
      } yield Ok(t)
    }

  val patchedTodo: Endpoint[Todo => Todo] = body.as[Todo => Todo]

  val patchTodo: TodoEndpoint[Todo] =
    patch("todos" :: uuid :: patchedTodo).map {
      case id :: pt :: HNil =>
        for {
          t <- patchTodoI(id, pt)
        } yield Xor.fromOption(t, TodoNotFound(id)).fold(BadRequest, Ok)
    }

  val deleteTodo: TodoEndpoint[Todo] = delete("todos" :: uuid).map {
    id: UUID =>
      for {
        t <- deleteTodoI(id)
      } yield Xor.fromOption(t, TodoNotFound(id)).fold(BadRequest, Ok)
  }

  val deleteTodos: TodoEndpoint[List[Todo]] = delete("todos").map { _ =>
    for {
      list <- deleteAllTodosI
    } yield Ok(list)
  }

  val opts: Endpoint[Unit] = options(*) {
    NoContent[Unit].withHeader(("Allow", "POST, GET, OPTIONS, DELETE, PATCH"))
  }

  def service(externalUrl: String): Service[Request, Response] =
    (getTodosEndpoint.mapOutputAsync(TodoInterpreter.interpret) :+:
      getTodoEndpoint.mapOutputAsync(TodoInterpreter.interpret) :+:
        postTodo(externalUrl).mapOutputAsync(TodoInterpreter.interpret) :+:
          deleteTodo.mapOutputAsync(TodoInterpreter.interpret) :+:
            deleteTodos.mapOutputAsync(TodoInterpreter.interpret) :+:
              patchTodo.mapOutputAsync(TodoInterpreter.interpret) :+:
                opts).toService
}
