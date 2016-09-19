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
import com.twitter.finagle.http.filter.Cors
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import com.twitter.util.Future
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

/**
  * Backend for TODO application
  * See [[http://www.todobackend.com/ TODO backend website]],
  * [[http://www.todobackend.com/specs/index.html Specification]]
  *
  * @author Murzinov Ilya [murz42@gmail.com]
  */
object Main extends TwitterServer with Config {
  val host = serverConfig.getString("host")
  val port = serverConfig.getString("port")
  val internalUrl: String = s"$host:$port"
  val externalUrl = serverConfig.getString("externalUrl")

  type TodoEndpoint[T] = Endpoint[Free[TodoApp, Output[T]]]

  val L: LogI[TodoApp] = implicitly
  val T: TodoI[TodoApp] = implicitly
  import L._
  import T._

  val getTodosEndpoint: TodoEndpoint[List[Todo]] = get("todos").map { _ =>
    for {
      _ <- infoI("getting all todos")
      list <- getAllTodosI
    } yield Ok(list)
  }

  val getTodoEndpoint: TodoEndpoint[Todo] =
    get("todos" :: uuid).map { id: UUID =>
      for {
        t <- getTodoI(id)
      } yield Xor.fromOption(t, TodoNotFound(id)).fold(BadRequest, Ok)
    }

  val postTodo: TodoEndpoint[Todo] =
    post("todos" :: postedTodo(externalUrl)).map { t: Todo =>
      for {
        t <- saveTodoI(t)
      } yield Ok(t)
    }

  // val patchTodo: TodoEndpoint[Todo] =
  //   patch("todos" :: uuid :: patchedTodo).map { (id: UUID, pt: Todo => Todo) =>
  //     for {
  //       t <- patchTodoI(id, pt)
  //     } yield Xor.fromOption(t, TodoNotFound(id)).fold(BadRequest, Ok)
  //   }

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

  val patchedTodo: Endpoint[Todo => Todo] = body.as[Todo => Todo]

  val opts: Endpoint[Unit] = options(*) {
    NoContent[Unit].withHeader(("Allow", "POST, GET, OPTIONS, DELETE, PATCH"))
  }

  val service: Service[Request, Response] =
    (getTodosEndpoint.mapOutputAsync(TodoInterpreter.interpret) :+:
      getTodoEndpoint.mapOutputAsync(TodoInterpreter.interpret) :+:
        postTodo.mapOutputAsync(TodoInterpreter.interpret) :+:
          deleteTodo.mapOutputAsync(TodoInterpreter.interpret) :+:
            deleteTodos.mapOutputAsync(TodoInterpreter.interpret) :+:
              // patchTodo :+:
              opts).toService

  val policy: Cors.Policy = Cors.Policy(
    allowsOrigin = _ => Some("*"),
    allowsMethods = _ => Some(Seq("GET", "POST", "OPTIONS", "DELETE", "PATCH")),
    allowsHeaders = _ => Some(Seq("Accept"))
  )

  val api: Service[Request, Response] =
    new Cors.HttpFilter(policy).andThen(service)

  def main(): Unit = {
    val server = Http.server.serve(internalUrl, api)

    onExit { server.close() }

    Await.ready(server)
  }
}
