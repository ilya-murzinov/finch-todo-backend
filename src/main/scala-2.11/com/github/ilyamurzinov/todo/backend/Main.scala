package com.github.ilyamurzinov.todo.backend

import java.util.UUID

import com.github.ilyamurzinov.todo.backend.dsl._
import com.github.ilyamurzinov.todo.backend.dsl.logic.TodoAction._
import com.github.ilyamurzinov.todo.backend.interpreters.TodoInterpreter

import cats.data.Xor
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import com.twitter.util.Future
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.catbird.util._

/**
  * Backend for TODO application
  * See [[http://www.todobackend.com/ TODO backend website]],
  * [[http://www.todobackend.com/specs/index.html Specification]]
  *
  * @author Murzinov Ilya [murz42@gmail.com]
  */
object Main extends Config {
  val host = serverConfig.getString("host")
  val port = serverConfig.getString("port")
  val internalUrl: String = s"$host:$port"
  val externalUrl = serverConfig.getString("externalUrl")

  type TodoEndpoint[T] = Endpoint[TodoF[Output[T]]]

  val getTodosEndpoint: TodoEndpoint[List[Todo]] = get("todos").map { _ =>
    for {
      list <- getAllTodos
    } yield Ok(list)
  }

  val getTodoEndpoint: TodoEndpoint[TodoNotFound Xor Todo] =
    get("todos" :: uuid).map { id: UUID =>
      for {
        t <- getTodo(id)
      } yield Ok(Xor.fromOption(t, TodoNotFound(id)))
    }

  // def postedTodo(baseUrl: String): Endpoint[Todo] =
  //   body.as[Todo => Todo].map { f =>
  //     val id = UUID.randomUUID
  //     f(Todo(
  //       id = id,
  //       title = "",
  //       completed = false,
  //       order = 1,
  //       url = s"$baseUrl/todos/$id"
  //     ))
  //   }

  // val postTodo: Endpoint[Todo] =
  //   post("todos" :: postedTodo(externalUrl)) { t: Todo =>
  //     TodoRepo.save(t)
  //     Created(t)
  //   }

  // val deleteTodo: Endpoint[Todo] = delete("todos" :: uuid) { id: UUID =>
  //   TodoRepo.get(id).map {
  //     case Some(t) => TodoRepo.delete(id); Ok(t)
  //     case None => throw new TodoNotFound(id)
  //   }
  // }

  // val deleteTodos: TodoEndpoint[List[Todo]] = delete("todos").map { _ =>
  //   for {
  //     list <- getAll
  //   } yield Ok(list)
  // }

  // val patchedTodo: Endpoint[Todo => Todo] = body.as[Todo => Todo]

  // val patchTodo: Endpoint[Todo] =
  //   patch("todos" :: uuid :: patchedTodo) { (id: UUID, pt: Todo => Todo) =>
  //     TodoRepo.get(id).map {
  //       case Some(currentTodo) =>
  //         val newTodo: Todo = pt(currentTodo)
  //         TodoRepo.delete(id)
  //         TodoRepo.save(newTodo)

  //         Ok(newTodo)
  //       case None => throw TodoNotFound(id)
  //     }
  //   }

  val opts: Endpoint[Unit] = options(*) {
    NoContent[Unit].withHeader(("Allow", "POST, GET, OPTIONS, DELETE, PATCH"))
  }

  def interpret[T](action: TodoF[T]): Future[T] =
    action.foldMap(TodoInterpreter.interpreter)

  val api: Service[Request, Response] = (
    getTodosEndpoint.mapOutputAsync(interpret) :+:
      getTodoEndpoint.mapOutputAsync(interpret) // :+:
    // postTodo :+:
    // deleteTodo :+:
    // deleteTodos.mapOutputAsync(interpret) :+:
    // patchTodo :+:
    // opts
  ).withHeader(
      ("Access-Control-Allow-Origin", "*")
    )
    .withHeader(
      ("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PATCH")
    )
    .withHeader(
      ("Access-Control-Max-Age", "3600")
    )
    .withHeader(
      (
        "Access-Control-Allow-Headers",
        """Content-Type,
        |Cache-Control,
        |Content-Language,
        |Expires,
        |Last-Modified,
        |Pragma,
        |X-Requested-With,
        |Origin,
        |Accept
      """.stripMargin.filter(_ >= ' ')
      )
    )
    .toService

  def main(args: Array[String]): Unit = {
    Await.ready(Http.server.serve(internalUrl, api))
  }
}
