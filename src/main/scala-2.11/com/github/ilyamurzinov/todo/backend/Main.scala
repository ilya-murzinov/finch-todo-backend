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
object Main extends TwitterServer with Endpoints with Config {
  val host = serverConfig.getString("host")
  val port = serverConfig.getString("port")
  val internalUrl: String = s"$host:$port"
  val externalUrl = serverConfig.getString("externalUrl")

  val policy: Cors.Policy = Cors.Policy(
    allowsOrigin = _ => Some("*"),
    allowsMethods = _ => Some(Seq("GET", "POST", "OPTIONS", "DELETE", "PATCH")),
    allowsHeaders = _ => Some(Seq("Accept"))
  )

  val api: Service[Request, Response] =
    new Cors.HttpFilter(policy).andThen(service(externalUrl))

  def main(): Unit = {
    val server = Http.server.serve(internalUrl, api)

    onExit { server.close() }

    Await.ready(server)
  }
}
