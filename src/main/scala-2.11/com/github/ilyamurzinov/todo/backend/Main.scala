package com.github.ilyamurzinov.todo.backend

import java.util.UUID

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
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

  val api: Service[Request, Response] = endpoint(externalUrl)
    .withHeader(("Access-Control-Allow-Origin", "*"))
    .withHeader(
      ("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PATCH")
    )
    .withHeader(("Access-Control-Max-Age", "3600"))
    .withHeader(("Access-Control-Allow-Headers", """Content-Type,
        |Cache-Control,
        |Content-Language,
        |Expires,
        |Last-Modified,
        |Pragma,
        |X-Requested-With,
        |Origin,
        |Accept
      """.stripMargin.filter(_ >= ' ')))
    .toService

  def main(): Unit = {
    val server = Http.server.serve(internalUrl, api)

    onExit { server.close() }

    Await.ready(server)
  }
}
