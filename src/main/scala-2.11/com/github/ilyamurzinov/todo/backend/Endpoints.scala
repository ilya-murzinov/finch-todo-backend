package com.github.ilyamurzinov.todo.backend

import java.util.UUID

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.dsl.logic._
import com.github.ilyamurzinov.todo.backend.dsl.logging._
import com.github.ilyamurzinov.todo.backend.interpreters.TodoInterpreter

import cats.Show
import cats.data.Xor
import cats.free.Free
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import shapeless.{HNil, ::}

trait Endpoints {
  type TodoEndpoint[T] = Endpoint[Free[TodoApp, Output[T]]]

  val s: Show[Todo] = implicitly
  val L: LogI[TodoApp] = implicitly
  val T: TodoI[TodoApp] = implicitly
  import L._
  import T._

  val getTodosEndpoint: TodoEndpoint[List[Todo]] = get("todos").map { _ =>
    for {
      _ <- infoI("Getting all Todos")
      list <- getAllTodosI
    } yield Ok(list)
  }

  val getTodoEndpoint: TodoEndpoint[Todo] =
    get("todos" :: uuid).map { id: UUID =>
      for {
        _ <- infoI(s"Getting Todo by id '$id'")
        t <- getTodoI(id)
      } yield Xor.fromOption(t, TodoNotFound(id)).fold(NotFound, Ok)
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
        _ <- infoI(s"Saving new Todo: ${s.show(t)}")
        t <- saveTodoI(t)
      } yield Ok(t)
    }

  val patchedTodo: Endpoint[Todo => Todo] = body.as[Todo => Todo]

  val patchTodo: TodoEndpoint[Todo] =
    patch("todos" :: uuid :: patchedTodo).map {
      case id :: pt :: HNil =>
        for {
          _ <- infoI(s"Patching Todo with id $id")
          o <- patchTodoI(id, pt)
          _ <- infoI(o.map { t => s"Patched Todo: ${s.show(t)}" }.getOrElse(s"Not patched Todo with id $id"))
        } yield Xor.fromOption(o, TodoNotFound(id)).fold(NotFound, Ok)
    }

  val deleteTodo: TodoEndpoint[Todo] = delete("todos" :: uuid).map {
    id: UUID =>
      for {
        _ <- infoI(s"Deleting Todo with id $id")
        t <- deleteTodoI(id)
      } yield Xor.fromOption(t, TodoNotFound(id)).fold(NotFound, Ok)
  }

  val deleteTodos: TodoEndpoint[List[Todo]] = delete("todos").map { _ =>
    for {
      _ <- infoI("Deleting all Todos")
      list <- deleteAllTodosI
    } yield Ok(list)
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
